package primes

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

object ThreadSieve {
  val LIMIT: Long = 40009L
  var start: Long = _

  class SieveElement(q: BlockingQueue[Long]) extends Runnable {
    var myPrime: Long = _
    var nextActor: BlockingQueue[Long] = _

    def run: Unit = {
      myPrime = q.take
//      println(s"new processor, myPrime is $myPrime")
      if (myPrime >= LIMIT) {
        println(f"Time ${(System.nanoTime() - start) / 1000000000F}%6.3f")
        System.exit(0)
      }
      var nextNum = q.take
      while (nextNum % myPrime == 0) {
        nextNum = q.take
      }
      nextActor = new ArrayBlockingQueue[Long](10)
      new SieveElement(nextActor)
      nextActor.put(nextNum)
      while (true) {
        nextNum = q.take
        if (nextNum % myPrime != 0) {
          nextActor put nextNum
        }
      }
    }
    new Thread(this).start
  }

  def main(args: Array[String]): Unit = {
    val twoElement = new ArrayBlockingQueue[Long](10)
    new SieveElement(twoElement)
    start = System.nanoTime()
    for (n <- 2L to Sieve.LIMIT) {
      twoElement put n
    }
    println("sending finished")
  }
}
