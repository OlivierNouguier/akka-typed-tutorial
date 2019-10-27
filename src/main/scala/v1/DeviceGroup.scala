package v1
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Behavior
import akka.actor.typed.Signal
import akka.actor.typed.PostStop
import akka.actor.typed.scaladsl.Behaviors

object DeviceGroup {

  def apply(groupId: String): Behavior[Command] = Behaviors.setup(context => new DeviceGroup(context, groupId))

  trait Command

  private final case class DeviceTerminated(device: ActorRef[Device.Command], groupId: String, deviceId: String)
      extends Command

}

class DeviceGroup(context: ActorContext[DeviceGroup.Command], groupId: String)
    extends AbstractBehavior[DeviceGroup.Command](context) {

  import DeviceManager._

  context.log.info(s"Device group $groupId started")

  var deviceIdToActor = Map.empty[String, ActorRef[Device.Command]]

  def onMessage(msg: DeviceGroup.Command): Behavior[DeviceGroup.Command] =
    msg match {
      case RequestTrackDevice(`groupId`, deviceId, replyTo) =>
        deviceIdToActor.get(deviceId) match {
          case None =>
            context.log.debug(s"Spawn device $groupId-$deviceId")
            val deviceActor = context.spawn(Device(groupId, deviceId), s"device-$deviceId")
            deviceIdToActor += deviceId -> deviceActor
            replyTo ! DeviceRegistered(deviceActor)

          case Some(deviceActor) =>
            replyTo ! DeviceRegistered(deviceActor)
        }
        this
      case RequestTrackDevice(otherGroupId, _, _) =>
        context.log.warn(s"This device group($groupId) does not handle device targeted to $otherGroupId")
        this

    }

  override def onSignal: PartialFunction[Signal, Behavior[DeviceGroup.Command]] = {
    case PostStop =>
      context.log.info(s"Device group $groupId stopped.")
      this
  }

}
