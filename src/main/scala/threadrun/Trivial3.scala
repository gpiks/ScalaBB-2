package threadrun

object Trivial3 {

  class MyJob extends Runnable {
    var count = 0;

    override def run(): Unit = {
      for (x <- 1 to 10000) count += 1
      println(s"count in ${Thread.currentThread().getName} is $count")
    }
  }

  def main(args: Array[String]): Unit = {
    val r = new MyJob
    val t1 = new Thread(r)
    val t2 = new Thread(r)

    t1.start()
    t2.start()

    println("main exit...")
  }
}
