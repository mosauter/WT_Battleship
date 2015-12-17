$(function(){

    // get websocket class, firefox has a different way to get it
    var WS = window['MozWebSocket'] ? window['MozWebSocket'] : WebSocket;

    // open pewpew with websocket
    var socket = new WS('@routes.Application.wsInterface().webSocketURL(request)');

    var writeMessages = function(event){
        var dt = new Date();
        //var time = dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds() + " UHR";
        var time = dt.getHours() + ":" + dt.getMinutes() + " Uhr";
        //$('#socket-messages').prepend('<p>'+new Date($.now())+'    '+event.data+'</p>');
        $('#socket-messages').prepend('<p>'+'<span style="color:#5cdce0;font-size: 10pt;">'+time+'</span>'+'&nbsp;&nbsp;'+'<span style="font-size: 12.4pt">'+event.data+'</span>'+'</p>');
    }

    socket.onmessage = writeMessages;

    $('#socket-input').keyup(function(event){
        var charCode = (event.which) ? event.which : event.keyCode ;

        // if enter (charcode 13) is pushed, send message, then clear input field
        if(charCode === 13){
            socket.send($(this).val());
            $(this).val('');
        }
    });
});