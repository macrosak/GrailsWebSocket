<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Welcome to Grails</title>
    <script type="text/javascript">


        var TadoServer = {};

        TadoServer.socket = null;

        TadoServer.connect = (function(host) {
            if ('WebSocket' in window) {
                TadoServer.socket = new WebSocket(host);
            } else if ('MozWebSocket' in window) {
                TadoServer.socket = new MozWebSocket(host);
            } else {
                Console.log('Error: WebSocket is not supported by this browser.');
                return;
            }

            TadoServer.socket.onopen = function () {
                Console.log('Info: WebSocket connection opened.');
                document.getElementById('command').onkeydown = function(event) {
                    if (event.keyCode == 13) {
                        TadoServer.sendMessage();
                    }
                };
            };

            TadoServer.socket.onclose = function () {
                document.getElementById('command').onkeydown = null;
                Console.log('Info: WebSocket closed.');
                TadoServer.initialize();
            };

            TadoServer.socket.onmessage = function (message) {
                Console.log(message.data);
            };
        });

        TadoServer.initialize = function() {
            if (window.location.protocol == 'http:') {
                TadoServer.connect('ws://localhost:8080/GrailsWebSocket/command');
            } else {
                TadoServer.connect('wss://localhost:8080/GrailsWebSocket/command');
            }
        };

        TadoServer.sendMessage = (function() {
            var message = document.getElementById('command').value;
            if (message != '') {
                TadoServer.socket.send(message);
                document.getElementById('command').value = '';
            }
        });

        var Console = {};

        Console.log = (function(message) {
            var console = document.getElementById('console');
            var p = document.createElement('p');
            p.style.wordWrap = 'break-word';
            p.innerHTML = message;
            console.appendChild(p);
            while (console.childNodes.length > 25) {
                console.removeChild(console.firstChild);
            }
            console.scrollTop = console.scrollHeight;
        });

        TadoServer.initialize();

    </script>
</head>
<body>
<div>
    <p>
        <input type="text" placeholder="type command and press enter" id="command">
    </p>
    <div id="console-container">
        <div id="console"></div>
    </div>
</div>
</body>
</html>
