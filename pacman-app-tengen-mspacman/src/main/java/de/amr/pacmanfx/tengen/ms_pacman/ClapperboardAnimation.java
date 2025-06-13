/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Sprite;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Animated movie clapperboard.
 */
public class ClapperboardAnimation {

    private static final byte HIDDEN = -1, WIDE_OPEN = 0, OPEN = 1, CLOSED = 2;

    private final TengenMsPacMan_SpriteSheet spriteSheet;
    private int tick;
    private byte state;
    private boolean textVisible;
    private boolean running;

    public ClapperboardAnimation(TengenMsPacMan_SpriteSheet spriteSheet) {
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    public boolean isTextVisible() {
        return textVisible;
    }

    public Optional<Sprite> sprite() {
        if (state == HIDDEN) return Optional.empty();
        Sprite[] clapperboardSprites = spriteSheet.spriteSeq(SpriteID.CLAPPERBOARD);
        return Optional.of(clapperboardSprites[state]);
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