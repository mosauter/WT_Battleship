case class CollisionMessage(ship1: IShip, ship2: IShip) {}
case class ShipMessage(boardSize: Int, player: IPlayer, ship: IShip) {}
case class BorderMessage(ship: IShip, boardSize: Int) {}
case class PlayerDestroyedMessage(player: IPlayer, first: Boolean) {}
case class PlayerDestroyedResponse(destroyed: Boolean, first: Boolean) {}
case class ShipDestroyedMessage(ship: IShip, player: IPlayer) {}
case class ShootMessage(player1: IPlayer, player2: IPlayer, x: Int, y: Int, first: Boolean) {}
case class WinMessage(player1: IPlayer, player2: IPlayer) {}
case class WinnerResponse(won: Boolean, winner: IPlayer) {}
case class SaveMessage(master: IMasterController) {}
