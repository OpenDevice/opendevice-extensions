# Tests the performance of the connection between Java and Python (OpenDevice)
# Use: opendevice-3d-blender/src/main/java/SlidersForm.java

import Tkinter as tk
from opendevice import *

class Application(tk.Frame):
    def __init__(self, master=None):
        tk.Frame.__init__(self, master)
        self.grid()
        self.createWidgets()

    def createWidgets(self):
        self.quitButton = tk.Button(self, text='Quit',
                                    command=self.quit)
        self.quitButton.grid()

        self.scale = tk.Scale(self, from_=360.0, to=0, length=300)
        self.scale.grid()

app = Application()

def _onDeviceChange(device):
    app.scale.set(float(device.value))

ODev.addDevice(Device(1, Device.DIGITAL))
ODev.addDevice(Device(10, Device.DIGITAL))
ODev.onChange(_onDeviceChange)
ODev.connect(TCPConnection("localhost", 8182))
app.master.title('Python')
app.mainloop()


