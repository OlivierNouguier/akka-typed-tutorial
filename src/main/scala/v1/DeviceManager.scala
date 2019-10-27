package v1
import akka.actor.typed.ActorRef

object DeviceManager {
  trait Command

  final case class RequestTrackDevice(groupId: String, deviceId: String, replyTo: ActorRef[DeviceRegistered])
      extends Command
      with DeviceGroup.Command
  final case class DeviceRegistered(device: ActorRef[Device.Command]) extends Command
}
