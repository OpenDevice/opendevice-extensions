#!/usr/bin/env python
class ConnectionStatus:
    CONNECTING = u'CONNECTING'
    CONNECTED = u'CONNECTED'
    DISCONNECTING = u'DISCONNECTING'
    DISCONNECTED = u'DISCONNECTED'
    LOGGINGIN = u'LOGGINGIN'
    FAIL = u'FAIL'
    UNDEFINED = u'UNDEFINED'

class CommandType:
    # Indicates that the values are 0 or 1 (HIGH or LOW)
    DIGITAL = 1
    ANALOG = 2
    ANALOG_REPORT = 3
    # Commands sent directly to the pins (digitalWrite) */
    GPIO_DIGITAL = 4
    # Commands sent directly to the pins (analogWrite) */
    GPIO_ANALOG = 5
    PWM = 6
    INFRA_RED = 7

    # Response to commands like: DIGITAL, POWER_LEVEL, INFRA RED */
    DEVICE_COMMAND_RESPONSE = 10

    PING = 20
    PING_RESPONSE = 21
    # Report the amount of memory (displays the current and maximum).
    MEMORY_REPORT = 22
    CPU_TEMPERATURE_REPORT = 23
    CPU_USAGE_REPORT = 24

    GET_DEVICES = 30
    GET_DEVICES_RESPONSE = 31
    USER_COMMAND = 99

    @classmethod
    def is_device_command(cls, _type):
        if _type is None:
            return False
        if _type in [CommandType.DIGITAL, CommandType.ANALOG, CommandType.ANALOG_REPORT]:
            return True
        return False

    @classmethod
    def is_simple_command(cls, _type):
        if _type is None:
            return False
        if _type in [CommandType.PING, CommandType.PING_RESPONSE]:
            return True
        return False
