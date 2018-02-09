package mandelbrot

import java.awt.Color
import java.awt.Component
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.Image
import java.util

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object MandelbrotImage {
  private val SCREEN_DIMENSION = Toolkit.getDefaultToolkit.getScreenSize
  private val BUFFER_WIDTH = SCREEN_DIMENSION.width
  private val BUFFER_HEIGHT = SCREEN_DIMENSION.height

  def compute(x0: Double, y0: Double): Int = {
    var x = 0.0
    var y = 0.0
    var iteration = 0
    val max_iteration = 4000
    while ((x * x + y * y) < 4 && iteration < max_iteration) {
      val xtemp = x * x - y * y + x0
      y = 2 * x * y + y0
      x = xtemp
      iteration += 1
    }
    iteration
  }
}

final class MandelbrotImage(var originX: Double, var originY: Double, var scale: Double) {
  private var image1 = new BufferedImage(MandelbrotImage.BUFFER_WIDTH, MandelbrotImage.BUFFER_HEIGHT, BufferedImage.TYPE_INT_RGB)
  private var image2 = new BufferedImage(MandelbrotImage.BUFFER_WIDTH, MandelbrotImage.BUFFER_HEIGHT, BufferedImage.TYPE_INT_RGB)
  private var image = image1
  private var offscreenImage = image2
  private var x0 = 0.0
  private var y0 = 0.0
  private val repaintListeners = new util.ArrayList[Component]

  val system = ActorSystem("system")
  val manager = system.actorOf(Props(new ManagerActor(4)))
  val a1 = system.actorOf(Props(new RecalcActor))
  val a2 = system.actorOf(Props(new RecalcActor))
  val a3 = system.actorOf(Props(new RecalcActor))
  val a4 = system.actorOf(Props(new RecalcActor))

  zoomTo(originX, originY, scale)

  def zoomTo(originX: Double, originY: Double, scale: Double): Unit = {
    x0 = originX
    y0 = originY
    recalculate()
  }

  def panTo(originX: Double, originY: Double): Unit = {
    x0 = originX
    y0 = originY
    recalculate()
  }

  def scaleTo(scale: Double): Unit = {
    this.scale = scale
    recalculate()
  }

  def scaleBy(factor: Double): Unit = {
    scale *= factor
    recalculate()
  }

  case class RecalcMessage(startLine: Int, count: Int)

  class ManagerActor(segments: Int) extends Actor {
    var count = segments
    override def receive: Receive = {
      case "START" => count = segments
      case "RC-DONE" => {
        count -= 1
        if (count <=0) {
          // swap the buffers
          val oldimage = image
          image = offscreenImage
          offscreenImage = oldimage
          notifyListeners()
          count = segments
        }
      }
    }
  }

  class RecalcActor extends Actor {
    override def receive: Receive = {
      case RecalcMessage(s,c) => for {
        y <- s until c
        x <- 0 until MandelbrotImage.BUFFER_WIDTH
      }
      {
        val value: Int = MandelbrotImage.compute(getX(x), getY(y))
        offscreenImage.setRGB(x, y, Color.HSBtoRGB(
          (360 * value) / 4000.0F,
          1.0F,
          if (value == 4000) 0.0F else 0.5F))
      }
        manager ! "RC-DONE"
    }
  }
  val BLOCK_HEIGHT = MandelbrotImage.BUFFER_HEIGHT / 4
  private def recalculate(): Unit = {

    a1 ! new RecalcMessage(0, BLOCK_HEIGHT)
    a2 ! new RecalcMessage(BLOCK_HEIGHT, BLOCK_HEIGHT * 2)
    a3 ! new RecalcMessage(BLOCK_HEIGHT * 2, BLOCK_HEIGHT * 3)
    a4 ! new RecalcMessage(BLOCK_HEIGHT * 3, MandelbrotImage.BUFFER_HEIGHT)
//    for {
//      y <- 0 until MandelbrotImage.BUFFER_HEIGHT;
//      x <- 0 until MandelbrotImage.BUFFER_WIDTH
//    }
//    {
//      val value: Int = MandelbrotImage.compute(getX(x), getY(y))
//      image.setRGB(x, y, Color.HSBtoRGB(
//        (360 * value) / 4000.0F,
//        1.0F,
//        if (value == 4000) 0.0F else 0.5F))
//    }
//    notifyListeners()
  }

  def getImage: Image = image

  def getX(x: Int): Double = x0 + ((x - (MandelbrotImage.BUFFER_WIDTH / 2)) * scale)

  def getY(y: Int): Double = y0 - ((y - (MandelbrotImage.BUFFER_HEIGHT / 2)) * scale)

  def addRepaintListener(obs: Component): Unit = {
    repaintListeners.add(obs)
  }

  private def notifyListeners(): Unit =
    repaintListeners.forEach( _ repaint ())
}