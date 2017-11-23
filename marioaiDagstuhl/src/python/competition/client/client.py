import sys
import socket
__author__ = "Sergey Karakovskiy, sergey at idsia fullstop ch"
__date__ = "$May 1, 2009 9:02:32 AM$"

class Client:
    """
    Basic TCP Client. Can connect to arbitrary TCP Server
    """

    Host = 'localhost'
    Port = 4224
    bufSize = 4096
    sock = None


    def __init__(self, host, port, ownerName):
        """Documentation"""
        self.Host = host
        self.Port = port
        self.OwnerName = ownerName
        self.connectToDefaultServer()

    def __del__(self):
        self.sock.close()


    def connectToDefaultServer(self):
            """connect to a server using defind by constructor host and port"""
            self.connectToServer(self.Host, self.Port)

    def connectToServer(self, host, port):
        """connects to a server"""
        try:
            print "Client: Trying to connect to %s:%s" % (host, port)
            self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        except socket.error, msg:
            sys.stderr.write("[SOCKET CREATION ERROR1] %s\n" % msg[1])
            raise

#        self.sock.settimeout(5)
#        print "Timeout set to 5"
        try:
            self.sock.connect((host, port))
            print "Client: Connection to %s:%s succeeded!" % (host, port)
            print "Client: Looking forward to receive greeting message..."
            data = self.recvData()
            print "Client: Greeting received: %s" % data

        except socket.error, msg:
            sys.stderr.write("[CONNECTION ERROR] %s\n" % msg[1])
            raise

        GreatingMessage = "Client: Dear Server, hello! I am %s\r\n" % self.OwnerName
        self.sendData(GreatingMessage)
        



    def printConnectionData(self):
        """Print to standard output the current connection data"""
        print "Client: %s Connection Info: \r\nHost = %s, port = %d" % (self, self.Host, self.Port)

    def disconnet(self):
        """disconnects from the server"""
        print "Client is about to close the connection"
        self.disconnected = True
        self.sock.close()
        print "Connection closed"

    def recvData(self):
        """receive arbitrary data from server"""
        try:
            return self.sock.recv(self.bufSize)
        except  socket.error, msg:
            sys.stderr.write("[SOCKET PIPE ERROR WHILE RECEIVING] %s\n.Possible reason: socket closed due to time out and/or requested server is currently busy" % msg[1])
            raise


    def sendData(self, data):
        """send arbitrary string to server"""
        try:
            self.sock.send(data)
        except  socket.error, msg:
            sys.stderr.write("[SOCKET PIPE ERROR WHILE SENDING] %s\n" % msg[1])
            raise

