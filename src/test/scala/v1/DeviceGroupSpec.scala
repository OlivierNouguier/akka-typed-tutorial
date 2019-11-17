package v1
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import v1.DeviceManager.DeviceRegistered
import v1.DeviceManager.RequestTrackDevice
import org.scalatest.WordSpecLike
import v1.Device.RecordTemperature
import v1.Device._

import scala.concurrent.duration.DurationInt
import v1.DeviceManager.ReplyDeviceList
import v1.DeviceManager.RequestDeviceList

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
    "ignores message in wrong group" in {
      val probe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("group"))

      groupActor ! RequestTrackDevice("wrongGroup", "deviceId", probe.ref)

      probe.expectNoMessage(500.milliseconds)

    }

    "returns same actor for the same device" in {
      val probe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("group"))

      groupActor ! RequestTrackDevice("group", "device-1", probe.ref)

      val deviceActor1 = probe.receiveMessage()

      groupActor ! RequestTrackDevice("group", "device-1", probe.ref)

      val deviceActor2 = probe.receiveMessage()

      deviceActor1.device should ===(deviceActor2.device)
    }

    "be able to list device" in {
      val registeredProbe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("group1"))

      groupActor ! RequestTrackDevice("group1", "device1", registeredProbe.ref)

      registeredProbe.receiveMessage()

      groupActor ! RequestTrackDevice("group1", "device2", registeredProbe.ref)
      registeredProbe.receiveMessage()

      val deviceListProbe = createTestProbe[ReplyDeviceList]()

      groupActor ! RequestDeviceList(1, "group1", deviceListProbe.ref)

      deviceListProbe.expectMessage(ReplyDeviceList(requestId = 1, Set("device1", "device2")))
    }

    "be able to list active devices after one shuts down" in {
      val registeredProbe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("group1"))
      groupActor ! RequestTrackDevice("group1", "device1", registeredProbe.ref)

      val toShutdown = registeredProbe.receiveMessage().device

      groupActor ! RequestTrackDevice("group1", "device2", registeredProbe.ref)
      registeredProbe.receiveMessage()

      val deviceListProbe = createTestProbe[ReplyDeviceList]()

      groupActor ! RequestDeviceList(2, "group1", deviceListProbe.ref)

      deviceListProbe.expectMessage(ReplyDeviceList(2, Set("device1", "device2")))

      toShutdown ! Passivate

      registeredProbe.expectTerminated(toShutdown, registeredProbe.remainingOrDefault)

      // using awaitAssert to retry because it might take longer for the groupActor
      // to see the Terminated, that order is undefined
      registeredProbe.awaitAssert {
        groupActor ! RequestDeviceList(requestId = 1, groupId = "group1", deviceListProbe.ref)
        deviceListProbe.expectMessage(ReplyDeviceList(requestId = 1, Set("device2")))
      }

    }
  }
}
