__author__ = "Sergey Karakovskiy, sergey at idsia fullstop ch"
__date__ = "$May 13, 2009 1:25:30 AM$"

from client import Client
from environment import Environment
from utils.dataadaptor import show

class TCPEnvironment(Environment):

    verbose = False
    def __init__(self, agentName = "UnnamedClient", host = 'localhost', port = 4242, **otherargs):
        """General TCP Environment"""
        self.host = host
        self.port = port
        if self.verbose:
            print "TCPENV: agentName ", agentName
        self.client = Client(host, port, agentName)
        self.connected = True

    def isAvailable(self):
        """returns the availability status of the environment"""
        return self.connected

    def to_unicode_or_bust(self, obj, encoding = 'utf-8'):
        if isinstance(obj, basestring):
            if not isinstance(obj, unicode):
                obj = unicode(obj, encoding)
        return obj

    def getSensors(self):
        """ receives an observation via tcp connection"""
        #        print "Looking forward to receive data"
        
        data = self.client.recvData()
        data = self.to_unicode_or_bust(data)
                
        if data == "ciao":
            self.client.disconnect()
            self.connected = False            
        elif len(data) > 5:
        #        print data
#            for i in range(31):
#                cur_char = ord(data[i + 3])
#                if (cur_char != 0):
#                    print i + 3,
#                    show(cur_char)
            return data

    def performAction(self, action):
        """takes a numpy array of ints and sends as a string to server"""
        actionStr = ""
        for i in range(5):
            if action[i] == 1:
                actionStr += '1'
            elif action[i] == 0:
                actionStr += '0'
            else:
                raise "something very dangerous happen...."
        actionStr += "\r\n"
        self.client.sendData(actionStr)
