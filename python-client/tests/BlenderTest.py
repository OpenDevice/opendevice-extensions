import time

# -- blender/fakes ---
class Foo(object):
    pass

bge = Foo()
bge.logic = Foo()
obj = {}
# -- blender/fakes ---


from opendevice import *
from opendevice.util import MathUtils

move_interpolation = MathUtils.maprange_f(0, 360, 0, 1)

def main():
    # Will only run once, or when the var gets removed
    if 'init' not in obj:
        print("INIT")
        obj['init'] = True
        server = TCPConnection("localhost", 8182)
        server.add_listener(PoolingListener())
        server.connect()
        bge.logic._server = server
    else:
        poolingListener = bge.logic._server.listeners[0] # PoolingListener
        cmd = poolingListener.poll()
        while not (cmd is None):
            print("received loop: " + str(move_interpolation(cmd.value)))
            cmd = poolingListener.poll()


while 1:
    main()
    time.sleep(0.001)