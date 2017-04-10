abstract class Permission {
  def weight : Int = 0
}

trait `read user` extends Permission {
  override def weight : Int = 1 + super.weight
}

trait `write user` extends Permission {
  override def weight : Int = 2 + super.weight
}

val `delete user` = new Permission with `read user` with `write user`

`delete user`.weight