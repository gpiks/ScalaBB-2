package futuristic

import java.io.IOException

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Future, Promise}

object TryAFuture {
  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    Future {
      println(s"my future is uncertain! running in ${Thread.currentThread().getName}")
      Thread.sleep(1000);
      "This is the result..."
    }.map(x => {
      println(s"stage two got $x running in ${Thread.currentThread().getName}")
      Thread.sleep(1000)
      "I think it's true that " + x
    }).foreach(println)
    println("future pipeline configured")
    Thread.sleep(4000)
    println("main exiting")
  }
}

object PromiseStuff {
  def readFromDisk(fileName:String): Future[String] = {
    val p = Promise[String]
    new Thread(()=>{
      // go read a disk!
      println("Starting support thread")
      Thread.sleep(2000)
      val readThis = s"Read from $fileName The answer is 42!"
      if (math.random() > 0.5) {
        p.success(readThis)
      } else {
        p.failure(new IOException("disk failed to read"))
      }
    }).start()
    p.future
  }

  def main(args: Array[String]): Unit = {

    Future{
      "FileNameReadFromKeyboard"
    }
      .flatMap(x =>readFromDisk(x))
        .fallbackTo(readFromDisk("AlternateFilename.txt"))
      .map(x => s"I got this from the disk: $x")
      .foreach(println)

    println("disk has been queried...")
    Thread.sleep(3000)
    println("VM shutting down...")
  }
}










