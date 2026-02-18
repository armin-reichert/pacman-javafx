/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.LightBase;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Common base class for (Ms.) Pac-Man 3D representations.
 */
public abstract class PacBase3D extends Group implements Disposable {

    protected final Pac pac;
    protected final double size;
    protected final AnimationRegistry animationRegistry;

    protected PacBody body;
    protected PacBodyNoEyes jaw;
    protected Rotate moveRotation = new Rotate();

    protected PointLight light = new PointLight();

    protected RegisteredAnimation chewingAnimation;
    protected RegisteredAnimation dyingAnimation;
    protected RegisteredAnimation movementAnimation;

    protected PacBase3D(
        PacManModel3DRepository model3DRepository,
        AnimationRegistry animationRegistry,
        Pac pac,
        double size,
        Color headColor, Color eyesColor, Color palateColor)
    {
        this.pac = requireNonNull(pac);
        this.size = size;
        this.animationRegistry = requireNonNull(animationRegistry);

        requireNonNull(model3DRepository);

        body = model3DRepository.pacManModel().createPacBody(size, headColor, eyesColor, palateColor);
        jaw = model3DRepository.pacManModel().createBlindPacBody(size, headColor, palateColor);

        getChildren().setAll(jaw, body);
        getTransforms().add(moveRotation);
        setTranslateZ(-0.5 * size);

        chewingAnimation = new RegisteredAnimation(animationRegistry, "PacMan_Chewing") {
            @Override
            protected Animation createAnimationFX() {
                var mouthClosed = new KeyValue[] {
                        new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
                        new KeyValue(jaw.rotateProperty(), -54, Interpolator.LINEAR)
                };
                var mouthOpen = new KeyValue[] {
                        new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
                        new KeyValue(jaw.rotateProperty(), 0, Interpolator.LINEAR)
                };
                var animation = new Timeline(
                        new KeyFrame(Duration.ZERO,        "Open on Start", mouthOpen),
                        new KeyFrame(Duration.millis(100), "Start Closing", mouthOpen),
                        new KeyFrame(Duration.millis(130), "Closed",        mouthClosed),
                        new KeyFrame(Duration.millis(200), "Start Opening", mouthClosed),
                        new KeyFrame(Duration.millis(280), "Open",          mouthOpen)
                );
                animation.setCycleCount(Animation.INDEFINITE);
                return animation;
            }

            @Override
            public void stop() {
                Animation animation = getOrCreateAnimationFX();
                animation.stop();
                if (jaw != null) {
                    // open mouth when stopped
                    jaw.setRotationAxis(Rotate.Y_AXIS);
                    jaw.setRotate(0);
                }
            }
        };

        light.translateXProperty().bind(translateXProperty());
        light.translateYProperty().bind(translateYProperty());
        light.setTranslateZ(-30);
    }

    public LightBase light() {
        return light;
    }

    public RegisteredAnimation dyingAnimation() {
        return dyingAnimation;
    }

    public void setMovementPowerMode(boolean power) {}

    public abstract void updateMovementAnimation();

    public void init(GameLevel gameLevel) {
        if (chewingAnimation != null) {
            chewingAnimation.stop();
        }
        if (movementAnimation != null) {
            movementAnimation.stop();
        }
        if (dyingAnimation != null) {
            dyingAnimation.stop();
        }
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        updatePositionAndRotation();
        updateVisibility(gameLevel);
        setMovementPowerMode(false);
    }

    public void update(GameLevel gameLevel) {
        if (pac.isAlive()) {
            updatePositionAndRotation();
            updateVisibility(gameLevel);
            updateLight();
            if (movementAnimation != null) {
                movementAnimation.playOrContinue();
                updateMovementAnimation();
            }
            if (chewingAnimation != null) {
                if (pac.isParalyzed()) {
                    chewingAnimation.stop();
                } else {
                    chewingAnimation.playOrContinue();
                }
            }
        } else {
            if (movementAnimation != null) {
                movementAnimation.stop();
            }
            if (chewingAnimation != null) {
                chewingAnimation.stop();
            }
        }
    }

    protected void updatePositionAndRotation() {
        Vector2f center = pac.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size);
        double angle = switch (pac.moveDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        };
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(angle);
    }

    protected void updateVisibility(GameLevel gameLevel) {
        if (gameLevel != null) {
            WorldMap worldMap = gameLevel.worldMap();
            boolean outsideWorld = getTranslateX() < HTS || getTranslateX() > TS * worldMap.numCols() - HTS;
            setVisible(pac.isVisible() && !outsideWorld);
        }
        else {
            setVisible(pac.isVisible());
        }
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    protected void updateLight() {
        TickTimer powerTimer = pac.powerTimer();
        if (powerTimer.isRunning() && pac.isVisible()) {
            light.setLightOn(true);
            long remainingTicks = powerTimer.remainingTicks();
            float maxRange = (remainingTicks / (float) powerTimer.durationTicks()) * 60 + 30;
            light.setMaxRange(maxRange);
            Logger.debug("Power remaining: {}, light max range: {0.00}", remainingTicks, maxRange);
        } else {
            light.setLightOn(false);
        }
    }

    // Experimental:

    @Override
    public void dispose() {
        if (chewingAnimation != null) {
            chewingAnimation.dispose();
            chewingAnimation = null;
        }
        if (movementAnimation != null) {
            movementAnimation.dispose();
            movementAnimation = null;
        }
        if (dyingAnimation != null) {
            dyingAnimation.dispose();
            dyingAnimation = null;
        }
        light.translateXProperty().unbind();
        light.translateYProperty().unbind();
        light.translateZProperty().unbind();
        getChildren().clear();
        if (body != null) {
            body.dispose();
            body = null;
        }
        if (jaw != null) {
            jaw.dispose();
            jaw = null;
        }
    }
}