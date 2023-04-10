let ws;
var selectElement = document.getElementById("roomList");

function newRoom() {
    // calling the ChatServlet to retrieve a new room ID
    let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet";
    fetch(callURL, {
        method: 'GET',
        headers: {
            'Accept': 'text/plain',
        },
    })
        .then(response => response.text())
        .then(code => {
            console.log(code);
            enterRoom(code); // enter the room with the code
            selectElement.add(new Option(code));
        });
}

function enterRoom(code) {
    ws = new WebSocket("ws://localhost:8080/WSChatServerDemo-1.0-SNAPSHOT/ws/" + code);
    ws.onopen = function (event)
    {
        document.getElementById("log").value += "[" + timestamp() + "]" + "Welcome! Please state your username:" + "\n";
    }

    ws.onmessage = function (event) {
        let data = JSON.parse(event.data);
        document.getElementById("log").value += "[" + timestamp() + "] " + data.message + "\n";
    };


    document.getElementById("messageInput").addEventListener("keyup", function (event) {
        if (event.keyCode === 13) {
            let request = {"type": "chat", "msg": event.target.value};
            ws.send(JSON.stringify(request));
            event.target.value = "";
        }
    });

    function timestamp() {
        var d = new Date(), minutes = d.getMinutes();
        if (minutes < 10) minutes = '0' + minutes;
        return d.getHours() + ':' + minutes;
    }
}

// Function to send a message to the chat room
function sendMessage(message) {
    if (ws) {
        let jsonmsg = {
            'type': 'message',
            'message': message
        };
        ws.send(JSON.stringify(jsonmsg));
    }
}

// Function to close the chat room
function closeRoom() {
    if (ws) {
        ws.close();
    }
}

function clearChat()
{
    document.getElementById("log").value = "";
}



