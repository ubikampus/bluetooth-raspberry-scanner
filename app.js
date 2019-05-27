const BeaconScanner = require("node-beacon-scanner")
const UbiMqtt = require("ubimqtt");

const netInfo = require('os').networkInterfaces();
console.log(netInfo);

let mqtt = new UbiMqtt("mqtt://localhost:1883");
let scanner = new BeaconScanner();
let observations = new Map();

function average(id, rssi) {
    if (!observations.get(id)) {
        observations.set(id, []);
    }

    observations.get(id).push(rssi);
    if (observations.get(id).length > 10) {
        observations.get(id).shift();
    }

    let l = observations.get(id);
    l.sort();
    try {
        console.log(l[l.length - 3] - l[2]);
    } catch (e) {

    }
    return observations.get(id).reduce((a, c) => a+c) / observations.get(id).length; 
}

function publish(topic, message) {
    mqtt.publish(topic, message, null, function(err){});
}

scanner.onadvertisement = (ad) => {
    if (!ad.iBeacon) {
	    return;
    }

    let formatted = { beaconId: ad.iBeacon.uuid, rssi: ad.rssi }; 
    //console.log(JSON.stringify(formatted, null, "   "));
    console.log("average", average(formatted.beaconId, formatted.rssi));
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





