/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;

/**
 * The "game over" message in Tengen Ms. Pac-Man (in non-Arcade maps) moves (after some delay) from the center of the
 * scene to the right border, wraps around and moves from the left border back to the center.
 */
public class MessageAnimation extends GameEntity {

    private double wrapX;
    private double width;
    private boolean wrapped;
    private int delayTicks;
    private boolean running;
    private boolean finished;

    private Vector2f startPosition;

    public MessageAnimation() {
        setComp(MovementComp.class, new MovementComp());
    }

    public void setDelayTicks(int delayTicks) {
        this.delayTicks = delayTicks;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setWrapX(double wrapX) {
        this.wrapX = wrapX;
    }

    public void start(Vector2f startPosition, MovementSystem motor) {
        this.startPosition = startPosition;
        show();
        pos().set(startPosition);
        motor.setVelocity(this, 1, 0);
        running = true;
        finished = false;
    }

    public void update(MovementSystem motor) {
        if (!running) return;

        if (delayTicks > 0) {
            --delayTicks;
            return;
        }

        motor.move(this);
        if (wrapped) {
            if (pos().x() >= startPosition.x()) {
                motor.setVelocity(this, 0, 0);
                running = false;
                finished = true;
            }
        } else if (pos().x() > wrapX) {
            pos().setX(-0.5 * width);
            wrapped = true;
        }
    }

    public boolean finished() {
        return finished;
    }

}