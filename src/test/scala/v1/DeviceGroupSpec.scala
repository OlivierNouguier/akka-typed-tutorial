package v1
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import v1.DeviceManager.DeviceRegistered
import v1.DeviceManager.RequestTrackDevice
import org.scalatest.WordSpecLike
import v1.Device.RecordTemperature
import v1.Device.TemperatureRecorded

class DeviceGroupSpec extends ScalaTestWithActorTestKit with WordSpecLike {
  "Device group" should {
    "be able to register device" in {
      val probe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("myGroupId"))

      groupActor ! RequestTrackDevice("myGroupId", "device-1", probe.ref)
      val registeredMessage1 = probe.receiveMessage()
      val deviceActor1 = registeredMessage1.device

      groupActor ! RequestTrackDevice("myGroupId", "device-2", probe.ref)
      val registeredMessage2 = probe.receiveMessage()
      val deviceActor2 = registeredMessage2.device

      deviceActor1 should !==(deviceActor2)

      val recordProbe = createTestProbe[TemperatureRecorded]()

      deviceActor1 ! RecordTemperature(0, 1.0, recordProbe.ref)
      recordProbe.expectMessage(TemperatureRecorded(0))

      deviceActor2 ! Device.RecordTemperature(requestId = 1, 2.0, recordProbe.ref)
      recordProbe.expectMessage(Device.TemperatureRecorded(requestId = 1))

    }
  }
}
