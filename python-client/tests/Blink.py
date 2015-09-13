from opendevice import *
import time

led = Device(1, Device.DIGITAL)

def onConnect():
    print("onConnect ----------")
    while True:
        led.on()
        time.sleep(1)
        led.off()
        time.sleep(1)

def onDeviceChange(device):
    print("onDeviceChange client listener : " + str(device.id) + " - " + str(device.value))


ODev.onConnect(onConnect)
ODev.onChange(onDeviceChange)

ODev.connect(TCPConnection("localhost", 8182))



