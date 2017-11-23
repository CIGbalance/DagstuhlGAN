package ch.idsia.mario.engine;

import ch.idsia.mario.engine.sprites.Sprite;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Aug 5, 2009
 * Time: 7:05:46 PM
 * Package: ch.idsia.mario.engine
 */
public class GeneralizerLevelScene implements Generalizer
{
    public byte ZLevelGeneralization(byte el, int ZLevel)
    {
        switch (ZLevel)
        {
            case(0):
                return el;
            case(1):
                switch(el)
                {
                    case(-108):
                    case(-107):
                    case(-106):
                    case(14): // Particle
                    case(15): // Sparcle, irrelevant
                        return 0;
                    case(-128):
                    case(-127):
                    case(-126):
                    case(-125):
                    case(-120):
                    case(-119):
                    case(-118):
                    case(-117):
                    case(-116):
                    case(-115):
                    case(-114):
                    case(-101):
                    case(-112):
                    case(-111):
                    case(-110):
                    case(-109):
                    case(-104):
                    case(-103):
                    case(-102):
                    case(-100):
                    case(-99):
                    case(-98):
                    case(-97):
                    case(-69):
                    case(-88):
                    case(-87):
                    case(-86):
                    case(-85):
                    case(-84):
                    case(-83):
                    case(-82):
                    case(-81):
                    case(4):
                    case(30): // canon
                        return -10;   // border, cannot pass through, can stand on
                    case(9):
                        return -12; // hard formation border. Pay attention!
                    case(-124):
                    case(-123):
                    case(-122):
                    case(-76):
                    case(-74):
                        return -11; // half-border, can jump through from bottom and can stand on
                    case(10): case(11): case(26): case(27):
                        return 20; // flower pot
                }
                return el;
            case(2):
                switch(el)
                {
                    case(-108):
                    case(-107):
                    case(-106):
                    case(14): // Particle
                    case(15): // Sparcle, irrelevant
                        return 0;
                }
                return (el == 0 || el == Sprite.KIND_FIREBALL) ? el : -2;
        }
        return el; //TODO: Throw unknown ZLevel exception
    }
}
