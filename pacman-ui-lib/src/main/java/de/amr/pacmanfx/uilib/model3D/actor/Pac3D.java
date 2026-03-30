/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * Common base class for (Ms.) Pac-Man 3D representations.
 */
public abstract class Pac3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID {PAC_CHEWING, PAC_DYING, PAC_MOVING }

    protected final Pac pac;
    protected final AnimationRegistry animations;

    protected PointLight powerLight;

    protected Group body;
    protected Group jaw;

    protected Rotate moveRotation = new Rotate();

    public Group jaw() {
        return jaw;
    }

    protected Pac3D(AnimationRegistry animations, Pac pac) {
        this.animations = requireNonNull(animations);
        this.pac = requireNonNull(pac);
        getTransforms().add(moveRotation);
    }

    public void setBody(Group body) {
        this.body = requireNonNull(body);
        if (jaw == null) {
            getChildren().setAll(body);
        } else {
            getChildren().setAll(body, jaw);
        }
    }

    public void setJaw(Group jaw) {
        this.jaw = requireNonNull(jaw);
        if (body == null) {
            getChildren().setAll(jaw);
        } else {
            getChildren().setAll(body, jaw);
        }
    }

    @Override
    public void dispose() {
        animations.optAnimation(AnimationID.PAC_CHEWING).ifPresent(ManagedAnimation::dispose);
        animations.optAnimation(AnimationID.PAC_DYING).ifPresent(ManagedAnimation::dispose);
        animations.optAnimation(AnimationID.PAC_MOVING).ifPresent(ManagedAnimation::dispose);
        cleanupLight(powerLight);
        cleanupGroup(this, true);
    }

    @Override
    public void init(GameLevel level) {
        requireNonNull(level);
        stopAnimations();
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        updatePositionAndRotation();
        updateVisibility(level.worldMap());
        setMovementAnimationPowerMode(false);
    }

    @Override
    public void update(GameLevel level) {
        requireNonNull(level);
        if (pac.isAlive()) {
            updatePositionAndRotation();
            updateVisibility(level.worldMap());
            updatePowerLight();
            animations.optAnimation(AnimationID.PAC_MOVING).ifPresent(movementAnimation -> {
                movementAnimation.playOrContinue();
                updateMovementAnimation();
            });
            animations.optAnimation(AnimationID.PAC_CHEWING).ifPresent(chewingAnimation -> {
                if (pac.isParalyzed()) {
                    chewingAnimation.stop();
                } else {
                    chewingAnimation.playOrContinue();
                }
            });
        } else {
            stopMovementAnimation();
            stopChewingAnimation();
        }
    }

    public Optional<PointLight> powerLight() {
        return Optional.ofNullable(powerLight);
    }

    public void setMovementAnimationPowerMode(boolean power) {}

    public abstract void updateMovementAnimation();

    protected void stopChewingAnimation() {
        animations.optAnimation(AnimationID.PAC_CHEWING).ifPresent(ManagedAnimation::stop);
    }

    protected void stopMovementAnimation() {
        animations.optAnimation(AnimationID.PAC_MOVING).ifPresent(ManagedAnimation::stop);
    }

    protected void stopDyingAnimation() {
        animations.optAnimation(AnimationID.PAC_DYING).ifPresent(ManagedAnimation::stop);
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