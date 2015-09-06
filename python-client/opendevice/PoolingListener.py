class PoolingListener(object):

    def __init__(self):
        self.list = []

    def on_message_received(self, cmd):
        self.list.append(cmd)

    def poll(self):
        if len(self.list) == 0:
            return None
        return self.list.pop()
