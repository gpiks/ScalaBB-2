package dataparallel

import java.util.concurrent.ThreadLocalRandom
import java.util.function.Supplier

object Simple {
  def main(args: Array[String]): Unit = {
    val nums = (1 to 10000000).par
    val start = System.nanoTime

    val avg = nums
        .map(_.toDouble)
        .map(math.sin _)
        .map(math.asin _)
        .map(math.cos _)
      .aggregate((0.0, 0))((b, i) => (b._1 + i, b._2 + 1), (b, b1) => (b._1 + b1._1, b._2 + b1._2))

    val time = System.nanoTime() - start;
    println(f"Time was ${time / 1000000000D}%6.3f")
  }
}

object SimpleJava {
  class Avg {
    var sum: Double = 0.0
    var count: Long = 0

    def include(v: Double): Unit = {
      sum += v
      count += 1
    }

    def merge(other: Avg): Unit = {
      sum += other.sum
      count += other.count
    }

    def get: Double = sum / count
  }

  def main(args: Array[String]): Unit = {
    val p: Supplier[Avg] = () => new Avg
    import java.util.stream.DoubleStream
    val start = System.nanoTime()
    DoubleStream.generate(() => ThreadLocalRandom.current().nextDouble(-math.Pi, math.Pi))
        .parallel()
        .limit(100000000L)
        .map(math.sin _)
          .collect(p, (b:Avg, v:Double)=> b.include(v), (b:Avg, b1:Avg) => b.merge(b1))
    val time = System.nanoTime() - start
    println(f"Time was ${time / 1000000000D}%6.3f")
  }
}
