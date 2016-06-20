$socket.onMessage(function (message) {
    var msg = JSON.parse(message.data);
    console.log("Message %o received", msg);
    switch (msg.type) {
        case messageType.START:
            $scope.firstPlayer = msg.firstPlayer;
            break;
        case messageType.WAIT:
            $scope.me = msg.yourName;
            $scope.enemy = msg.opponentName;
            $scope.waiting = true;
            break;
        case messageType.HIT:
        case messageType.MISS:
            break;
        case messageType.WRONGINPUT:
            alert('Something went wrong, please try again!');
            break;
    }
});
