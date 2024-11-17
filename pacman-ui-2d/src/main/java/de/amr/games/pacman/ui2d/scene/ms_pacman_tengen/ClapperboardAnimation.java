/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;

/**
 * @author Armin Reichert
 */
public class ClapperboardAnimation {

    private static final byte WIDE = 0;
    private static final byte OPEN = 1;
    private static final byte CLOSED = 2;

    private final RectArea[] sprites;

    private boolean textVisible;
    private RectArea sprite;
    private int tick;
    private boolean running;

    public ClapperboardAnimation() {
        sprites = MsPacManTengenGameSpriteSheet.CLAPPERBOARD_SPRITES;
    }

    public boolean isTextVisible() {
        return textVisible;
    }

    public boolean isRunning() {
        return running;
    }

    public RectArea sprite() {
        return sprite;
    }

    public void start() {
        tick = 0;
        textVisible = true;
        sprite = sprites[CLOSED];
        running = true;
    }

    //TODO Times are probably not 100% accurate
    public void tick() {
        if (running) {
            switch (tick) {
                case 3 -> sprite = sprites[OPEN];
                case 5 -> sprite = sprites[WIDE];
                case 65 -> {
                    sprite = sprites[CLOSED];
                    textVisible = false;
                }
                case 69 -> sprite = sprites[OPEN];
                case 71 -> sprite = sprites[WIDE];
                case 129 -> {
                    sprite = RectArea.PIXEL;
                    running = false;
                }
                default -> {}
            }
            ++tick;
        }
    }
}