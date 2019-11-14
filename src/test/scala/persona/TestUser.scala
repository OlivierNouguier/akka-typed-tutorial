package persona
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll
import org.scalacheck.Gen

import scala.math.Ordering

object TestUser extends App {
  implicit val randomUser: Arbitrary[User] = Arbitrary(for {
    randomName <- Gen.alphaStr
    randomAge <- Gen.choose(0, 80)
  } yield User(randomName, randomAge))

  println(randomUser.arbitrary.sample)

  val allAdultCanDrink = forAll { user: User =>
    if (user.isAdult)
      user.isAllowedToDrink
    else true
  }

  allAdultCanDrink.check()

  implicit val userOrdering: Ordering[User] = Ordering.by(_.age)

}
