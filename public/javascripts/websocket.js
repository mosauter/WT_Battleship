"use strict";

$(function() {
    var socket = new WebSocket("ws://localhost:9000/socket");

    socket.onopen = function () {
        alert("OPEN");
        socket.send("HELLO ME FRIEND...")
    };
    socket.onmessage = function (message) {
        alert("MESSAGE CAME: " + message);
        console.log(message);

        console.log(JSON.parse(message.data));
    };
    socket.onerror = function () {
        alert("ERROR")
    };
    socket.onclose = function () {
        alert("CLOSED")
    };
});
