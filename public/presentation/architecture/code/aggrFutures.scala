def createFutures(shipList: Array[IShip], player: IPlayer): List[Future[Boolean]] = {
    var list = new ListBuffer[Future[Boolean]]()
    for (counter <- 0 until player.getOwnBoard.getShips) {
        list += (shipper ? ShipDestroyedMessage(shipList(counter), player)).mapTo[Boolean]
    }
    list.toList
}

def handleMessage(msg: PlayerDestroyedMessage, ref: ActorRef) = {
    val futureList = createFutures(msg.player.getOwnBoard.getShipList, msg.player)
    val aggrFuture = Future sequence futureList
    aggrFuture onSuccess {
        case results: List[Boolean] =>
            val result = results.reduce(Reducers.reduceBooleanAND)
            ref ! PlayerDestroyedResponse(result, msg.first)
    }
}
