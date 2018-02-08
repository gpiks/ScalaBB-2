package broken

import scala.reflect.ClassTag
class BQueue[T](implicit t: ClassTag[T]) {
  private val QSIZE = 10
  private val buffer = new Array[T](QSIZE)
  private var count = 0

  def take: T = {
    this.synchronized {
      while (count == 0) {
        this.wait()
      }
      count -= 1
      val rv = buffer(0)
      System.arraycopy(buffer, 1, buffer, 0, count)
      this.notify() // notifyAll is horribly non-scalable
      rv
    }
  }

  def put(item: T): Unit = {
    this.synchronized {
      while (count == QSIZE) {
        this.wait()
      }
      buffer(count) = item
      count += 1
      this.notify() // notifyAll is horribly non-scalable
    }
  }
}

object TestBQueue {
  def main(args: Array[String]): Unit = {
    val q = new BQueue[Int]
    val producer: Runnable = () => {
      for (x <- 0 until 1000) {
        if (x < 300) Thread.sleep(1)
        q put x
        //        if (x == 998) q put x
      }
      println("All data put, producer quitting")
    }
    val consumer: Runnable = () => {
      for (x <- 0 until 1000) {
        if (x > 700) Thread.sleep(1)
        val r = q.take
        if (r != x) println(s"ERROR, expected $x got $r")
      }
      println("All done receiving")
    }

    new Thread(producer).start()
    new Thread(consumer).start()

    println("jobs started...")
  }
}
