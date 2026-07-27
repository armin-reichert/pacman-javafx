/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.level.GameLevelMessage;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;

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
        registerComponent(Movement.class, new Movement());
        this.startPosition = requireNonNull(startPosition);
        this.delayTicks = delayTicks;
        position().set(startPosition);
    }

    public void startMovement(float rightEdge, double messageTextWidth) {
        width = (float) messageTextWidth;
        wrapX = rightEdge + 0.5f * width;
        movement().setVelocity(1, 0);
        playing = true;
    }

    public void stopMovement() {
        movement().setVelocity(0,0);
        playing = false;
    }

    public void updateMovement(GameContext gameContext) {
        if (!playing) return;

        if (delayTicks > 0) {
            --delayTicks;
            return;
        }

        final MovementSystem motor = gameContext.systems().motor;
        motor.moveAccelerated(this);
        if (wrapped) {
            if (position().x >= startPosition.x()) {
                position().set(startPosition);
                stopMovement();
            }
        } else if (position().x > wrapX) {
            position().setX(-0.5 * width);
            wrapped = true;
        }
    }
}