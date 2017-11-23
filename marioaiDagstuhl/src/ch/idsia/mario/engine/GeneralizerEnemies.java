package ch.idsia.mario.engine;

import ch.idsia.mario.engine.sprites.Sprite;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Aug 5, 2009
 * Time: 7:04:19 PM
 * Package: ch.idsia.mario.engine
 */
public class GeneralizerEnemies implements Generalizer
{

    public byte ZLevelGeneralization(byte el, int ZLevel)
    {
        byte ret = 0;
        switch (ZLevel)
        {
            case(0):
                switch(el)
                {
                    case(Sprite.KIND_PARTICLE):
                    case(Sprite.KIND_SPARCLE):
                        return Sprite.KIND_NONE;
                }
                return el;
            case(1):
                switch(el)
                {
                    case(Sprite.KIND_COIN_ANIM):
                    case(Sprite.KIND_PARTICLE):
                    case(Sprite.KIND_SPARCLE):
                        return Sprite.KIND_NONE;
                    case(Sprite.KIND_FIREBALL):
                        return Sprite.KIND_FIREBALL;
                    case(Sprite.KIND_BULLET_BILL):
                    case(Sprite.KIND_GOOMBA):
                    case(Sprite.KIND_GOOMBA_WINGED):
                    case(Sprite.KIND_GREEN_KOOPA):
                    case(Sprite.KIND_GREEN_KOOPA_WINGED):
                    case(Sprite.KIND_RED_KOOPA):
                    case(Sprite.KIND_RED_KOOPA_WINGED):
                        return Sprite.KIND_GOOMBA;
                    case(Sprite.KIND_SPIKY):
                    case(Sprite.KIND_ENEMY_FLOWER):
                        return Sprite.KIND_SPIKY;
                }
                return el;
            case(2):
                switch(el)
                {
                    case(Sprite.KIND_COIN_ANIM):
                    case(Sprite.KIND_PARTICLE):
                    case(Sprite.KIND_SPARCLE):
                    case(Sprite.KIND_FIREBALL):
                        return Sprite.KIND_NONE;
                    case(Sprite.KIND_BULLET_BILL):
                    case(Sprite.KIND_GOOMBA):
                    case(Sprite.KIND_GOOMBA_WINGED):
                    case(Sprite.KIND_GREEN_KOOPA):
                    case(Sprite.KIND_GREEN_KOOPA_WINGED):
                    case(Sprite.KIND_RED_KOOPA):
                    case(Sprite.KIND_RED_KOOPA_WINGED):
                    case(Sprite.KIND_SPIKY):
                    case(Sprite.KIND_ENEMY_FLOWER):
                        return Sprite.KIND_SPIKY;
                }
                return (el == 0 || el == Sprite.KIND_FIREBALL) ? el : -2;
        }
        return el; //TODO: Throw unknown ZLevel exception
    }
}
