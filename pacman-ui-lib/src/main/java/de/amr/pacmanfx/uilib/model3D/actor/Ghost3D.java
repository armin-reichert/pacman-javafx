/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.GhostMaterialSet;
import de.amr.pacmanfx.uilib.model3D.PacManGameModel3D;
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

    private final ManagedAnimationsRegistry animations;

    private final Ghost ghost;
    private final GhostColorSet colorSet;
    private GhostMaterialSet materialSet;

    private Group dressGroup;
    private MeshView dressShape;
    private MeshView pupilsShape;
    private MeshView eyeballsShape;

    private final Rotate facing = new Rotate(0, Rotate.Z_AXIS);
    private final Scale scaling = new Scale(1, 1, 1);

    public Ghost3D(
        ManagedAnimationsRegistry animations,
        Ghost ghost,
        GhostColorSet colorSet,
        GhostMeshSet meshSet,
        GhostMaterialSet materialSet,
        double size)
    {
        this.animations    = requireNonNull(animations);
        this.ghost         = requireNonNull(ghost);
        this.colorSet      = requireNonNull(colorSet);
        this.dressShape    = new MeshView(meshSet.dress());
        this.pupilsShape   = new MeshView(meshSet.pupils());
        this.eyeballsShape = new MeshView(meshSet.eyeballs());
        this.materialSet = requireNonNull(materialSet);
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

        getTransforms().addAll(scaling, facing);
        PacManGameModel3D.fixShapeOrientation(this);

        setSize(size);

        animations.register(AnimationID.GHOST_DRESS.forGhost(ghost),    new DressAnimation(ghost, dressGroup));
        animations.register(AnimationID.GHOST_FLASHING.forGhost(ghost), new GhostFlashingAnimation(ghost, materialSet, colorSet));
    }

    @Override
    public void dispose() {
        animations.optAnimation(AnimationID.GHOST_DRESS.forGhost(ghost)).ifPresent(ManagedAnimation::dispose);
        animations.optAnimation(AnimationID.GHOST_FLASHING.forGhost(ghost)).ifPresent(ManagedAnimation::dispose);
        cleanupGroup(this, true);
        materialSet = null;
        dressShape = null;
        pupilsShape = null;
        eyeballsShape = null;
        dressGroup = null;
    }

    public void setSize(double size) {
        final Bounds b = getBoundsInLocal();
        scaling.setX(size / b.getWidth());
        scaling.setY(size / b.getHeight());
        scaling.setZ(size / b.getDepth());
    }

    public Ghost ghost() {
        return ghost;
    }

    public GhostColorSet colorSet() {
        return colorSet;
    }

    public GhostMaterialSet materials() {
        return materialSet;
    }

    public void turnTowards(Direction dir) {
        requireNonNull(dir);
        facing.setAngle(switch (dir) {
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
        setMaterialSet(materialSet.flashingMaterial());
        dressShape.setVisible(true);

        animations.optAnimation(AnimationID.GHOST_FLASHING.forGhost(ghost), GhostFlashingAnimation.class).ifPresent(flashing -> {
            // TODO: this is crap
            if (flashing.numFlashes() != numFlashes) {
                flashing.stop();
                flashing.setNumFlashes(numFlashes);
                flashing.setTotalDuration(Duration.millis(1990));
            }
            flashing.playOrContinue();
        });
    }

    private void setMaterialSet(GhostComponentMaterialSet materialSet) {
        dressShape.setMaterial(materialSet.dressMaterial());
        pupilsShape.setMaterial(materialSet.pupilsMaterial());
        eyeballsShape.setMaterial(materialSet.eyeballsMaterial());
    }

    public void setNormalLook() {
        animations.animation(AnimationID.GHOST_FLASHING.forGhost(ghost)).stop();
        animations.animation(AnimationID.GHOST_DRESS.forGhost(ghost)).playOrContinue();
        dressShape.setVisible(true);
        setMaterialSet(materialSet.normalMaterial());
    }

    public void setFrightenedLook() {
        animations.animation(AnimationID.GHOST_FLASHING.forGhost(ghost)).stop();
        animations.animation(AnimationID.GHOST_DRESS.forGhost(ghost)).playOrContinue();
        dressShape.setVisible(true);
        setMaterialSet(materialSet.frightenedMaterial());
    }

    public void setEyesOnlyLook() {
        animations.animation(AnimationID.GHOST_FLASHING.forGhost(ghost)).stop();
        animations.animation(AnimationID.GHOST_DRESS.forGhost(ghost)).stop();
        dressShape.setVisible(false);
        setMaterialSet(materialSet.normalMaterial());
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