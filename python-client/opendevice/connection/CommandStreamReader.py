#
# ******************************************************************************
#  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
#  All rights reserved. This program and the accompanying materials
#  are made available under the terms of the Eclipse Public License v1.0
#  which accompanies this distribution, and is available at
#  http://www.eclipse.org/legal/epl-v10.html
#
#  Contributors:
#  Ricardo JL Rufino - Initial API and Implementation
# *****************************************************************************
#
# package: br.com.criativasoft.opendevice.connection
#
# Base class for Connections
# @author Ricardo JL Rufino
# @date 05/09/2015
#

import atexit
from threading import Thread
from opendevice.Command import Command
from opendevice.Constants import ConnectionStatus, CommandType

# @reference br.com.criativasoft.opendevice.core.command.CommandStreamReader
class CommandStreamReader(Thread):
    def __init__(self, parent, sock):
        super(CommandStreamReader, self).__init__()
        self.daemon = True
        self.parent = parent
        self.sock = sock
        self.processing = False
        self.buffer = []
        atexit.register(self.parent.disconnect)  # on program exit

    def run(self):
        while self.parent.isConnected():
            self.check_data_available()
        print(">>> END: CommandStreamReader Thread")

    def check_data_available(self):

        chunk = self.sock.recv(256)  # blocking mode

        if not chunk:
            return

        if type(chunk) == bytes:  # python 3 compatible
            chunk = chunk.decode("utf-8")

        try:
            for c in chunk:
                # NOTE: Start bit is equals to the SEPARATOR
                if c == Command.START_FLAG and not self.processing:
                    self.processing = True
                    continue
                elif c == Command.ACK_FLAG:
                    msg = self.parse("".join(self.buffer))
                    if not (msg is None):
                        self.parent.notifyListeners(msg)

                    self.buffer = []
                    self.processing = False

                else:
                    self.buffer.append(c)
        except TypeError:
            print("Unexpected error:")
            raise

    @staticmethod
    def parse(_buffer):
        split = _buffer.split(Command.DELIMITER)
        ctype = int(split[0])
        cmd = Command(ctype)  # type

        if CommandType.is_device_command(ctype):
            cmd.deviceID = int(split[2])
            cmd.value = int(split[3])
        elif CommandType.is_simple_command(ctype):
            cmd.value = int(split[3])

        return cmd

    @staticmethod
    def serialize(cmd):
        data = ""
        data += Command.START_FLAG
        data += str(cmd['type'])
        data += Command.DELIMITER_FLAG
        data += "0"  # UID (TODO not implemented)
        if CommandType.is_device_command(cmd['type']):
            data += Command.DELIMITER_FLAG
            data += str(cmd['deviceID'])
            data += Command.DELIMITER_FLAG
            data += str(cmd['value'])
        else:
            data += "0"
            data += Command.DELIMITER_FLAG
            data += "0"
        data += Command.ACK_FLAG

        return data.encode('utf-8')
