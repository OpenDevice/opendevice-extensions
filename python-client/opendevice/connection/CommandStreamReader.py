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

# @reference br.com.criativasoft.opendevice.core.command.CommandStreamReader
from opendevice.Command import Command
from opendevice.Constants import ConnectionStatus, CommandType


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
        while self.parent.status == ConnectionStatus.CONNECTED:
            self.check_data_available()

    def check_data_available(self):

        chunk = self.sock.recv(9)

        if not chunk:
            return

        for c in chunk:
            c = chr(c)  # python 3 compatible

            # NOTE: Start bit is equals to the SEPARATOR
            if c == Command.START_FLAG and not self.processing:
                self.processing = True
                continue
            elif c == Command.ACK_FLAG:
                msg = self.parse("".join(self.buffer))
                if not (msg is None):
                    self.parent.notify_listeners(msg)

                self.buffer = []
                self.processing = False

            else:
                self.buffer.append(c)

    @staticmethod
    def parse(_buffer):
        split = _buffer.split(Command.DELIMITER)
        ctype = int(split[0]);
        cmd = Command(ctype)  # type

        if CommandType.is_device_command(ctype):
            cmd.deviceID = int(split[2])
            cmd.value = int(split[3])
        elif CommandType.is_simple_command(ctype):
            cmd.value = int(split[3])

        return cmd
