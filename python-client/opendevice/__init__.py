__author__ = 'Ricardo JL Rufino'
import logging
from .connection.TCPConnection import TCPConnection
from .PoolingListener import PoolingListener

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)