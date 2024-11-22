/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.lib.RectArea;

import java.util.Optional;

import static de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameSpriteSheet.CLAPPERBOARD_SPRITES;

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

    public Optional<RectArea> currentSprite() {
        if (tick == 0) {
            return Optional.of(CLAPPERBOARD_SPRITES[CLOSED]);
        }
        if (tick <= 47) {
            return Optional.of(CLAPPERBOARD_SPRITES[WIDE]);
        }
        if (tick <= 53) {
            return Optional.of(CLAPPERBOARD_SPRITES[OPEN]);
        }
        if (tick <= 58) {
            return Optional.of(CLAPPERBOARD_SPRITES[CLOSED]);
        }
        if (tick <= 87) {
            return Optional.of(CLAPPERBOARD_SPRITES[WIDE]);
        }
        return Optional.empty();
    }
}