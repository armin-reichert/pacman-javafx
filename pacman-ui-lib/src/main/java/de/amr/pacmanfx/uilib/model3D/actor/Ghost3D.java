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

    public enum AnimationID {
        GHOST_DRESS, GHOST_FLASHING;

        public String forGhost(Ghost ghost) {
            requireNonNull(ghost);
            return "%s_%d".formatted(name(), ghost.personality());
        }
    }

    public class DressAnimation extends ManagedAnimation {

        public DressAnimation() {
            super("Ghost Dress Animation (%s)".formatted(ghost.name()));
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

        public FlashingAnimation() {
            super("Ghost Flashing (%s)".formatted(ghost.name()));
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
                    new KeyValue(materials.flashing().dress().diffuseColorProperty(),  colorSet.flashing().dress()),
                    new KeyValue(materials.flashing().pupils().diffuseColorProperty(), colorSet.flashing().pupils())
                ),
                new KeyFrame(flashEndTime,
                    new KeyValue(materials.flashing().dress().diffuseColorProperty(),  colorSet.frightened().dress()),
                    new KeyValue(materials.flashing().pupils().diffuseColorProperty(), colorSet.frightened().pupils())
                )
            );
            flashingTimeline.setCycleCount(numFlashes);
            flashingTimeline.setOnFinished(_ -> {
                materials.flashing().dress().setDiffuseColor(colorSet.frightened().dress());
                materials.flashing().dress().setSpecularColor(colorSet.frightened().dress().brighter());
                materials.flashing().pupils().setDiffuseColor(colorSet.frightened().pupils());
                materials.flashing().pupils().setSpecularColor(colorSet.frightened().pupils().brighter());
            });
            return flashingTimeline;
        }
    }

    private final Ghost ghost;
    private final GhostColorSet colorSet;
    private final AnimationRegistry animations;

    private GhostMaterials materials;
    private MeshView dressShape;
    private MeshView pupilsShape;
    private MeshView eyeballsShape;
    private Group dressGroup;

    public Ghost3D(
        AnimationRegistry animations,
        Ghost ghost,
        GhostColorSet colorSet,
        GhostMeshes meshes,
        GhostMaterials materials,
        double size)
    {
        this.animations = requireNonNull(animations);
        this.ghost             = requireNonNull(ghost);
        this.colorSet          = requireNonNull(colorSet);
        this.dressShape        = new MeshView(requireNonNull(meshes.dress()));
        this.pupilsShape       = new MeshView(requireNonNull(meshes.pupils()));
        this.eyeballsShape     = new MeshView(requireNonNull(meshes.eyeballs()));
        this.materials         = requireNonNull(materials);
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

        animations.register(AnimationID.GHOST_DRESS.forGhost(ghost),    new DressAnimation());
        animations.register(AnimationID.GHOST_FLASHING.forGhost(ghost), new FlashingAnimation());
    }

    @Override
    public void dispose() {
        animations.optAnimation(AnimationID.GHOST_DRESS.forGhost(ghost)).ifPresent(ManagedAnimation::dispose);
        animations.optAnimation(AnimationID.GHOST_FLASHING.forGhost(ghost)).ifPresent(ManagedAnimation::dispose);
        cleanupGroup(this, true);
        materials = null;
        dressShape = null;
        pupilsShape = null;
        eyeballsShape = null;
        dressGroup = null;
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

        animations.optAnimation(AnimationID.GHOST_FLASHING.forGhost(ghost), FlashingAnimation.class).ifPresent(flashing -> {
            // TODO: this is crap
            if (flashing.numFlashes != numFlashes) {
                flashing.stop();
                flashing.setNumFlashes(numFlashes);
                flashing.setTotalDuration(Duration.millis(1990));
            }
            flashing.playOrContinue();
        });
    }

    private void setMaterials(GhostComponentMaterials materialSet) {
        dressShape.setMaterial(materialSet.dress());
        pupilsShape.setMaterial(materialSet.pupils());
        eyeballsShape.setMaterial(materialSet.eyeballs());
    }

    public void setNormalLook() {
        animations.animation(AnimationID.GHOST_FLASHING.forGhost(ghost)).stop();
        animations.animation(AnimationID.GHOST_DRESS.forGhost(ghost)).playOrContinue();
        dressShape.setVisible(true);
        setMaterials(materials.normal());
    }

    public void setFrightenedLook() {
        animations.animation(AnimationID.GHOST_FLASHING.forGhost(ghost)).stop();
        animations.animation(AnimationID.GHOST_DRESS.forGhost(ghost)).playOrContinue();
        dressShape.setVisible(true);
        setMaterials(materials.frightened());
    }

    public void setEyesOnlyLook() {
        animations.animation(AnimationID.GHOST_FLASHING.forGhost(ghost)).stop();
        animations.animation(AnimationID.GHOST_DRESS.forGhost(ghost)).stop();
        dressShape.setVisible(false);
        setMaterials(materials.normal());
    }

    public void stopAnimations() {
        for (AnimationID animationID : AnimationID.values()) {
            animations.optAnimation(animationID.forGhost(ghost)).ifPresent(ManagedAnimation::stop);
        }
    }

    public void animateDress(boolean on) {
        animations.optAnimation(AnimationID.GHOST_DRESS.forGhost(ghost)).ifPresent(dressAnimation -> {
            if (on) {
                dressAnimation.playOrContinue();
            } else {
                dressAnimation.stop();
            }
        });
    }
}