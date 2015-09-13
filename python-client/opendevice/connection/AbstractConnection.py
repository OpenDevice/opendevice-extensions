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

import logging
from abc import ABCMeta, abstractmethod
from opendevice.Constants import ConnectionStatus, CommandType
from .CommandStreamReader import CommandStreamReader
from opendevice.PoolingListener import PoolingListener


class AbstractConnection(object):
    __metaclass__ = ABCMeta

    def __init__(self):
        self.conn = None
        self.listeners = []
        self._status = ConnectionStatus.DISCONNECTED
        self.manager = None
        self.reader = None

    def _start_reading(self, conn):
        self.conn = conn
        # Start reading thread.
        self.reader = CommandStreamReader(self, conn)
        self.reader.start()

    def send(self, cmd):
        data = CommandStreamReader.serialize(cmd)
        self.conn.send(data)

    #
    # 	Notify All Listeners about received command.
    # 	 
    def notifyListeners(self, message):
        self.manager._onMessageReceived(message)

    # =======================================================
    #  Set's / Get's
    #  =======================================================

    def setStatus(self, value):
        print("AbstractConnection.setStatus >>>>>>>> " + str(value))
        self._status = value

    def getStatus(self):
        return self._status

    status = property(getStatus, setStatus)

    def isConnected(self):
        return self.status == ConnectionStatus.CONNECTED

    def add_listener(self, e):
        if not self.listeners.__contains__(e):
            self.listeners.append(e)
            return True
        return False

    def remove_listener(self, e):
        return self.listeners.remove(e)

    @abstractmethod
    def connect(self):
        pass

    @abstractmethod
    def disconnect(self):
        pass
