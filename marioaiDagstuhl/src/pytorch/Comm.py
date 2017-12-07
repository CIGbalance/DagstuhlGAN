import sys
import time
if __name__=="__main__":
    fo = open("ganreadin.txt","w")
    fo.write('GAN starts at: %.0f\n'%(time.time()*1000))
    line = sys.stdin.readline()
    while line != "END":
        fo.write(line)
        # sys.stdout.write("GAN sent " + line)
        print ("GAN received " + line)
        line = sys.stdin.readline()
    fo.write('GAN ends at: %.0f\n'%(time.time()*1000))
    fo.close()