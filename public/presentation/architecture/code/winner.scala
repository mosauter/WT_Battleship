class WinActor extends Actor {
    // create an actor
    val playerer = context.actorOf(Props[PlayerDestroyedActor], PlayerDestroyedActor.ACTOR_NAME)
    // set timeout for ask -> ?
    implicit val timeout = Timeout(5 seconds)

    def handleMessage(player1: IPlayer, player2: IPlayer, ref: ActorRef) = {
        // ? -> ask an ActorRef, returns a future
        val pl1 = (playerer ? PlayerDestroyedMessage(player1, first = true)).mapTo[PlayerDestroyedResponse]
        val pl2 = (playerer ? PlayerDestroyedMessage(player2, first = false)).mapTo[PlayerDestroyedResponse]

        // use the FIRST future result with flatMap
        val entireFuture = pl1 flatMap { first =>
            // use the SECOND future reuslt with map to have the same depth level
            pl2 map { second =>
                if (first.destroyed || second.destroyed) {
                    // ! -> fire and forget a response
                    ref ! WinnerResponse(won = true, winner = if (first.destroyed) player2 else player1)
                } else {
                    ref ! WinnerResponse(won = false, winner = null)
                }
            }
        }
    }

    override def receive: Receive = {
        case msg: WinMessage =>
            // save sender now
            handleMessage(msg.player1, msg.player2, sender())
        case msg => unhandled(msg)
    }
}
