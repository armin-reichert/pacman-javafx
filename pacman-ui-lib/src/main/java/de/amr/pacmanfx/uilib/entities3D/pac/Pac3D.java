/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.DisposableGraphicsObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * (Ms.) Pac-Man 3D representations.
 */
public class Pac3D extends GameEntity implements UpdatableEntity, DisposableGraphicsObject {

    public enum AnimationID implements Named {
        CHEWING,
        DYING,
        MOVING
    }

    private final Group root = new Group();
    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);

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

        root.getChildren().setAll(facingGroup);

        Ufx.bindDrawMode(bodyGroup, drawMode);
        Ufx.bindDrawMode(jaw, drawMode);
    }

    public Group root() {
        return root;
    }

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawMode;
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
        cleanupGroup(root, true);
    }

    @Override
    public void init(GameContext gameContext) {
        requireNonNull(gameContext);
        transformController.init(this, gameContext);
        animationController.init();
        setPowerMode(false);
    }

    @Override
    public void update(GameContext gameContext) {
        requireNonNull(gameContext);
        transformController.update(gameContext, this);
        animationController.update(gameContext, this);
    }
}