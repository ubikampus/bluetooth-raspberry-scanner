import bluetooth
import select
import paho.mqtt.client as mqtt
import time
import json

class MyDiscoverer(bluetooth.DeviceDiscoverer):

    def pre_inquiry(self):
        self.done = False

    def device_discovered(self, address, device_class, rssi, name):
        volume = rssi
        macAddr = address
        message = {
            'beaconId':macAddr,
            'raspId':4,
            'rssi':volume,
            'name': name.decode('utf8'),
            }

        with open('message.json', 'w') as f:
            json.dump(message, f)
        mess = open('message.json', 'r').read()
        client.publish("ohtu/test2",mess)
        print("message published to broker: ",mess)

    def inquiry_complete(self):
        self.done = True

    def on_log(client, userdata, level, buf):
        print("log: ",buf)

    def on_connect(client, userdata, flags, rc):
        if rc==0:
            print("connected!")
        else:
            print("Not connected: ", rc)

broker = "iot.ubikampus.net"
global client
client = mqtt.Client("P1")
print("connecting: ",broker)
client.connect(broker)
print("connected!")

#client.on_connect = on_connect
#client.on_log = on_log
#client.on_message = on_message
#client.loop_start()
#time.sleep(1)

while True:
    d = MyDiscoverer()
    d.find_devices(lookup_names = True)

    readfiles = [ d, ]

    while True:
        rfds = select.select( readfiles, [], [] )[0]

        if d in rfds:
            d.process_event()

        if d.done:
            break

# client.loop_stop()
