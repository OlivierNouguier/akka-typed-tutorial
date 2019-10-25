package v1
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.WordSpec
import org.scalatest.WordSpecLike

class DeviceSpec extends ScalaTestWithActorTestKit with WordSpecLike {

  import Device._

  "Device actor" must {

    "reply with empty message if no temperature is known" in {
      val probe = createTestProbe[RespondTemperature]()
      val deviceActor = spawn(Device("group", "device"))
      deviceActor ! ReadTemperature(requestId = 42, probe.ref)

      val response = probe.receiveMessage()

      response.requestId should ===(42)
      response.value should ===(None)

    }

    "reply with latest temperature reading " in {
      val recordProbe = createTestProbe[TemperatureRecorded]()
      val readProbe = createTestProbe[RespondTemperature]()

      val deviceActor = spawn(Device("group", "device"))

      deviceActor ! RecordTemperature(42, 24.0, recordProbe.ref)
      recordProbe.expectMessage(TemperatureRecorded(42))

      deviceActor ! ReadTemperature(52, readProbe.ref)
      readProbe.expectMessage(RespondTemperature(52, Some(24)))

    }

  }

}
