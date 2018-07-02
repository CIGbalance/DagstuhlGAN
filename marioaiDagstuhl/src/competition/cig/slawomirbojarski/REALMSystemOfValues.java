package competition.cig.slawomirbojarski;

/**
 * Copyright (c) 2010, Slawomir Bojarski <slawomir.bojarski@maine.edu>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
import ch.idsia.maibe.tasks.SystemOfValues;

/** 
 * Custom system of values used in previous tests.
 * 
 * @author Slawomir Bojarski
 */
public class REALMSystemOfValues extends SystemOfValues {
    final public int distance = 1; // original value: 1
    final public int win = 1024; // original value: 1024
    final public int mode = 32; // original value: 32
    final public int coins = 0; // original value: 16
    final public int hiddenCoins = 0; // original value: 24
    final public int flowerFire = 0;  // original value: 64 (not used for now)
    final public int kills = 0; // original value: 42
    final public int killedByFire = 0; // original value: 4
    final public int killedByShell = 0; // original value: 17
    final public int killedByStomp = 0; // original value: 12
    final public int timeLeft = 8; // original value: 8

    public interface timeLengthMapping {
        final public static int TIGHT = 10;
        final public static int MEDIUM = 20;
        final public static int FLEXIBLE = 30;
    }
}
