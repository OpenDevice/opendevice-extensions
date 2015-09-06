# 
#  * ******************************************************************************
#  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
#  *  All rights reserved. This program and the accompanying materials
#  *  are made available under the terms of the Eclipse Public License v1.0
#  *  which accompanies this distribution, and is available at
#  *  http://www.eclipse.org/legal/epl-v10.html
#  *
#  *  Contributors:
#  *  Ricardo JL Rufino - Initial API and Implementation
#  * *****************************************************************************
#  
# package: br.com.criativasoft.opendevice.core.command
# 
#  * @author Ricardo JL Rufino
#  * @date 04/09/2011 13:48:57
#
class Command(object):
    START_FLAG = '/'
    ACK_FLAG = '\r'
    DELIMITER_FLAG = '/'
    DELIMITER = "/"  # used to separate data strings
    MIN_LENGTH = 9

    uid = None

    #  id of connection/channel that requested the command
    applicationID = None

    #  id of client (for Multitenancy support)
    trackingID = 0

    #  To monitor execution of commands, is usually a sequential number and managed by CommandDelivery

    timestamp = None

    def __init__(self, ctype):
        self.ctype = ctype



