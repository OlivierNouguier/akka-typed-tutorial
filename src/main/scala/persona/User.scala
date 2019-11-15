package persona

case class User(name: String, age: Int) {
  def isAdult: Boolean = age >= 18
  def isAllowedToDrink: Boolean = age >= 21
}
