/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.RectArea;

import java.util.Optional;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.CLAPPERBOARD_SPRITES;

/**
 * Animated movie clapperboard.
 */
public class ClapperboardAnimation {

    private static final byte HIDDEN = -1, WIDE_OPEN = 0, OPEN = 1, CLOSED = 2;

    private int tick;
    private byte state;
    private boolean textVisible;
    private boolean running;

    public boolean isTextVisible() {
        return textVisible;
    }

    public Optional<RectArea> sprite() {
        return state == HIDDEN ? Optional.empty() : Optional.of(CLAPPERBOARD_SPRITES[state]);
    }

    public void start() {
        tick = 0;
        textVisible = true;
        state = CLOSED;
        running = true;
    }

    public void tick() {
        if (!running) return;

        //TODO Verify exact tick values
        switch (tick) {
            case 3 -> state = OPEN;
            case 5 -> state = WIDE_OPEN;
            case 65 -> {
                state = CLOSED;
                textVisible = false;
            }
            case 69 -> state = OPEN;
            case 71 -> state = WIDE_OPEN;
            case 129 -> {
                state = HIDDEN;
                running = false;
            }
            default -> {}
        }
        ++tick;
    }
}