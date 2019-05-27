const Noble = require("noble")
const BeaconScanner = require("node-beacon-scanner")
const UbiMqtt = require("ubimqtt");

const netInfo = require('os').networkInterfaces();

let mqtt = new UbiMqtt("mqtt://localhost:1883");
let scanner = new BeaconScanner();
let observations = [];

function average(rssi) {
    observations.push(rssi);
    if (observations.length > 10) {
        observations.shift();
    }
    return observations.reduce((a, c) => a+c) / observations.length; 
}


scanner.onadvertisement = (ad) => {
    if (!ad.iBeacon) {
	return;
    }

    let formatted = { beaconId: ad.iBeacon.uuid, raspberryId: netInfo.wlan0[0].mac, rssi: ad.rssi }; 
    console.log(JSON.stringify(formatted, null, "   "));
    console.log("average", average(formatted.rssi));
    publish("ohtu/test", JSON.stringify(formatted));
}

mqtt.connect(function(error) {
    scanner.startScan().then(() => {
        console.log("Scanning...");
    }).catch(error => {
        console.error(error);
    })
    
    publish("ohtu/test", "hello there");
});

function publish(topic, message) {
    mqtt.publish(topic, message, null, function(err){});
}




