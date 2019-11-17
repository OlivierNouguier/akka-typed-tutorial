package v1
import akka.actor.typed.ActorRef

import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.Signal

import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.PostStop

object DeviceManager {
  trait Command

  final case class RequestTrackDevice(groupId: String, deviceId: String, replyTo: ActorRef[DeviceRegistered])
      extends Command
      with DeviceGroup.Command
  final case class DeviceRegistered(device: ActorRef[Device.Command]) extends Command

  final case class ReplyDeviceList(requestId: Long, ids: Set[String])

  final case class RequestDeviceList(requestId: Long, groupId: String, replyTo: ActorRef[ReplyDeviceList])
      extends Command
      with DeviceGroup.Command

  private final case class DeviceGroupTerminated(groupId: String) extends DeviceManager.Command

}

class DeviceManager(context: ActorContext[DeviceManager.Command])
    extends AbstractBehavior[DeviceManager.Command](context) {
  import DeviceManager._

  var groupId2Actor = Map.empty[String, ActorRef[DeviceGroup.Command]]

  override def onMessage(msg: Command): Behavior[Command] = msg match {

    case req @ RequestTrackDevice(groupId, deviceId, replyTo) =>
      groupId2Actor.get(groupId) match {
        case Some(ref) =>
          ref ! req
        case None =>
          val ref = context.spawn(DeviceGroup(groupId), s"group-$groupId")
          context.watchWith(ref, DeviceGroupTerminated(groupId))
          groupId2Actor += groupId -> ref
          ref ! req
      }
      this

    case req @ RequestDeviceList(requestId, groupId, replyTo) =>
      groupId2Actor.get(groupId) match {
        case Some(ref) =>
          ref ! req
          this
        case None =>
          replyTo ! ReplyDeviceList(requestId, Set.empty)
          Behaviors.unhandled
      }

    case DeviceRegistered(device) =>
      this

    case DeviceGroupTerminated(groupId) =>
      groupId2Actor -= groupId
      this
  }

  override def onSignal: PartialFunction[Signal, Behavior[DeviceManager.Command]] = {
    case PostStop =>
      context.log.info("Device manager stopped")
      this
  }

}
