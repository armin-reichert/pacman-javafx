/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import javafx.animation.*;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Common base class for (Ms.) Pac-Man 3D representations.
 */
public abstract class Pac3D extends Group implements DisposableGraphicsObject {

    protected final Pac pac;

    protected PointLight powerLight;

    protected PacBody body;
    protected PacBodyNoEyes jaw;

    protected Rotate moveRotation = new Rotate();

    protected ManagedAnimation chewingAnimation;
    protected ManagedAnimation dyingAnimation;
    protected ManagedAnimation movementAnimation;

    protected Pac3D(AnimationRegistry animationRegistry, Pac pac) {
        requireNonNull(animationRegistry);
        this.pac = requireNonNull(pac);

        getTransforms().add(moveRotation);

        chewingAnimation = new ManagedAnimation(animationRegistry, "PacMan_Chewing");
        chewingAnimation.setFactory(this::createChewingAnimation);
    }

    public void setBody(PacBody body) {
        this.body = requireNonNull(body);
        if (jaw == null) {
            getChildren().setAll(body);
        } else {
            getChildren().setAll(body, jaw);
        }
    }

    public void setJaw(PacBodyNoEyes jaw) {
        this.jaw = requireNonNull(jaw);
        if (body == null) {
            getChildren().setAll(jaw);
        } else {
            getChildren().setAll(body, jaw);
        }
    }

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
        cleanupLight(powerLight);
        cleanupGroup(this, true);
    }

    public void init(GameLevel level) {
        requireNonNull(level);
        stopAnimations();
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        updatePositionAndRotation();
        updateVisibility(level.worldMap());
        setMovementPowerMode(false);
    }

    public void update(GameLevel level) {
        requireNonNull(level);
        if (pac.isAlive()) {
            updatePositionAndRotation();
            updateVisibility(level.worldMap());
            updatePowerLight();
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
            stopMovementAnimation();
            stopChewingAnimation();
        }
    }

    public Optional<PointLight> light() {
        return Optional.ofNullable(powerLight);
    }

    public ManagedAnimation dyingAnimation() {
        return dyingAnimation;
    }

    public void setMovementPowerMode(boolean power) {}

    public abstract void updateMovementAnimation();

    protected Animation createChewingAnimation() {
        final var mouthClosed = new KeyValue[] {
            new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
            new KeyValue(jaw.rotateProperty(), -54, Interpolator.LINEAR)
        };
        final var mouthOpen = new KeyValue[] {
            new KeyValue(jaw.rotationAxisProperty(), Rotate.Y_AXIS),
            new KeyValue(jaw.rotateProperty(), 0, Interpolator.LINEAR)
        };
        final var chewing = new Timeline(
            new KeyFrame(Duration.ZERO,        "Open on Start", mouthOpen),
            new KeyFrame(Duration.millis(100), "Start Closing", mouthOpen),
            new KeyFrame(Duration.millis(130), "Closed",        mouthClosed),
            new KeyFrame(Duration.millis(200), "Start Opening", mouthClosed),
            new KeyFrame(Duration.millis(280), "Open",          mouthOpen)
        );
        chewing.setCycleCount(Animation.INDEFINITE);
        chewing.statusProperty().addListener((_, _, newStatus) -> {
            if (newStatus == Animation.Status.STOPPED) {
                jaw.setRotationAxis(Rotate.Y_AXIS);
                jaw.setRotate(0);
            }
        });
        return chewing;
    }

    protected void stopChewingAnimation() {
        if (chewingAnimation != null) {
            chewingAnimation.stop();
        }
    }

    protected void stopMovementAnimation() {
        if (movementAnimation != null) {
            movementAnimation.stop();
        }
    }

    protected void stopDyingAnimation() {
        if (dyingAnimation != null) {
            dyingAnimation.stop();
        }
    }

    protected void stopAnimations() {
        stopChewingAnimation();
        stopMovementAnimation();
        stopDyingAnimation();
    }

    protected void updatePositionAndRotation() {
        final Vector2f center = pac.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        final double angle = switch (pac.moveDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        };
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(angle);
    }

    protected void updateVisibility(WorldMap worldMap) {
        final boolean outsideWorld = getTranslateX() < HTS || getTranslateX() > TS * worldMap.numCols() - HTS;
        setVisible(pac.isVisible() && !outsideWorld);
    }

    public void createPowerLight(PacConfig pacConfig) {
        powerLight = new PointLight();
        powerLight.setColor(pacConfig.colors().head().desaturate());
        powerLight.translateXProperty().bind(translateXProperty());
        powerLight.translateYProperty().bind(translateYProperty());
        powerLight.setTranslateZ(-30);
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    public void updatePowerLight() {
        if (powerLight == null) return;
        final TickTimer powerTimer = pac.powerTimer();
        if (powerTimer.isRunning() && pac.isVisible()) {
            powerLight.setLightOn(true);
            final long remainingTicks = powerTimer.remainingTicks();
            final float maxRange = (remainingTicks / (float) powerTimer.durationTicks()) * 60 + 30;
            powerLight.setMaxRange(maxRange);
            Logger.debug("Power remaining: {}, light max range: {0.00}", remainingTicks, maxRange);
        } else {
            powerLight.setLightOn(false);
        }
    }
}