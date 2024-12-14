/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.RectArea;

import java.util.Optional;

import static de.amr.games.pacman.arcade.ms_pacman.MsPacManGameSpriteSheet.CLAPPERBOARD_SPRITES;

/**
 * @author Armin Reichert
 */
public class ClapperboardAnimation {

    private static final RectArea
        WIDE_SPRITE = CLAPPERBOARD_SPRITES[0],
        OPEN_SPRITE = CLAPPERBOARD_SPRITES[1],
        CLOSED_SPRITE = CLAPPERBOARD_SPRITES[2];

    private final String number;
    private final String text;

    private int tick;
    private boolean running;
    private RectArea currentSprite;

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
        currentSprite = CLOSED_SPRITE;
        running = true;
    }

    public void tick() {
        if (running) {
            ++tick;
        }
    }

    public Optional<RectArea> currentSprite() {
        switch (tick) {
            case  1 -> currentSprite = WIDE_SPRITE;
            case 48 -> currentSprite = OPEN_SPRITE;
            case 54 -> currentSprite = CLOSED_SPRITE;
            case 59 -> currentSprite = WIDE_SPRITE;
            case 88 -> {
                currentSprite = null;
                running = false;
            }
        }
        return Optional.ofNullable(currentSprite);
    }
}