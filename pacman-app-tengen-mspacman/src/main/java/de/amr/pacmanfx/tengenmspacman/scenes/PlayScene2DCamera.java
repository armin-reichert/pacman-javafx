/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.world.WorldMap;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.ParallelCamera;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;

public class PlayScene2DCamera extends ParallelCamera {

    public static final float NORMAL_CAMERA_SPEED = 0.014f;
    public static final float INTRO_CAMERA_SPEED = 0.03f;

    private record RangeY(double topPosition, double bottomPosition) {}

    private enum State { INTRO, TRACKING, MANUAL }

    //TODO determine exact values in NES emulator
    private static final int INTRO_TILT_START_TICK = 60;
    private static final int INTRO_TILT_DURATION_TICKS = 120;

    private final DoubleProperty scaling = new SimpleDoubleProperty(1);

    private RangeY rangeY;
    private State state;
    private int introTick;
    private double targetY;
    private float cameraSpeed = NORMAL_CAMERA_SPEED;

    public PlayScene2DCamera() {
        rangeY = new RangeY(Double.MIN_VALUE, Double.MAX_VALUE);
        state = State.MANUAL;
    }

    // This is "alchemy", not science :-)
    public void updateRange(WorldMap worldMap) {
        final int mapHeightTiles = worldMap.terrainLayer().numRows();
        final int spannedTiles = mapHeightTiles - 26;
        final int topPosition = switch (mapHeightTiles) {
            case 30 -> -3;     // all MINI maps
            case 35, 36 -> -6; // one STRANGE, all ARCADE maps
            case 42 -> -9;     // all BIG maps
            default -> {
                Logger.warn("Unexpected map height (tiles): {}", mapHeightTiles);
                yield 0;
            }
        };
        double top = Math.floor(scaling.get() * TS(topPosition));
        double bottom = Math.ceil(scaling.get() * TS(topPosition + spannedTiles));
        rangeY = new RangeY(top, bottom);
    }

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void update(double mapHeightPixels, MovingActor movingActor) {
        switch (state) {
            case INTRO -> updateIntroMode();
            case TRACKING -> updateTrackingMode(mapHeightPixels, movingActor);
        }
    }

    /**
     * Intro: Show top of maze, wait some time, move to bottom of maze, finally start tracking Pac-Man.
     */
    public void enterIntroMode() {
        if (state == State.INTRO) {
            Logger.warn("Camera intro sequence is already running");
            return;
        }
        enterManualMode();
        setToTopPosition();
        state = State.INTRO;
        introTick = 0;
        cameraSpeed = INTRO_CAMERA_SPEED;
        Logger.info("Camera intro sequence started");
    }

    private void updateIntroMode() {
        ++introTick;
        if (introTick < INTRO_TILT_START_TICK) {
            return;
        }
        if (introTick == INTRO_TILT_START_TICK) {
            setTargetToBottom();
        }
        else if (introTick == INTRO_TILT_START_TICK + INTRO_TILT_DURATION_TICKS) {
            Logger.info("Camera intro sequence ended");
            cameraSpeed = NORMAL_CAMERA_SPEED;
            enterTrackingMode();
            return;
        }
        move();
        Logger.debug("Intro tick={} y={} maxY={}", introTick, getTranslateY(), rangeY.bottomPosition());
    }

    public void enterTrackingMode() {
        state = State.TRACKING;
    }

    private void updateTrackingMode(double mapHeightPixels, MovingActor movingActor) {
        double relY = movingActor.y() / mapHeightPixels;
        if (relY < 0.5 || relY < 0.6 && movingActor.moveDir() == Direction.UP) {
            setTargetToTop();
        } else if (relY > 0.5 || relY > 0.4 && movingActor.moveDir() == Direction.DOWN) {
            setTargetToBottom();
        }
        move();
    }

    public void enterManualMode() {
        state = State.MANUAL;
    }

    public void setToTopPosition() {
        setTranslateY(rangeY.topPosition());
    }

    public void setTargetToTop() {
        targetY = rangeY.topPosition();
    }

    public void setTargetToBottom() {
        targetY = rangeY.bottomPosition();
    }

    private void move() {
        double y = lerp(getTranslateY(), targetY, cameraSpeed);
        setTranslateY(y);
    }
}