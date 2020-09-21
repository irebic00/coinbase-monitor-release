var socket = null;
var previousPrice = -1;
var subscriptionId;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    var span = document.getElementById('connection-status');

    if (connected) {
        $("#ticker-table").show();
        span.innerText = span.textContent = "Connected";
        span.classList.remove("badge-danger");
        span.classList.add("badge-success");
    }
    else {
        $("#ticker-table").hide();
        span.innerText = span.textContent = "Disconnected";
        span.classList.remove("badge-success");
        span.classList.add("badge-danger");
    }
}

function configureServer() {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "https://" + location.host + "/subscription", true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && (xhr.status == 201 || xhr.status == 208)) {
            subscriptionId = JSON.parse(xhr.responseText).uuid;
        }
    };
    xhr.send(JSON.stringify({
        type: "subscribe",
        product_ids: [updateTicker()],
        channels: ["ticker"],
    }));
}

function connect() {

    configureServer()

    socket = new WebSocket("wss://" + location.host + "/websocket/coinbase");

    socket.onopen = function(e) {
        setConnected(true);
        console.log("[open] Connection established");
    };

    socket.onmessage = function(event) {
        console.log("[message] Data received from server: " + event.data);
        showUpdate(event.data)
    };

    socket.onclose = function(event) {
        if (event.wasClean) {
            console.log("[close] Connection closed cleanly, code=" + event.code + " reason=" + event.reason);
        } else {
            // e.g. server process killed or network down
            // event.code is usually 1006 in this case
            console.log("[close] Connection died");
        }
        setConnected(false);
    };

    socket.onerror = function(error) {
      console.log("[error] " + error.message);
      setConnected(false);
    };
}

function disconnect() {
    if (socket !== null) {
        socket.close(1000, "Work complete");
    }
    var xhr = new XMLHttpRequest();
    xhr.open('DELETE', "https://" + location.host + "/subscription/" + subscriptionId, true);
    xhr.send();
    setConnected(false);
    console.log("Disconnected");
}

function showUpdate(jsonMessage) {
    var data = JSON.parse(jsonMessage);
    var timestamp = new Date(data.time);
    var tickerId = updateTicker();
    var row = document.getElementById(tickerId);

    if (previousPrice > data.price) {
        row.classList.remove(...row.classList);
        row.classList.add("table-danger");
    } else if (previousPrice < data.price){
        row.classList.remove(...row.classList);
        row.classList.add("table-success");
    }
    previousPrice = data.price;
    row.cells[0].innerHTML = data.price;
    row.cells[1].innerHTML = timestamp;
}

function updateInterval() {
    var refreshInterval = $( "#refresh-interval-select" )[0].value;
    var span = document.getElementById('refresh-interval');

    span.innerText = span.textContent = refreshInterval + "s";
    console.log("New refresh interval: " + refreshInterval);
}

function updateTicker() {
    var selectedTicker = $( "#ticker-select" )[0].value;
    var span = document.getElementById('ticker-selected');

    span.innerText = span.textContent = selectedTicker;
    console.log("Selected ticker: " + selectedTicker);

    var table = document.getElementById("ticker-table").getElementsByTagName('tbody')[0];

    var row;
    if (table.rows.length == 0){
        row = table.insertRow(0);
        row.insertCell(0);
        row.insertCell(1);
        row.classList.add("table-active");
    } else {
        row = table.rows[0];
    }
    row.id = selectedTicker;
    return selectedTicker;
}

$(function () {
    updateInterval();
    updateTicker();
    setConnected(false);

    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#refresh-interval-select" ).on('change',function(){ updateInterval(); });
    $( "#ticker-select" ).on('change',function(){ updateTicker(); });
});
