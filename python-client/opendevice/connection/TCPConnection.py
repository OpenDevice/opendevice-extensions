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

import socket
import errno
import logging
from socket import error as socket_error

from .AbstractConnection import AbstractConnection
from opendevice.Constants import ConnectionStatus


class TCPConnection(AbstractConnection):

    def __init__(self, host, port):
        super(TCPConnection, self).__init__()
        self.host = host
        self.port = port
        self.sock = None

    def __del__(self):
        self.disconnect()

    def connect(self):
        logging.info("Connecting to: %s - port: %d", self.host, self.port)
        try:
            if self.sock is None:
                self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.sock.connect((self.host, self.port))
            self.status = ConnectionStatus.CONNECTED
            self._start_reading(self.sock)
            self.manager._notifyListeners("onConnect")
        except socket_error as e:
            self.status = ConnectionStatus.FAIL
            if e.errno == errno.ECONNREFUSED:
                logging.error("Connection refused, check host and port !")
            raise e

    def disconnect(self):
        super(TCPConnection, self).disconnect()
        if self.sock is not None:
            logging.debug("Disconnecting...")
            self.sock.close()
        self.sock = None
        self.status = ConnectionStatus.DISCONNECTED
