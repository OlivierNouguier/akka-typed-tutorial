package v1
import akka.actor.typed.ActorSystem
import akka.actor.CoordinatedShutdown
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object IotApp extends App {

  val system = ActorSystem[Nothing](IotSupervisor(), "iot-supervisor")

  sys.addShutdownHook {
    println("Byebye!")
    val terminate = system.whenTerminated
    Await.result(terminate, Duration("10 seconds"))
  }
}
