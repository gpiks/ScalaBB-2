package actor

import akka.actor.{Actor, ActorSystem, Props}

object Hello {
  private val system = ActorSystem("system")

  sealed case class Message(m: String)
  sealed case class Instruction(m: String)

  class Other(val v: String) extends Actor {
    override def receive: Receive = {
      case Instruction("describe") => println(s"my val is $v, ${Thread.currentThread().getName}")
    }
  }
  class Listener extends Actor {
    override def receive: Receive = {
      case "Hello" => println("you said hello")
      case Message(x) => println(s"you sent me the message $x")
      case Instruction("describe") => println(s"${Thread.currentThread().getName}")
      case Instruction("goSlow") => {
        println(s"Listener going slow on ${Thread.currentThread().getName}")
        Thread.sleep(1000)
        println("Listener done being slow")
      }
      case Instruction(i) if i == "shutdown" => system.terminate()
      case Instruction(i) => println(s"you told me to $i, but I don't know how to")
    }
  }

  def main(args: Array[String]): Unit = {
    val listener = system.actorOf(Props[Listener])
    val other = system.actorOf(Props(new Other("I'm another")))
    listener ! Instruction("describe")
    other ! Instruction("describe")
    other ! Instruction("describe")
    listener ! Instruction("describe")
    listener ! "Hello"
    Thread.sleep(1000)
    listener ! Message("Goodbye")
    listener ! "Hello"
    Thread.sleep(1000)
    listener ! "Hello"
    println("Goodbye")
    listener ! Instruction("buy the newspaper")
    listener ! Instruction("goSlow")
    other ! Instruction("describe")
    listener ! Instruction("goSlow")
    listener ! Instruction("shutdown")

  }
}
