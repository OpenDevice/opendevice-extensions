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
        self.status = ConnectionStatus.DISCONNECTED
        self.manager = None

    def _start_reading(self, conn):
        self.conn = conn
        # Start reading thread.
        CommandStreamReader(self, conn).start()

    def send(self, message):
        self.conn.sendall(message + "\r\n");

    #
    # 	Notify All Listeners about received command.
    # 	 
    def notify_listeners(self, message):
        print("notify_listeners : " + str(message.ctype) + " , " + str(message.deviceID)+ " , " + str(message.value))
        if not self.listeners:
            logging.error("No listener was registered ! use: addListener")
        for listener in self.listeners:
            if hasattr(listener, 'on_message_received'):
                listener.on_message_received(message)
            else:
                listener(message)

    # =======================================================
    #  Set's / Get's
    #  =======================================================
    def setStatus(self, status):
        """ generated source for method setStatus """
        self.status = status
        if self.listeners.isEmpty():
            self.log.warn("No listener was registered ! use: addListener")
        for listener in self.listeners:
            listener.connectionStateChanged(self, status)

    def is_connected(self):
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