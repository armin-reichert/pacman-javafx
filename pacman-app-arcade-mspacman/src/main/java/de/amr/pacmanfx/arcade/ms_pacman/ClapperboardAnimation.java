/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.RectArea;

import java.util.Optional;

import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_SpriteSheet.CLAPPERBOARD_SPRITES;

/**
 * Animated move clapperboard.
 */
public class ClapperboardAnimation {

    private static final byte HIDDEN = -1, WIDE_OPEN = 0, OPEN = 1, CLOSED = 2;

    private final String number;
    private final String text;

    private int tick;
    private boolean running;
    private byte state;

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
        state = WIDE_OPEN;
        running = true;
    }

    public void tick() {
        if (running) {
            switch (tick) {
                case 48 -> state = OPEN;
                case 54 -> state = CLOSED;
                case 59 -> state = WIDE_OPEN;
                case 88 -> {
                    state = HIDDEN;
                    running = false;
                }
            }
            ++tick;
        }
    }

    public Optional<RectArea> currentSprite() {
        return state == HIDDEN ? Optional.empty() : Optional.of(CLAPPERBOARD_SPRITES[state]);
    }
}