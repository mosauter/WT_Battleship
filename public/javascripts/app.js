/**
 * Angular App
 * Created by fw on 09.12.15.
 */
var app = angular.module('battleship', ['ngRoute', 'ngWebSocket']);

app.config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'assets/partials/angular/index.html',
            controller: 'HomeCtrl'
        })
        .when('/battle', {
            templateUrl: 'assets/partials/angular/battle.html',
            controller: 'BattleCtrl'
        })
        .otherwise({
            redirectTo: '/'
        });
});

app.filter('range', function () {
    return function (input, total) {
        total = parseInt(total);
        for (var i = 0; i < total; i++) {
            input.push(i);
        }
        return input;
    };
});

app.controller('HomeCtrl', ['$scope', '$location', function ($scope, $location) {
    $scope.fight = function () {
        $location.path('/battle');
    };
}]);

function getSocketAddress() {
    var socketAddress = window.location.origin.replace("http", "ws");
    return socketAddress + "/socket";
}

app.controller('BattleCtrl', ['$scope', '$websocket', '$location', function ($scope, $websocket, $location) {
    var messageType = {
        // HIT and MISS
        HIT: "HIT",
        MISS: "MISS",
        // INVALID
        WRONGINPUT: "WRONGINPUT",
        PLACEERR: "PLACEERR",
        WAIT: "WAIT",
        START: "START",
        // PLACE
        PLACE1: "PLACE1",
        PLACE2: "PLACE2",
        FINALPLACE1: "FINALPLACE1",
        FINALPLACE2: "FINALPLACE2",
        // SHOOT
        SHOOT1: "SHOOT1",
        SHOOT2: "SHOOT2",
        // WIN
        WIN1: "WIN1",
        WIN2: "WIN2"
    };

    $scope.alphabet = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'];
    $scope.field = [];
    $scope.opponent = [];
    $scope.sendNext = 2;
    $scope.duplicate = {};

    $scope.ships = {
        '2': {'isPlaced': false},
        '3': {'isPlaced': false},
        '4': {'isPlaced': false},
        '5': {'isPlaced': false},
        '6': {'isPlaced': false}
    };

    // ALL SHIPS HORIZONTAL
    /*$scope.ships = {
     '2': {'x': 0, 'y': 0, 'orientation': true, 'isPlaced': true},
     '3': {'x': 0, 'y': 1, 'orientation': true, 'isPlaced': true},
     '4': {'x': 0, 'y': 2, 'orientation': true, 'isPlaced': true},
     '5': {'x': 0, 'y': 3, 'orientation': true, 'isPlaced': true},
     '6': {'x': 0, 'y': 4, 'orientation': true, 'isPlaced': true}
     };*/

    // ALL SHIPS VERTICAL
    /*$scope.ships = {
     '2': {'x': 0, 'y': 0, 'orientation': false, 'isPlaced': true},
     '3': {'x': 1, 'y': 0, 'orientation': false, 'isPlaced': true},
     '4': {'x': 2, 'y': 0, 'orientation': false, 'isPlaced': true},
     '5': {'x': 3, 'y': 0, 'orientation': false, 'isPlaced': true},
     '6': {'x': 4, 'y': 0, 'orientation': false, 'isPlaced': true}
     };*/

    $scope.waiting = false;
    $scope.placing = true;

    var $socket = $websocket(getSocketAddress());

    $socket.onOpen(function () {
        console.log('Connection established!');
    });

    $socket.onClose(function () {
        console.log('Connection closed!');
        $location.path('/');
    });

    $socket.onError(function () {
        console.log('An error occured on socket.');
    });

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
            case messageType.PLACEERR:
                $scope.sendNext = msg.errShipLength;
                alert("You can't place ship with length " + msg.errShipLength +
                    " on x:" + $scope.ships['' + msg.errShipLength]['x'] +
                    " y:" + $scope.alphabet[$scope.ships['' + msg.errShipLength]['y']]);
                $scope.removeShip('' + msg.errShipLength);
                var newShips = {};
                for (var i = msg.errShipLength; i < 7; i++) {
                    newShips['' + i] = $scope.ships['' + i];
                }
                $scope.ships = newShips;
                $scope.duplicate = {};
                break;
            case messageType.PLACE1:
            case messageType.PLACE2:
                $scope.fillField($scope.field, msg.shipMap, 's');
                if (Object.keys($scope.duplicate).length !== 0) {
                    $scope.sendShip();
                }
                $scope.waiting = false;
                break;
            case messageType.FINALPLACE1:
            case messageType.FINALPLACE2:
                $scope.fillField($scope.field, msg.shipMap, 's');
                $scope.waiting = false;
                $scope.placing = false;
                break;
            case messageType.SHOOT1:
            case messageType.SHOOT2:
                $scope.fillHitMap($scope.opponent, msg.isShootMap, msg.isHitMap);
                $scope.fillHitMap($scope.field, msg.opponentShootMap, 1);
                break;
            case messageType.WIN1:
            case messageType.WIN2:
                $scope.endOfGame(msg);
                break;
            default:
                break;
        }
    });

    $scope.endOfGame = function (msg) {
        if ($scope.end) {
            return;
        }
        // if opponent closes game in PLACE-States
        // -> this player has won
        $scope.placing = false;
        $scope.waiting = false;

        $scope.field = $scope.initFields();
        $scope.opponent = $scope.initFields();
        var winnerField, looserField, alertMsg;

        if ($scope.firstPlayer && msg.type === messageType.WIN1 ||
            !$scope.firstPlayer && msg.type === messageType.WIN2) {
            winnerField = $scope.field;
            looserField = $scope.opponent;
            alertMsg = 'You won the game!';
        } else {
            winnerField = $scope.opponent;
            looserField = $scope.field;
            alertMsg = 'You lost!';
        }
        $scope.fillField(winnerField, msg.winnerMap, 's');
        $scope.fillField(looserField, msg.looserMap, 's');

        $scope.fillHitMap(looserField, msg.winnerShootMap, 1);
        $scope.fillHitMap(winnerField, msg.looserShootMap, 1);

        alert(alertMsg);
        $scope.end = true;
        $socket.close();
    };

    $scope.goBack = function () {
        $location.path('/');
    };

    $scope.initFields = function () {
        var arr = [];
        for (var i = 0; i < 10; i++) {
            arr[i] = ['x', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x'];
        }
        return arr;
    };

    $scope.field = $scope.initFields();
    $scope.opponent = $scope.initFields();

    $scope.switchOrientation = function (ship) {
        if (($scope.ships[ship]['orientation'] && parseInt(ship) + $scope.ships[ship]['y'] - 1 > 9) ||
            (!$scope.ships[ship]['orientation'] && parseInt(ship) + $scope.ships[ship]['x'] - 1 > 9)) {
            alert("You can't switch the orientation at this position!");
            return;
        }
        $scope.toggleShipOnField($scope.ships[ship]['x'], $scope.ships[ship]['y'], parseInt(ship), 'x');
        $scope.ships[ship]['orientation'] = $scope.ships[ship]['orientation'] ? false : true;
        $scope.toggleShipOnField($scope.ships[ship]['x'], $scope.ships[ship]['y'], parseInt(ship), 's');
    };
    $scope.removeShip = function (ship) {
        $scope.toggleShipOnField($scope.ships[ship]['x'], $scope.ships[ship]['y'], parseInt(ship), 'x');
        $scope.ships[ship] = {'isPlaced': false};
    };
    $scope.placeShip = function (ship) {
        var x = parseInt(prompt('Type in your x value:')) - 1;
        if (0 > x || x > 9 || isNaN(x)) {
            alert('Wrong input!');
            return;
        }
        var y = $scope.alphabet.indexOf(prompt('Type in your y value:').toUpperCase());
        if (0 > y || y > 9 || isNaN(y)) {
            alert('Wrong input!');
            return;
        }
        var orientation = true;
        if (x + parseInt(ship) - 1 > 9) {
            orientation = false;
        }
        if (!orientation && y + parseInt(ship) - 1 > 9) {
            alert("Ship can't be placed horizontal or vertical");
            return;
        }

        $scope.ships[ship] = {
            'x': x,
            'y': y,
            'orientation': orientation,
            'isPlaced': true
        };
        $scope.toggleShipOnField(x, y, parseInt(ship), 's');
    };

    $scope.toggleShipOnField = function (x, y, length, value) {
        for (var i = 0; i < length; i++) {
            if ($scope.ships[length.toString()]['orientation']) {
                $scope.field[x + i][y] = value;
            } else {
                $scope.field[x][y + i] = value;
            }
        }
    };

    var BreakException = {};

    $scope.submitShips = function () {
        try {
            angular.forEach($scope.ships, function (value, key) {
                if (!value.isPlaced) {
                    alert("You're not ready yet! Please reset ship with length " + key);
                    throw BreakException;
                }
            });
        } catch (e) {
            if (e !== BreakException) {
                throw e;
            }
            // function is properly exited
            return;
        }
        $scope.duplicate = angular.copy($scope.ships);
        $scope.sendShip();
    };

    $scope.sendShip = function () {
        console.log("Sending ship with length = %o", $scope.sendNext);
        $socket.send($scope.duplicate['' + $scope.sendNext]['x'] + ' ' +
            $scope.duplicate['' + $scope.sendNext]['y'] + ' ' +
            $scope.duplicate['' + $scope.sendNext]['orientation']);
        console.log("sent this: %o", $scope.duplicate['' + $scope.sendNext]['x'] + ' ' +
            $scope.duplicate['' + $scope.sendNext]['y'] + ' ' +
            $scope.duplicate['' + $scope.sendNext]['orientation']);
        $scope.sendNext++;
    };

    $scope.fillField = function (field, arr, val) {
        for (var y = 0; y < Object.keys(arr).length; y++) {
            for (var x = 0; x < arr[y].length; x++) {
                field[arr[y][x]][y] = val;
            }
        }
    };

    $scope.fillHitMap = function (field, shootMap, hitMap) {
        for (var x = 0; x < shootMap.length; x++) {
            for (var y = 0; y < shootMap[x].length; y++) {
                if (shootMap[x][y] && typeof hitMap === 'number') {
                    field[x][y] = field[x][y] === 's' ? 'h' : field[x][y] === 'h' ? 'h' : 'm';
                } else if (shootMap[x][y]) {
                    field[x][y] = hitMap[x][y] ? 'h' : 'm';
                }
            }
        }
    };

    $scope.shoot = function (x, y) {
        if (!!$scope.end) {
            return;
        }
        if ($scope.placing) {
            alert("You're not allowed to shoot yet!");
            return;
        }
        if ($scope.opponent[x][y] == 'x') {
            $socket.send(x + ' ' + y);
        } else {
            alert('You shot on this field already!');
        }
    };

}]);
