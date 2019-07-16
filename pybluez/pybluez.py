import json
import select

import paho.mqtt.client as mqtt
import bluetooth

MQTT_URL = "iot.ubikampus.net"
MQTT_PUB_TOPIC = "ohtu/test2"


class MyDiscoverer(bluetooth.DeviceDiscoverer):
    def __init__(self, mqtt_client):
        super().__init__()
        self.mqtt_client = mqtt_client

    def pre_inquiry(self):
        self.done = False

    def device_discovered(self, address, device_class, rssi, name):
        message = {
            "beaconId": address,
            "raspId": 4,
            "rssi": rssi,
            "name": name.decode("utf8"),
        }

        self.mqtt_client.publish("ohtu/test2", json.dumps(message))
        print("message published to broker: ", message)

    def inquiry_complete(self):
        self.done = True


def on_log(client, userdata, level, buf):
    print("log: ", buf)


def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("connected to mqtt")
    else:
        raise RuntimeError("failed to connect, return code {}".format(rc))


def run():
    client = mqtt.Client("P1")
    client.on_connect = on_connect
    client.on_log = on_log

    print("connecting: ", MQTT_URL)
    client.connect(MQTT_URL)
    print("connected!")

    while True:
        d = MyDiscoverer(client)
        d.find_devices(lookup_names=True)

        readfiles = [d, ]

        while True:
            rfds = select.select(readfiles, [], [])[0]

            if d in rfds:
                d.process_event()

            if d.done:
                break


if __name__ == "__main__":
    run()
