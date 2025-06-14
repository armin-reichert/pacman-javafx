package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.model.actors.Actor;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Animated clapperboard.
 */
public class Clapperboard extends Actor {

    private static final byte HIDDEN = -1, WIDE_OPEN = 0, OPEN = 1, CLOSED = 2;

    private final ArcadeMsPacMan_SpriteSheet spriteSheet;
    private final String number;
    private final String text;

    private int tick;
    private boolean running;
    private byte state;

    public Clapperboard(ArcadeMsPacMan_SpriteSheet spriteSheet, String number, String text) {
        this.spriteSheet = requireNonNull(spriteSheet);
        this.number = number;
        this.text = text;

    }
    public String number() {
        return number;
    }

    public String text() {
        return text;
    }

    public void startAnimation() {
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

    public Optional<Sprite> currentSprite() {
        if (state == HIDDEN) return Optional.empty();
        Sprite sprite = spriteSheet.spriteSeq(SpriteID.CLAPPERBOARD)[state];
        return Optional.of(sprite);
    }

}
