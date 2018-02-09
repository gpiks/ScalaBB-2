package broken

import java.util.concurrent.locks.ReentrantLock

import scala.reflect.ClassTag

class FQueue[T](implicit t: ClassTag[T]) {
  private val QSIZE = 10
  private val buffer = new Array[T](QSIZE)
  private var count = 0
  private val lock = new ReentrantLock
  private val notFull = lock.newCondition
  private val notEmpty = lock.newCondition

  def take: T = {
    lock.lock;
    try {
      while (count == 0) {
        notEmpty.await
      }
      count -= 1
      val rv = buffer(0)
      System.arraycopy(buffer, 1, buffer, 0, count)
      notFull.signal
      rv
    } finally {
      lock.unlock
    }
  }

  def put(item: T): Unit = {
    lock.lock;
    try {
      while (count == QSIZE) {
        notFull.await
      }
      buffer(count) = item
      count += 1
      notEmpty.signal
    } finally {
      lock.unlock
    }
  }
}

object TestFQueue {
  def main(args: Array[String]): Unit = {
    val q = new FQueue[Int]
    val producer: Runnable = () => {
      for (x <- 0 until 1000) {
        if (x < 300) Thread.sleep(1)
        q put x
//        if (x == 998) q put x // test with bad data!
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
