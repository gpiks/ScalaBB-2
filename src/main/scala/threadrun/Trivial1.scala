package threadrun

object Trivial1 {
  class MyJob extends Runnable {
    override def run(): Unit = {
      for (x <- 1 to 100) {
        println(s"${Thread.currentThread().getName} count is $x")
      }
    }
  }

  def main(args: Array[String]): Unit = {
    new Thread(new MyJob).start()
    println(s"${Thread.currentThread().getName} exiting")
  }
}
