/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.MessageType;

import static java.util.Objects.requireNonNull;

/**
 * The "game over" message in Tengen Ms. Pac-Man (in non-Arcade maps) moves (after some delay) from the center of the
 * scene to the right border, wraps around and moves from the left border back to the center.
 */
public class GameOverMessage extends GameLevelMessage {

    private final Vector2f startPosition;
    private float wrapX;
    private float width;
    private boolean wrapped;
    private long delayTicks;
    private boolean playing;

    public GameOverMessage(Vector2f startPosition, int delayTicks) {
        super(MessageType.GAME_OVER);
        this.startPosition = requireNonNull(startPosition);
        this.delayTicks = delayTicks;
        setPosition(startPosition);
    }

    public void start(float rightEdge, double messageTextWidth) {
        this.width = (float) messageTextWidth;
        this.wrapX = rightEdge + 0.5f * width;
        setVelocity(1, 0);
        playing = true;
    }

    public void stop() {
        setVelocity(Vector2f.ZERO);
        playing = false;
    }

    public void update() {
        if (!playing) return;

        if (delayTicks > 0) {
            --delayTicks;
            return;
        }
        move();
        if (wrapped) {
            if (x() >= startPosition.x()) {
                setPosition(startPosition);
                stop();
            }
        } else if (x() > wrapX) {
            setX(-0.5 * width);
            wrapped = true;
        }
    }
}