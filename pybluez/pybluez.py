import bluetooth
import select
import paho.mqtt.client as mqtt
import time
import json

class MyDiscoverer(bluetooth.DeviceDiscoverer):

    def pre_inquiry(self):
        self.done = False

    def device_discovered(self, address, device_class, rssi, name):
        id = name
        volume = rssi
        message = "name: {}, rssi: {}".format(id,volume)
        client.publish("ohtu/test",message)
       # print("%s - %s - %s" % (address, name, str(rssi)))
        print("message published to broker: ",message)

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
client = mqtt.Client("P1") #create new instance
#client.on_connect = on_connect
#client.on_log = on_log
#client.on_message = on_message


print("connecting: ",broker)
client.connect(broker) #connect to broker

client.loop_start()
time.sleep(2)
client.loop_stop()

#client.disconnect()
#print("disconnected")

d = MyDiscoverer()
d.find_devices(lookup_names = True)

readfiles = [ d, ]

while True:
    rfds = select.select( readfiles, [], [] )[0]

    if d in rfds:
        d.process_event()

    if d.done: break


