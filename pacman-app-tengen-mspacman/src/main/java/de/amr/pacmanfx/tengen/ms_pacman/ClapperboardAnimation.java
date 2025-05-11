/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.RectArea;

import java.util.Optional;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.CLAPPERBOARD_SPRITES;

/**
 * @author Armin Reichert
 */
public class ClapperboardAnimation {

    private static final RectArea
        WIDE = CLAPPERBOARD_SPRITES[0],
        OPEN = CLAPPERBOARD_SPRITES[1],
        CLOSED = CLAPPERBOARD_SPRITES[2];

    private int tick;
    private RectArea sprite;
    private boolean textVisible;
    private boolean running;

    public boolean isTextVisible() {
        return textVisible;
    }

    public boolean isRunning() {
        return running;
    }

    public Optional<RectArea> sprite() {
        return Optional.ofNullable(sprite);
    }

    public void start() {
        tick = 0;
        textVisible = true;
        sprite = CLOSED;
        running = true;
    }

    //TODO Times are probably not 100% accurate
    public void tick() {
        if (running) {
            switch (tick) {
                case 3 -> sprite = OPEN;
                case 5 -> sprite = WIDE;
                case 65 -> {
                    sprite = CLOSED;
                    textVisible = false;
                }
                case 69 -> sprite = OPEN;
                case 71 -> sprite = WIDE;
                case 129 -> {
                    sprite = null;
                    running = false;
                }
                default -> {}
            }
            ++tick;
        }
    }
}