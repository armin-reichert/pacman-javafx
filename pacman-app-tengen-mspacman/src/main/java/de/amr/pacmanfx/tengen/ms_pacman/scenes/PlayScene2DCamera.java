/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Pac;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.ParallelCamera;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;

class PlayScene2DCamera extends ParallelCamera {

    record Range(double min, double max) {}

    enum State { INTRO, TRACKING, MANUAL }

    private final DoubleProperty scaling = new SimpleDoubleProperty(1);

    //TODO determine exakt values in NES emulator
    private static final int INTRO_MOVEMENT_START_TICK = 60;
    private static final int INTRO_MOVEMENT_DURATION_TICKS = 120;

    private static final float INTRO_MOVEMENT_SPEED = 0.015f;
    private static final float TRACKING_SPEED = 0.015f;

    private Range range;
    private State state;
    private int introTick;
    private float speed;
    private double targetY;

    public PlayScene2DCamera() {
        range = new Range(Double.MIN_VALUE, Double.MAX_VALUE);
        state = State.MANUAL;
        introTick = 0;
        speed = 0;
        targetY = 0;
    }

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void update(GameLevel gameLevel) {
        switch (state) {
            case INTRO -> updateIntro();
            case TRACKING -> updateTracking(gameLevel);
        }
    }

    /**
     * Intro: Show top of maze, wait some time, move to bottom of maze, finally start tracking Pac-Man.
     */
    public void startIntro() {
        if (state == State.INTRO) {
            Logger.warn("Camera intro sequence is already running");
            return;
        }
        setToTop();
        introTick = 0;
        state = State.INTRO;
        Logger.info("Camera intro sequence started");
    }

    private void updateIntro() {
        ++introTick;
        if (introTick < INTRO_MOVEMENT_START_TICK) {
            return;
        }
        if (introTick == INTRO_MOVEMENT_START_TICK) {
            setTargetBottom();
            speed = INTRO_MOVEMENT_SPEED;
        }
        else if (introTick == INTRO_MOVEMENT_START_TICK + INTRO_MOVEMENT_DURATION_TICKS) {
            startTracking();
            return;
        }
        move();
        Logger.debug("Intro tick={} y={} maxY={}", introTick, getTranslateY(), range.max());
    }

    public void startTracking() {
        speed = TRACKING_SPEED;
        state = State.TRACKING;
    }

    private void updateTracking(GameLevel gameLevel) {
        Pac pac = gameLevel.pac();
        double relY = pac.y() / TS(gameLevel.worldMap().terrainLayer().numRows());
        if (relY < 0.5 || relY < 0.6 && pac.moveDir() == Direction.UP) {
            setTargetTop();
        } else if (relY > 0.5 || relY > 0.4 && pac.moveDir() == Direction.DOWN) {
            setTargetBottom();
        }
        move();
    }

    public void endTracking() {
        speed = 0;
        state = State.MANUAL;
    }

    public void setToTop() {
        setToY(range.min());
    }

    public void setToBottom() {
        setToY(range.max());
    }

    public void setToY(double y) {
        switch (state) {
            case INTRO -> Logger.error("Cannot set camera to y-position {} while intro is running", y);
            case TRACKING -> Logger.error("Cannot set camera to y-position {} while tracking", y);
            case MANUAL -> setTranslateY(y);
        }
    }

    public void setTargetTop() {
        targetY = range.min();
    }

    public void setTargetBottom() {
        targetY = range.max();
    }

    private void move() {
        setTranslateY(lerp(getTranslateY(), targetY, speed));
    }

    // This is "alchemy", not science :-)
    public void updateRange(GameLevel gameLevel) {
        final int numRows = gameLevel.worldMap().terrainLayer().numRows();
        final int span = numRows - 26;
        int min;
        if (numRows <= 30) {  // MINI maps: 30
            min = -3;
        } else if (numRows <= 36) { // ARCADE maps: 36, a single STRANGE map: 35
            min = -6;
        } else { // BIG maps: 42
            min = -9;
        }
        range = new Range(scaledTiles(min), scaledTiles(min + span));
    }

    private double scaledTiles(int n) {
        return scaling.get() * TS(n);
    }
}