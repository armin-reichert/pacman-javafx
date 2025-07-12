/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import javafx.scene.text.Font;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Animated movie clapperboard.
 */
public class Clapperboard extends Actor {

    private static final byte HIDDEN = -1, WIDE_OPEN = 0, OPEN = 1, CLOSED = 2;

    private final TengenMsPacMan_SpriteSheet spriteSheet;
    private int tick;
    private byte state;
    private boolean textVisible;
    private boolean running;
    private final byte number;
    private final String text;
    private Font font = Font.font(8);

    public Clapperboard(TengenMsPacMan_SpriteSheet spriteSheet, int number, String text) {
        super(null); // no game context
        this.spriteSheet = requireNonNull(spriteSheet);
        this.number = (byte) number;
        this.text = requireNonNull(text);
    }

    public byte number() {
        return number;
    }

    public String text() {
        return text;
    }

    public Font font() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public boolean isTextVisible() {
        return textVisible;
    }

    public Optional<RectShort> sprite() {
        if (state == HIDDEN) return Optional.empty();
        RectShort[] clapperboardSprites = spriteSheet.spriteSeq(SpriteID.CLAPPERBOARD);
        return Optional.of(clapperboardSprites[state]);
    }

    public void startAnimation() {
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