const BeaconScanner = require("node-beacon-scanner")
const UbiMqtt = require("ubimqtt");
const Beacon = require('./beacon');

require('dotenv').config();

let mqtt = new UbiMqtt("mqtt://" + process.env.mqttUrl);

let scanner = new BeaconScanner();
let beacons = new Map();

function publish(topic, message) {
    mqtt.publish(topic, message, null, function(err){});
}

scanner.onadvertisement = (ad) => {
    if (!ad.iBeacon) {
	    return;
    }

    let formatted = { beaconId: ad.iBeacon.uuid, raspId: process.env.raspId, volume: ad.rssi }; 

    const beaconId = ad.iBeacon.uuid;
    const rssi = ad.rssi;
    let beacon;
    if (beacons.has(beaconId)) {
        beacon = beacons.get(beaconId);
    } else {
        beacon = new Beacon(beaconId);
        beacons.set(beaconId, beacon);
    }

    beacon.addObservation(rssi);
    publish("ohtu/test", JSON.stringify(formatted));
    console.log("average", beacon.average());
}

mqtt.connect(function(error) {
    scanner.startScan().then(() => {
        console.log("Scanning...");
    }).catch(error => {
        console.error(error);
    })
    
    publish("ohtu/test", "hello there");
});





