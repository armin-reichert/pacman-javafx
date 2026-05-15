/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * (Ms.) Pac-Man 3D representations.
 */
public class Pac3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID {CHEWING, DYING, MOVING}

    private final ManagedAnimationsRegistry animations;
    private final Pac pac;

    private final Group bodyGroup;
    private final Group jaw;

    private PointLight powerLight;

    private final Rotate facingRotation = new Rotate(0, Rotate.Z_AXIS);

    private final Pac3DTransformController transformController = new Pac3DTransformController();

    public Pac3D(
        ManagedAnimationsRegistry animations,
        Pac pac,
        Group body,
        Group jaw)
    {
        this.animations = requireNonNull(animations);
        this.pac = requireNonNull(pac);

        requireNonNull(body);
        this.jaw = requireNonNull(jaw);

        bodyGroup = new Group(body, jaw);
        final Group facingGroup = new Group(bodyGroup);

        facingGroup.getTransforms().addAll(facingRotation);

        getChildren().setAll(facingGroup);
    }

    public Pac pac() {
        return pac;
    }

    public Rotate facingRotation() {
        return facingRotation;
    }

    public Group bodyGroup() {
        return bodyGroup;
    }

    public Group jaw() {
        return jaw;
    }

    public void setPowerLight(PointLight powerLight) {
        this.powerLight = powerLight;
    }

    @Override
    public void dispose() {
        for (var animID : AnimationID.values()) {
            animations.optAnimation(animID).ifPresent(ManagedAnimation::dispose);
        }
        cleanupLight(powerLight);
        cleanupGroup(this, true);
    }

    @Override
    public void init(GameLevel level) {
        requireNonNull(level);

        transformController.init(this, level.worldMap());

        animations.optAnimation(AnimationID.CHEWING).ifPresent(ManagedAnimation::stop);
        animations.optAnimation(AnimationID.MOVING).ifPresent(ManagedAnimation::stop);
        animations.optAnimation(AnimationID.DYING).ifPresent(ManagedAnimation::stop);
        setPowerMode(false);
    }

    @Override
    public void update(GameLevel level) {
        requireNonNull(level);

        if (pac.isAlive()) {
            transformController.update(this, level.worldMap());

            updatePowerLight();

            animations.optAnimation(AnimationID.MOVING).ifPresent(movementAnimation -> {
                movementAnimation.playOrContinue();
                animations.optAnimation(AnimationID.MOVING, Pac3DMovementAnimation.class).ifPresent(movement -> movement.update(pac));
            });
            animations.optAnimation(AnimationID.CHEWING).ifPresent(chewingAnimation -> {
                if (pac.isParalyzed()) {
                    chewingAnimation.stop();
                } else {
                    chewingAnimation.playOrContinue();
                }
            });
        }
        else {
            animations.optAnimation(AnimationID.MOVING).ifPresent(ManagedAnimation::stop);
            animations.optAnimation(AnimationID.CHEWING).ifPresent(ManagedAnimation::stop);
        }
    }

    public Optional<PointLight> powerLight() {
        return Optional.ofNullable(powerLight);
    }

    public void setPowerMode(boolean power) {
        animations.optAnimation(AnimationID.MOVING, Pac3DMovementAnimation.class)
            .ifPresent(movement -> movement.setPowerMode(power));
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    public void updatePowerLight() {
        if (powerLight == null) return;
        final TickTimer powerTimer = pac.powerTimer();
        if (powerTimer.isRunning() && pac.isVisible() && !pac.isDead()) {
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