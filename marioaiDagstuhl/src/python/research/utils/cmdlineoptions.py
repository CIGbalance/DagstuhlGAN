import sys

import getopt
from agents.forwardagent import ForwardAgent
from agents.forwardrandomagent import ForwardRandomAgent
__author__="Sergey Karakovskiy, sergey at idsia fullstop ch"
__date__ ="$May 13, 2009 3:44:46 AM$"

class CmdLineOptions:
    """
    Class for convenient processing of command line options
    """

    agent = None
    host = "localhost"
    port = 4242

    def __init__(self, argv):
        """Constructor"""
        try:
            opts, _ = getopt.getopt(argv[1:], "pa", ["port=", "agent="])
        except getopt.GetoptError, err:
            print str(err)
            self.usage()
            sys.exit(2)
        agentName = "ForwardAgent" # by default.

        for o, a in opts:
            if o == "--port":
                self.port = int(a)
            elif o == "--agent":
                agentName = a
            else:
                self.usage()
                assert False, "unhandled option"
        if agentName == "ForwardAgent":
            self.agent = ForwardAgent()
        elif agentName == "ForwardRandomAgent":
            self.agent = ForwardRandomAgent()
        else:
            assert  False, "unknown Agent"

    def usage():
        print "python iPyMario.py [--port port][--agent AgentName]"

    def getHost(self):
        """returns default host 'localhost'"""
        return self.host

    def getPort(self):
        """return either processed or default port 4242"""
        return self.port
    def getAgent(self):
        return self.agent

