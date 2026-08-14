/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.level.GameLevelMessage;
import de.amr.pacmanfx.core.level.GameLevelMessageType;

/**
 * The "game over" message in Tengen Ms. Pac-Man (in non-Arcade maps) moves (after some delay) from the center of the
 * scene to the right border, wraps around and moves from the left border back to the center.
 */
public class MovingGameLevelMessage extends GameLevelMessage {

    private Vector2f startPosition;
    private float wrapX;
    private float width;
    private boolean wrapped;
    private int delayTicks;
    private boolean playing;

    public MovingGameLevelMessage(GameLevelMessageType messageType) {
        super(messageType);
        setComp(MovementComp.class, new MovementComp());
    }

    public void setStartPosition(Vector2f startPosition) {
        this.startPosition = startPosition;
    }

    public void setDelayTicks(int delayTicks) {
        this.delayTicks = delayTicks;
    }

    public void startMovement(MovementSystem motor, float rightEdge, double messageTextWidth) {
        width = (float) messageTextWidth;
        wrapX = rightEdge + 0.5f * width;
        pos().set(startPosition);
        motor.setVelocity(this, 1, 0);
        playing = true;
    }

    public void stopMovement(MovementSystem motor) {
        motor.setVelocity(this, 0, 0);
        playing = false;
    }

    public void updateMovement(GameContext game) {
        if (!playing) return;

        if (delayTicks > 0) {
            --delayTicks;
            return;
        }

        final MovementSystem motor = game.variantConfig().systems().motor();
        motor.move(this);
        if (wrapped) {
            if (pos().x() >= startPosition.x()) {
                pos().set(startPosition);
                stopMovement(motor);
            }
        } else if (pos().x() > wrapX) {
            pos().setX(-0.5 * width);
            wrapped = true;
        }
    }
}