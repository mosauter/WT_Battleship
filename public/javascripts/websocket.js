"use strict";

var socketAddress = "ws://localhost:9000/socket";
var socket;

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


$(function() {
    socket = new WebSocket(socketAddress);

    socket.onopen = function () {
        console.log("Opened socket %o");
    };
    /**
     * message = {
     *      data: "THE REAL MESSAGE"
     * }
     */
    socket.onmessage = function (message) {
        let msg = JSON.parse(message.data);
        console.log("Message %o received", msg);
        switch (msg) {
            case messageType.HIT:
                break;
            case messageType.WRONGINPUT:
            case messageType.PLACEERR:
                break;
            case messageType.PLACE1:
                break;
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
                break;
            default:
                break;
        }
    };
    socket.onerror = function () {
        console.log("An error occured on socket %o");
    };
    socket.onclose = function () {
        console.log("Closed socket %o");
    };
});
