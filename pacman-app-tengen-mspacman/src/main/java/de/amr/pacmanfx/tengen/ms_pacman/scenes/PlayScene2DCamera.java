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

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;

class PlayScene2DCamera extends ParallelCamera {

    private final DoubleProperty scaling = new SimpleDoubleProperty(1);

    private static final float MIN_CAMERA_MOVEMENT = 0.5f;
    private static final float CAMERA_SPEED = 0.02f;

    private static final int INTRO_DELAY_TICKS = 60;
    private static final int INTRO_MOVE_TICKS = 30;

    private boolean introRunning;
    private int introTick;

    private boolean trackingPac;
    private double tgtY;
    private double minY;
    private double maxY;

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    /**
     * Show top of maze, wait some time, then move to bottom, then focus Pac-Man.
     */
    public void startIntro() {
        introTick = 0;
        introRunning = true;
    }

    private void playIntro() {
        switch (introTick) {
            case 0 -> {
                setToTop();
                trackingPac = false;
            }
            case INTRO_DELAY_TICKS -> setTargetBottom();
            case INTRO_DELAY_TICKS + INTRO_MOVE_TICKS -> {
                trackingPac = true;
                introRunning = false;
                return;
            }
        }
        ++introTick;
    }

    public void stop() {
        introRunning = false;
        trackingPac = false;
    }

    public void setTrackingPac(boolean follow) {
        trackingPac = follow;
    }

    public void setToTop() {
        setTrackingPac(false);
        setTranslateY(minY);
    }

    public void setToBottom() {
        setTrackingPac(false);
        setTranslateY(maxY);
    }

    public void setTargetTop() {
        tgtY = minY;
    }

    public void setTargetBottom() {
        tgtY = maxY;
    }

    private void setTargetFollowingPac(GameLevel gameLevel) {
        Pac pac = gameLevel.pac();
        double relY = pac.y() / TS(gameLevel.worldMap().terrainLayer().numRows());
        if (relY < 0.25 || relY < 0.6 && pac.moveDir() == Direction.UP) {
            setTargetTop();
        } else if (relY > 0.75 || relY > 0.4 && pac.moveDir() == Direction.DOWN) {
            setTargetBottom();
        }
    }

    public void update(GameLevel gameLevel) {
        updateRange(gameLevel);
        if (introRunning) {
            playIntro();
            return;
        }
        if (trackingPac) {
            setTargetFollowingPac(gameLevel);
        }
        move();
    }

    // This is "alchemy", not science :-)
    private void updateRange(GameLevel gameLevel) {
        int numRows = gameLevel.worldMap().terrainLayer().numRows();
        if (numRows <= 30) {
            // MINI
            minY = scaledTiles(-3);
            maxY = scaledTiles(2);
        } else if (numRows >= 42) {
            // BIG
            minY = scaledTiles(-9);
            maxY = scaledTiles(8);
        } else {
            // ARCADE and a single STRANGE maze
            minY = scaledTiles(-6);
            maxY = scaledTiles(5);
        }
    }

    private double scaledTiles(int n) {
        return scaling.get() * TS(n);
    }

    private void move() {
        double oldCameraY = getTranslateY();
        double newCameraY = lerp(oldCameraY, tgtY, CAMERA_SPEED);
        double delta = Math.abs(oldCameraY - newCameraY);
        if (delta > MIN_CAMERA_MOVEMENT) {
            setTranslateY(newCameraY);
        }
    }
}
