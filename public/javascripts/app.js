/**
 * Created by fw on 09.12.15.
 */

var app = angular.module('battleship', ['ngRoute', 'ngWebSocket']);

app.config(function($routeProvider){
    $routeProvider
        .when('/', {
            templateUrl: 'assets/partials/index.html',
            controller: 'HomeCtrl'
        })
        .when('/lobby', {
            templateUrl: 'assets/partials/lobby.html',
            controller: 'LobbyCtrl'
        })
        .when('/battle', {
            templateUrl: 'assets/partials/battle.html',
            controller: 'BattleCtrl'
        })
        .otherwise({
            redirectTo: '/'
        });
});

app.filter('range', function() {
    return function(input, total) {
        total = parseInt(total);
        for (var i=0; i<total; i++) {
          input.push(i);
        }
        return input;
    };
});

app.controller('HomeCtrl', ['$scope', '$location', function($scope, $location){
    //TODO: Google Login magic goes here, maybe!

    $scope.lobby = function(){
        $location.path("/lobby");
    };

    $scope.fight = function(){
        $location.path('/battle')
    };
}]);

app.controller('LobbyCtrl', ['$scope', function($scope){
    $scope.user = {};
}]);

app.controller('BattleCtrl', ['$scope', '$websocket', '$location', function($scope, $websocket, $location){
    var messageType = {
        // HIT and MISS
        HIT: "HIT", // HIT and MISS are the same message
        // INVALID
        WRONGINPUT: "WRONGINPUT",
        PLACEERR: "PLACEERR",
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

    var $socket = $websocket('ws://localhost:9000/socket');

    $socket.onOpen(function(){
        console.log('Connection established!');
    });

    $socket.onClose(function(){
        console.log('Connection closed!');
        $location.path('/');
    });

    $socket.onError(function(){
        console.log('An error occured on socket.');
    });

    $socket.onMessage(function(message){
        var msg = JSON.parse(message.data);
        console.log("Message %o received", msg);
        switch (msg.type) {
            case messageType.HIT:
                break;
            case messageType.WRONGINPUT:
            case messageType.PLACEERR:
                alert('Something went wrong, please try again!');
                break;
            case messageType.PLACE1:
            case messageType.PLACE2:
                break;
            case messageType.FINALPLACE1:
                break;
            case messageType.FINALPLACE2:
                break;
            case messageType.SHOOT1:
                break;
            case messageType.SHOOT2:
                break;
            case messageType.WIN1:
            case messageType.WIN2:
                if(msg.winner){
                    alert(msg.winner + ' has won the game!');
                }
                $location.path('/');
                break;
            default:
                break;
        }
    });

    $scope.alphabet = ['A','B','C','D','E','F','G','H','I','J'];

    $scope.initFields = function() {
        var arr = [];
        for (var i = 0; i < 10; i++){
            arr[i] = ['x','x','x','x','x','x','x','x','x','x','x'];
        }
        return arr;
    };

    $scope.field = $scope.initFields();
    $scope.opponent = $scope.initFields();

    // 'x': 0, 'y': 0, 'orientation': true
    $scope.ships = {
        '2': {'isPlaced': false},
        '3': {'isPlaced': false},
        '4': {'isPlaced': false},
        '5': {'isPlaced': false},
        '6': {'isPlaced': false}
    };

    $scope.placing = true;
    $scope.switchOrientation = function(ship){
        $scope.toggleShipOnField($scope.ships[ship]['x'], $scope.ships[ship]['y'], parseInt(ship), 'x');
        $scope.ships[ship]['orientation'] = $scope.ships[ship]['orientation'] ? false : true;
        $scope.toggleShipOnField($scope.ships[ship]['x'], $scope.ships[ship]['y'], parseInt(ship), 's');
    };
    $scope.removeShip = function(ship){
        $scope.toggleShipOnField($scope.ships[ship]['x'], $scope.ships[ship]['y'], parseInt(ship), 'x');
        $scope.ships[ship] = {'isPlaced': false};
    };
    $scope.placeShip = function(ship){
        var x = parseInt(prompt('Type in your x value:')) - 1;
        if(0 > x || x > 9){
            alert('Wrong input!');
            return;
        }
        var y = $scope.alphabet.indexOf(prompt('Type in your y value:').toUpperCase());
        if(0 > y || y > 9){
            alert('Wrong input!');
            return;
        }

        $scope.ships[ship] = {'x': x, 'y': y, 'orientation': true, 'isPlaced': true};
        $scope.toggleShipOnField(x, y, parseInt(ship), 's');
    };

    $scope.toggleShipOnField = function(x, y, length, value){
        for(var i = 0; i < length; i++){
            if($scope.ships[length.toString()]['orientation']){
                $scope.field[x + i][y] = value;
            } else {
                $scope.field[x][y + i] = value;
            }
        }
    };

    $scope.sendShips = function(){
        angular.forEach($scope.ships, function(value, key){
            if(!value.isPlaced){
                alert("You're not ready yet! Please reset ship with length " + key);
                return;
            }
        });
        angular.forEach($scope.ships, function(value, key){
            $socket.send(JSON.stringify({
                'type': 'PLACE',
                'x': value['x'],
                'y': value['y'],
                'orientation': value['orientation']
            }));
        });
    };

    $scope.fillField = function(field, arr, value){
        angular.forEach(arr, function(x, y){
            angular.forEach(x, function(v, k){
                field[x][k] = value;
            });
        });
    };

    /*
     * always getting no such element if id="card{{x}}{{i}}" in battle.html
     * so we can't trigger an animation to flip the card via js
     *
     * probably has to be moved to socket on message event
     */
    $scope.shoot = function(x, y){
        if ($scope.field[x][y] == 'x'){
            $socket.send(JSON.stringify({
                'type': 'SHOOT',
                'x': x,
                'y': y
            }));
            //angular.element('#card' + y + x).removeClass('water').addClass('hit');
        } else {
            alert('You shot on this field already');
        }
    };

}]);