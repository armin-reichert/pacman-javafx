/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Animated movie clapperboard.
 */
public class Clapperboard extends Actor {

    public enum State { HIDDEN, WIDE_OPEN, OPEN, CLOSED }

    private final int number;
    private final String text;

    private State state;
    private int tick;
    private boolean textVisible;
    private boolean running;

    public Clapperboard(int number, String text) {
        this.number = number;
        this.text = requireNonNull(text);
    }

    public int number() {
        return number;
    }

    public String text() {
        return text;
    }

    public boolean isTextVisible() {
        return textVisible;
    }

    public Optional<RectShort> sprite() {
        RectShort[] sprites = TengenMsPacMan_SpriteSheet.instance().sprites(SpriteID.CLAPPERBOARD);
        return switch (state) {
            case HIDDEN -> Optional.empty();
            case WIDE_OPEN -> Optional.of(sprites[0]);
            case OPEN -> Optional.of(sprites[1]);
            case CLOSED -> Optional.of(sprites[2]);
        };
    }

    public void startAnimation() {
        tick = 0;
        textVisible = true;
        state = State.CLOSED;
        running = true;
    }

    public void tick() {
        if (!running) return;

        //TODO Verify exact tick values
        switch (tick) {
            case 3 -> state = State.OPEN;
            case 5 -> state = State.WIDE_OPEN;
            case 65 -> {
                state = State.CLOSED;
                textVisible = false;
            }
            case 69 -> state = State.OPEN;
            case 71 -> state = State.WIDE_OPEN;
            case 129 -> {
                state = State.HIDDEN;
                running = false;
            }
            default -> {}
        }
        ++tick;
    }
}