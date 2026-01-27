/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.GameLevelMessageType;

import static java.util.Objects.requireNonNull;

/**
 * The "game over" message in Tengen Ms. Pac-Man (in non-Arcade maps) moves (after some delay) from the center of the
 * scene to the right border, wraps around and moves from the left border back to the center.
 */
public class MovingGameLevelMessage extends GameLevelMessage {

    private final Vector2f startPosition;
    private float wrapX;
    private float width;
    private boolean wrapped;
    private long delayTicks;
    private boolean playing;

    public MovingGameLevelMessage(GameLevelMessageType messageType, Vector2f startPosition, int delayTicks) {
        super(messageType);
        this.startPosition = requireNonNull(startPosition);
        this.delayTicks = delayTicks;
        setPosition(startPosition);
    }

    public void startMovement(float rightEdge, double messageTextWidth) {
        width = (float) messageTextWidth;
        wrapX = rightEdge + 0.5f * width;
        setVelocity(1, 0);
        playing = true;
    }

    public void stopMovement() {
        setVelocity(Vector2f.ZERO);
        playing = false;
    }

    public void updateMovement() {
        if (!playing) return;

        if (delayTicks > 0) {
            --delayTicks;
            return;
        }
        move();
        if (wrapped) {
            if (x() >= startPosition.x()) {
                setPosition(startPosition);
                stopMovement();
            }
        } else if (x() > wrapX) {
            setX(-0.5 * width);
            wrapped = true;
        }
    }
}