from opendevice import *

class Device(object):
    DIGITAL = 1
    ANALOG = 2

    def __init__(self, id, type=DIGITAL):
        self.id = id
        self.type = type
        self._value = 0
        ODev.addDevice(self)

    def setValue(self, value):
        if value != self._value:
            self._value = value
            ODev._notifyDeviceChange(self)

    def getValue(self):
        return self._value

    value = property(getValue, setValue)

    def on(self):
        self.value = 1

    def off(self):
        self.value = 0