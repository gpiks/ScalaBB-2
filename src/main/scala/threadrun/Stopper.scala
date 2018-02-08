package threadrun

object Stopper {
  def main(args: Array[String]): Unit = {
    var stop: Boolean = false

    new Thread(()=>{
      while(!stop) {}
      println("stopper thread stopping")
    }).start()

    Thread.sleep(1000)
    stop = true
    println("Main exiting...")
  }
}
