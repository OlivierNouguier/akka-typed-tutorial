package v1
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Signal
import akka.actor.typed.PostStop

object Device {
  def apply(groupId: String, deviceId: String): Behavior[Command] =
    Behaviors.setup(context => new Device(context, groupId, deviceId))

  sealed trait Command
  final case class ReadTemperature(requestId: Long, replyTo: ActorRef[RespondTemperature]) extends Command
  final case class RespondTemperature(requestId: Long, value: Option[Double])

  final case class RecordTemperature(requestId: Long, value: Double, replyTo: ActorRef[TemperatureRecorded])
      extends Command
  final case class TemperatureRecorded(requestId: Long) extends Command
  final object Passivate extends Command
}

class Device(context: ActorContext[Device.Command], groupId: String, deviceId: String)
    extends AbstractBehavior[Device.Command](context) {

  import Device._

  var lastTemperatureReading: Option[Double] = None

  override def onMessage(msg: Device.Command): Behavior[Device.Command] = msg match {
    case RecordTemperature(requestId, value, replyTo) =>
      lastTemperatureReading = Some(value)
      context.log.debug(s"Recorded temperature reading $value with $requestId")
      replyTo ! TemperatureRecorded(requestId)
      this
    case ReadTemperature(requestId, replyTo) =>
      replyTo ! RespondTemperature(requestId, lastTemperatureReading)
      this

    case Passivate =>
      Behaviors.stopped
  }

  override def onSignal: PartialFunction[Signal, Behavior[Device.Command]] = {
    case PostStop =>
      context.log.info(s"Device $groupId-$deviceId.")
      this
  }

}
