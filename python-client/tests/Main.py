import time
from connection.TCPConnection import TCPConnection

__author__ = 'ricardo'

def on_message_received(cmd):
    print("on_message_received" + str(cmd.ctype) + " , " + str(cmd.deviceID)+ " , " + str(cmd.value))

conn = TCPConnection("localhost", 8182)

conn.add_listener(on_message_received)

conn.connect()

while conn.is_connected():
    time.sleep(1);

# conn.send("/1/1/1/1")


