__author__ = "Sergey Karakovskiy, sergey at idsia fullstop ch"
__date__ = "$Apr 30, 2009 1:53:54 PM$"

import numpy
    
from bitsTest import powsof2
    
def show(el):
#    powsof2 = (1, 2, 4, 8, 16, 32, 64, 128)
    print "block (", el, ") :",
    for  i in range(16):
        print ((int(el) & powsof2[i])),
    print


def decode(estate):
    """
    decodes the encoded state estate, which is a string of 61 chars
    """
#    powsof2 = (1, 2, 4, 8, 16, 32, 64, 128)
    dstate = numpy.empty(shape = (22, 22), dtype = numpy.int)
    for i in range(22):
        for j in range(22):
            dstate[i, j] = 2
    row = 0
    col = 0
    totalBitsDecoded = 0
    reqSize = 31
    assert len(estate) == reqSize, "Error in data size given %d! Required: %d \n data: %s " % (len(estate), reqSize, estate)
    check_sum = 0
    for i in range(len(estate)):
        cur_char = estate[i]
        if (ord(cur_char) != 0):
#            show(ord(cur_char))
            check_sum += ord(cur_char)
        for j in range(16):
            totalBitsDecoded += 1
            if (col > 21):
                row += 1
                col = 0
            if ((int(powsof2[j]) & int(ord(cur_char))) != 0):
#                show((int(ord(cur_char))))
                dstate[row, col] = 1
            else:
                dstate[row, col] = 0
            col += 1
            if (totalBitsDecoded == 484):
                break
    print "totalBitsDecoded = ", totalBitsDecoded
    return dstate, check_sum;


def extractObservation(data):
    """
     parse the array of strings and return array 22 by 22 of doubles
    """

    obsLength = 487
    levelScene = numpy.empty(shape = (22, 22), dtype = numpy.int)
    enemiesFloats = []
    dummy = 0
    if(data[0] == 'E'): #Encoded observation, fastTCP mode, have to be decoded
#        assert len(data) == eobsLength
        mayMarioJump = (data[1] == '1')
        isMarioOnGround = (data[2] == '1')
        levelScene, check_sum_got = decode(data[3:34])
        check_sum_recv = int(data[34:])
#        assert check_sum_got == check_sum_recv, "Error check_sum! got %d != etalon %d" % (check_sum_got, check_sum_recv)
        if check_sum_got != check_sum_recv:
            print "Error check_sum! got %d != recv %d" % (check_sum_got, check_sum_recv)
#        for i in range(22):
#            for j in range(22):
#               if levelScene[i, j] != 0:
#                   print '1',
#               else:
#                   print ' ',
#            print 
        
#        enemies = decode(data[0][64:])
        return (mayMarioJump, isMarioOnGround, levelScene)
    data = data.split(' ')
    if (data[0] == 'FIT'):
        status = int(data[1])
        distance = float(data[2])
        timeLeft = int(data[3])
        marioMode = int(data[4])
        coins = int(data[5])
#        print "S: %s, F: %s " % (data[1], data[2])
        #print "status %s, dist %s, timeleft %s, mmode %s, coins %s" % (status, distance, timeLeft, marioMode, coins) 
        return status, distance, timeLeft, marioMode, coins
    elif(data[0] == 'O'):
        mayMarioJump = (data[1] == 'true')
        isMarioOnGround = (data[2] == 'true')
#        assert len(data) == obsLength, "Error in data size given %d! Required: %d \n data: %s " % (len(data), obsLength, data)
        k = 0
        for i in range(22):
            for j in range(22):
                levelScene[i, j] = int(data[k + 3])
                k += 1
        k += 3
        marioFloats = (float(data[k]), float(data[k + 1]))
        k += 2        
        while k < len(data):
            enemiesFloats.append(float(data[k]))
            k += 1
         
#        for i in range(22):
#            for j in range(22):
#               if levelScene[i, j] != 0:
#                   print 1,
#               else:
#                   print ' ',
#            print 
           
        return (mayMarioJump, isMarioOnGround, marioFloats, enemiesFloats, levelScene, dummy)
    else:
        raise "Wrong format or corrupted observation..."
