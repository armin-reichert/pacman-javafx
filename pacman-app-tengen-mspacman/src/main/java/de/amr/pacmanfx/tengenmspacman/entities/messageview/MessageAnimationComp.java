/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.entities.messageview;

import de.amr.basics.ecs.GameEntityComp;
import de.amr.basics.math.Vector2f;

/**
 * The "game over" message in Tengen Ms. Pac-Man (in non-Arcade maps) moves (after some delay) from the center of the
 * scene to the right border, wraps around and moves from the left border back to the center.
 */
public class MessageAnimationComp implements GameEntityComp {

    private int delayTicks;

    private double wrapX;

    private double width;

    private boolean wrapped;

    private boolean running;

    private boolean finished;

    private Vector2f startPosition;

    public MessageAnimationComp() {}

    public Vector2f startPosition() {
        return startPosition;
    }

    public void setStartPosition(Vector2f startPosition) {
        this.startPosition = startPosition;
    }

    public boolean wrapped() {
        return wrapped;
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
    }

    public boolean running() {
        return running;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean finished() {
        return finished;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int delayTicks() {
        return delayTicks;
    }

    public void setDelayTicks(int delayTicks) {
        this.delayTicks = delayTicks;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double width() {
        return width;
    }

    public void setWrapX(double wrapX) {
        this.wrapX = wrapX;
    }

    public double wrapX() {
        return wrapX;
    }
}