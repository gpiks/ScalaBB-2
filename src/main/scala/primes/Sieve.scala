package primes

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Sieve {
  val system = ActorSystem("SieveActors")
  lazy val collector = system.actorOf(Props[CollectorElement], "collector")
  lazy val timer = system.actorOf(Props[TimerElement], "timer")
  val LIMIT: Long = 40009L

  class TimerElement extends Actor {
    var start: Long = System.nanoTime

    override def receive: Receive = {
      case "Start" => {
        println("Starting timer")
        start = System.nanoTime
      }
      case "Stop" => {
        val time = System.nanoTime - start
        println(s"Total time taken is ${time / 1000000000.0} seconds")
        system.terminate()
      }
    }
  }

  class CollectorElement extends Actor {
    override def receive: Receive = {
      case x: Long => {
        println(s"Number ${x} is prime")
        if (x == 2L) timer ! "Start"
        if (x >= Sieve.LIMIT) {
          println("Sending stop signal")
          timer ! "Stop"
        }
      }
    }
  }

  class SieveElement extends Actor {
    var myPrime: Long = _
    var nextActor: ActorRef = _

    def ongoing = (n: Long) => {
      if (n % myPrime != 0) {
        nextActor ! n
      }
    }

    def awaitingFirstNewPrime = (n: Long) => {
      if (n % myPrime != 0) {
        nextActor = system.actorOf(Props[SieveElement], s"sieving-by-$n")
        nextActor ! n
        handler = ongoing
      }
    }

    def initial: Long => Unit = n => {
      myPrime = n
      collector ! n
      handler = awaitingFirstNewPrime
    }

    var handler: Long => Unit = initial

    var receive = {
      case n: Long => handler(n)
    }
  }

  def main(args: Array[String]): Unit = {
    val twoElement = system.actorOf(Props[SieveElement], "sieving-by-2")
    for (n <- 2L to Sieve.LIMIT) {
      twoElement ! n
    }
    println("sending finished")
  }
}

// 19979	19991 19993	19997
// 999907  999917  999931  999953  999959  999961  999979  999983  1000003 1000033