package persona
import org.scalatest.WordSpec
import org.scalatest.Matchers
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

class UserSpec extends WordSpec with Matchers {

  implicit val randomUser: Arbitrary[User] = Arbitrary(for {
    randomName <- Gen.alphaStr
    randomAge <- Gen.choose(0, 80)
  } yield User(randomName, randomAge))

  "User" should {
    "check if adult are authorized to drink" in {
      val allAdultCanDrink = forAll { user: User =>
        if (user.isAdult)
          user.isAllowedToDrink
        else true
      }

      allAdultCanDrink.check()

    }
  }
}
