'''
Created on Aug 10, 2009
'''
powsof2 = (1, 2, 4, 8, 16, 32, 64, 128,
                         256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072)

def show(el):
    print "block (", el, ") :",
    for  i in range(16):
        print ((int(el) & powsof2[i])),
    print

