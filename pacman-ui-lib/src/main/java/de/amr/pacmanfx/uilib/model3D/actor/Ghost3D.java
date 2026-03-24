/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.GhostMaterials;
import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of a ghost.
 */
public class Ghost3D extends Group implements DisposableGraphicsObject {

    public class DressAnimation extends ManagedAnimation {

        public DressAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Ghost_DressAnimation_%s".formatted(ghost.name()));
            setFactory(() -> {
                final var animation = new RotateTransition(Duration.seconds(0.3), dressGroup);
                // TODO: I expected this should be the z-axis but...
                animation.setAxis(Rotate.Y_AXIS);
                animation.setByAngle(30);
                animation.setCycleCount(Animation.INDEFINITE);
                animation.setAutoReverse(true);
                return animation;
            });
        }
    }

    public class FlashingAnimation extends ManagedAnimation {

        private Duration totalDuration = Duration.seconds(3);
        private int numFlashes = 5;

        public FlashingAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Ghost_Flashing_%s".formatted(ghost.name()));
            setFactory(this::createAnimationFX);
        }

        public void setTotalDuration(Duration totalDuration) {
            this.totalDuration = requireNonNull(totalDuration);
            invalidate();
        }

        public void setNumFlashes(int numFlashes) {
            this.numFlashes = Validations.requireNonNegativeInt(numFlashes);
            invalidate();
        }

        private Animation createAnimationFX() {
            final Duration flashEndTime = totalDuration.divide(numFlashes);
            final Duration highlightTime = flashEndTime.divide(3);
            final var flashingTimeline = new Timeline(
                new KeyFrame(highlightTime,
                    new KeyValue(materials.flashing().dress().diffuseColorProperty(),  colorSet.flashing().dressColor()),
                    new KeyValue(materials.flashing().pupils().diffuseColorProperty(), colorSet.flashing().pupilsColor())
                ),
                new KeyFrame(flashEndTime,
                    new KeyValue(materials.flashing().dress().diffuseColorProperty(),  colorSet.frightened().dressColor()),
                    new KeyValue(materials.flashing().pupils().diffuseColorProperty(), colorSet.frightened().pupilsColor())
                )
            );
            flashingTimeline.setCycleCount(numFlashes);
            flashingTimeline.setOnFinished(_ -> {
                materials.flashing().dress().setDiffuseColor(colorSet.frightened().dressColor());
                materials.flashing().dress().setSpecularColor(colorSet.frightened().dressColor().brighter());
                materials.flashing().pupils().setDiffuseColor(colorSet.frightened().pupilsColor());
                materials.flashing().pupils().setSpecularColor(colorSet.frightened().pupilsColor().brighter());
            });
            return flashingTimeline;
        }
    }

    private final Ghost ghost;
    private final GhostColorSet colorSet;
    private GhostMaterials materials;

    private MeshView dressShape;
    private MeshView pupilsShape;
    private MeshView eyeballsShape;
    private Group dressGroup;

    private DressAnimation dressAnimation;
    private FlashingAnimation flashingAnimation;

    public Ghost3D(
        AnimationRegistry animationRegistry,
        Ghost ghost,
        GhostColorSet colorSet,
        GhostMeshes meshes,
        GhostMaterials materials,
        double size)
    {
        requireNonNull(animationRegistry);
        this.ghost         = requireNonNull(ghost);
        this.colorSet      = requireNonNull(colorSet);
        this.dressShape    = new MeshView(requireNonNull(meshes.dress()));
        this.pupilsShape   = new MeshView(requireNonNull(meshes.pupils()));
        this.eyeballsShape = new MeshView(requireNonNull(meshes.eyeballs()));
        this.materials     = requireNonNull(materials);
        requireNonNegative(size);

        dressGroup = new Group(dressShape);
        final var eyesGroup = new Group(pupilsShape, eyeballsShape);

        getChildren().setAll(dressGroup, eyesGroup);

        final Bounds dressBounds = dressShape.getBoundsInLocal();
        var centeredOverOrigin = new Translate(
            -dressBounds.getCenterX(),
            -dressBounds.getCenterY(),
            -dressBounds.getCenterZ()
        );
        dressShape.getTransforms().add(centeredOverOrigin);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        // TODO: change orientation in OBJ file?
        getTransforms().addAll(
            new Rotate(90, Rotate.X_AXIS),
            new Rotate(180, Rotate.Y_AXIS),
            new Rotate(180, Rotate.Z_AXIS)
        );

        final Bounds bounds = getBoundsInLocal();
        getTransforms().add(
            new Scale(
                size / bounds.getWidth(),
                size / bounds.getHeight(),
                size / bounds.getDepth())
        );

        dressAnimation = new DressAnimation(animationRegistry);
        flashingAnimation = new FlashingAnimation(animationRegistry);
    }

    @Override
    public void dispose() {
        materials = null;

        cleanupGroup(this, true);

        dressShape = null;
        pupilsShape = null;
        eyeballsShape = null;
        dressGroup = null;

        if (dressAnimation != null) {
            dressAnimation.dispose();
            dressAnimation = null;

        }
        if (flashingAnimation != null) {
            flashingAnimation.dispose();
            flashingAnimation = null;
        }
    }

    public Ghost ghost() {
        return ghost;
    }

    public GhostColorSet colorSet() {
        return colorSet;
    }

    public GhostMaterials materials() {
        return materials;
    }

    public ManagedAnimation dressAnimation() {
        return dressAnimation;
    }

    public FlashingAnimation flashingAnimation() {
        return flashingAnimation;
    }

    public void turnTowards(Direction dir) {
        requireNonNull(dir);
        setRotationAxis(Rotate.Z_AXIS);
        setRotate(switch (dir) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });
    }

    public void setFlashingLook(int numFlashes) {
        if (numFlashes == 0) {
            setFrightenedLook();
            return;
        }
        setMaterials(materials.flashing());
        dressShape.setVisible(true);
        flashingAnimation.setNumFlashes(numFlashes);
        // TODO (fixme): Total flashing time must be shorter than Pac power fading time (2s)!
        flashingAnimation.setTotalDuration(Duration.millis(1990));
        flashingAnimation.playFromStart();
    }

    private void setMaterials(GhostComponentMaterials materialSet) {
        dressShape.setMaterial(materialSet.dress());
        pupilsShape.setMaterial(materialSet.pupils());
        eyeballsShape.setMaterial(materialSet.eyeballs());
    }

    public void setNormalLook() {
        flashingAnimation.stop();
        dressAnimation.playOrContinue();
        dressShape.setVisible(true);
        setMaterials(materials.normal());
    }

    public void setFrightenedLook() {
        flashingAnimation.stop();
        dressAnimation.playOrContinue();
        dressShape.setVisible(true);
        setMaterials(materials.frightened());
    }

    public void setEyesOnlyLook() {
        flashingAnimation.stop();
        dressAnimation.pause();
        dressShape.setVisible(false);
        setMaterials(materials.normal());
    }

    public void stopAnimations() {
        if (dressAnimation != null) {
            dressAnimation.stop();
        }
        if (flashingAnimation != null) {
            flashingAnimation.stop();
        }
    }
}