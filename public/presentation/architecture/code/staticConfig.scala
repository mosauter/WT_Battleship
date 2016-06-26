// additional actor object -> static informations
object WinActor {
    val ACTOR_NAME = "winner"
}

object Reducers {
    def reduceBooleanAND(one: Boolean, two: Boolean): Boolean = one && two

    def reduceBooleanOR(one: Boolean, two: Boolean): Boolean = one || two
}
