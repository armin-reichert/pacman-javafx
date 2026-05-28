/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.basics.spriteanim.AnimationIdentifier;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.transform.Rotate;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * (Ms.) Pac-Man 3D representations.
 */
public class Pac3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID implements AnimationIdentifier {
        CHEWING,
        DYING,
        MOVING
    }

    private final AnimationRegistry animations;
    private final Pac pac;

    private final Group bodyGroup;
    private final Group jaw;

    private PointLight powerLight;

    private final Rotate facingRotate = new Rotate(0, Rotate.Z_AXIS);

    private final Pac3DTransformController transformController;
    private final Pac3DAnimationController animationController;

    public Pac3D(
        AnimationRegistry animations,
        Pac pac,
        Group body,
        Group jaw)
    {
        this.animations = requireNonNull(animations);
        this.pac = requireNonNull(pac);

        this.transformController = new Pac3DTransformController();
        this.animationController = new Pac3DAnimationController(animations);

        requireNonNull(body);
        this.jaw = requireNonNull(jaw);

        bodyGroup = new Group(body, jaw);
        final Group facingGroup = new Group(bodyGroup);

        facingGroup.getTransforms().addAll(facingRotate);

        getChildren().setAll(facingGroup);
    }

    public Pac pac() {
        return pac;
    }

    public Rotate facingRotate() {
        return facingRotate;
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

    public Optional<PointLight> powerLight() {
        return Optional.ofNullable(powerLight);
    }

    public void setPowerMode(boolean power) {
        animationController.setPowerMode(power);
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
        animationController.init(this);
        setPowerMode(false);
    }

    @Override
    public void update(GameLevel level) {
        requireNonNull(level);
        transformController.update(this, level.worldMap());
        animationController.update(this);
    }
}