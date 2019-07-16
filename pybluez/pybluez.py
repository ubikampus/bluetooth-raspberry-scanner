import json
import select
from os import environ

import paho.mqtt.client as mqtt
import bluetooth

MQTT_PUB_TOPIC = "ohtu/test2"


class MyDiscoverer(bluetooth.DeviceDiscoverer):
    def __init__(self, mqtt_client, raspberry_id):
        super().__init__()
        self.mqtt_client = mqtt_client
        self.raspberry_id = raspberry_id

    def pre_inquiry(self):
        self.done = False

    def device_discovered(self, address, device_class, rssi, name):
        message = {
            "beaconId": address,
            "raspId": self.raspberry_id,
            "rssi": rssi,
            "name": name.decode("utf8"),
        }

        self.mqtt_client.publish(MQTT_PUB_TOPIC, json.dumps(message))
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


def validate_environ(environ):
    for key in ("MQTT_URL", "RASPBERRY_ID"):
        if not environ.get(key):
            raise RuntimeError(
                'env var "{}" is missing, see installation.md'.format(key)
            )


def run():
    validate_environ(environ)
    client = mqtt.Client("P1")
    client.on_connect = on_connect
    client.on_log = on_log

    print("connecting: ", environ["MQTT_URL"])
    client.connect(environ["MQTT_URL"])
    print("connected!")

    while True:
        d = MyDiscoverer(client, environ["RASPBERRY_ID"])
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
