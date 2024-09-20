/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.ms_pacman;

import de.amr.games.pacman.ui2d.rendering.RectangularArea;

/**
 * @author Armin Reichert
 */
public class ClapperboardAnimation {

    private static final byte WIDE = 0;
    private static final byte OPEN = 1;
    private static final byte CLOSED = 2;

    private final String number;
    private final String text;

    private long tick;
    private boolean running;

    public ClapperboardAnimation(String number, String text) {
        this.number = number;
        this.text = text;
    }

    public String number() {
        return number;
    }

    public String text() {
        return text;
    }

    public void start() {
        tick = 0;
        running = true;
    }

    public void tick() {
        if (running) {
            ++tick;
        }
    }

    public RectangularArea currentSprite(RectangularArea[] sprites) {
        if (tick == 0) {
            return sprites[CLOSED];
        }
        if (tick <= 47) {
            return sprites[WIDE];
        }
        if (tick <= 53) {
            return sprites[OPEN];
        }
        if (tick <= 58) {
            return sprites[CLOSED];
        }
        if (tick <= 87) {
            return sprites[WIDE];
        }
        return RectangularArea.EMPTY;
    }
}