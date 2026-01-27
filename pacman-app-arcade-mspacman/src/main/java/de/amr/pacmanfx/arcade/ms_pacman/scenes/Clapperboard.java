/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import javafx.scene.text.Font;

import static java.util.Objects.requireNonNull;

/**
 * Animated clapperboard.
 */
public class Clapperboard extends Actor {

    public static final byte WIDE_OPEN = 0, OPEN = 1, CLOSED = 2;

    private final String number;
    private final String text;
    private Font font = Font.font(8);

    private int tick;
    private boolean running;
    private byte state;

    public Clapperboard(String number, String text) {
        this.number = number;
        this.text = text;
        this.state = CLOSED;
    }

    public byte state() {
        return state;
    }

    public String number() {
        return number;
    }

    public String text() {
        return text;
    }

    public Font font() { return font; }

    public void setFont(Font font) { this.font = requireNonNull(font); }

    public void startAnimation() {
        tick = 0;
        state = WIDE_OPEN;
        running = true;
        show();
    }

    public void tick() {
        if (running) {
            switch (tick) {
                case 48 -> state = OPEN;
                case 54 -> state = CLOSED;
                case 59 -> state = WIDE_OPEN;
                case 88 -> {
                    hide();
                    running = false;
                }
            }
            ++tick;
        }
    }
}
