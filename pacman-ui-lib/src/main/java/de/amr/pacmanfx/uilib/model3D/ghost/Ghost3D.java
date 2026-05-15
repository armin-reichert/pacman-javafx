/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.animation.GhostBrakeAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostDressAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostFlashingAnimation3D;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * Represents the different 3D appearances of a ghost. One of:
 * <ul>
 * <li>{@link GhostAppearance#NORMAL}: colored ghost, blue eyes,
 * <li>{@link GhostAppearance#FRIGHTENED}: blue ghost, empty, "pinkish" eyes (looking blind),
 * <li>{@link GhostAppearance#FLASHING}: blue-white flashing skin, pink-red flashing eyes,
 * <li>{@link GhostAppearance#EYES} eyes only,
 * </ul>
 */
public class Ghost3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID {
        GHOST_BRAKING, GHOST_DRESS, GHOST_FLASHING;

        public String forGhost(Ghost ghost) {
            requireNonNull(ghost);
            return "%s_%d".formatted(name(), ghost.personality());
        }
    }

    private final ManagedAnimationsRegistry animations;
    private final Ghost ghost;
    private final GhostConfig config;

    private GhostMaterialSet materialSet;

    private final Group facingGroup = new Group();

    private MeshView dressShape;
    private MeshView pupilsShape;
    private MeshView eyeballsShape;

    private final Scale scaling = new Scale(1, 1, 1);

    private Ghost3DTransformController transformController;
    private Ghost3DAppearanceController appearanceController;

    public Ghost3D(
        ManagedAnimationsRegistry animations,
        Ghost ghost,
        GhostConfig config,
        GhostMeshSet meshSet,
        GhostMaterialSet materialSet)
    {
        this.animations = requireNonNull(animations);
        this.ghost = requireNonNull(ghost);
        this.config = requireNonNull(config);

        requireNonNull(meshSet);
        this.dressShape    = new MeshView(meshSet.dress());
        this.pupilsShape   = new MeshView(meshSet.pupils());
        this.eyeballsShape = new MeshView(meshSet.eyeballs());

        this.materialSet = requireNonNull(materialSet);

        final Group dressGroup = new Group(dressShape);
        final var eyesGroup = new Group(pupilsShape, eyeballsShape);

        getChildren().setAll(facingGroup);
        getTransforms().addAll(scaling);

        facingGroup.getChildren().addAll(dressGroup, eyesGroup);

        final Bounds dressBounds = dressShape.getBoundsInLocal();
        final Translate originCentered = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());
        dressShape.getTransforms().add(originCentered);
        eyesGroup.getTransforms().add(originCentered);

        animations.register(AnimationID.GHOST_DRESS.forGhost(ghost),
            new GhostDressAnimation3D(ghost, dressGroup));

        animations.register(AnimationID.GHOST_FLASHING.forGhost(ghost),
            new GhostFlashingAnimation3D(ghost, materialSet, config.colors()));

        animations.register(AnimationID.GHOST_BRAKING.forGhost(ghost),
            new GhostBrakeAnimation3D(this));

        scaling.setX(config().size3D() / dressBounds.getWidth());
        scaling.setY(config().size3D() / dressBounds.getHeight());
        scaling.setZ(config().size3D() / dressBounds.getDepth());

    }

    @Override
    public void init(GameLevel level) {
        transformController.update(level.worldMap());
        appearanceController.init(this, level);
    }

    @Override
    public void update(GameLevel level) {
        transformController.update(level.worldMap());
        appearanceController.update(this, level);
    }

    @Override
    public void dispose() {
        stopAllAnimations();
        for (AnimationID animationID : AnimationID.values()) {
            animations.optAnimation(animationID.forGhost(ghost)).ifPresent(ManagedAnimation::dispose);
        }

        cleanupGroup(this, true);

        materialSet = null;
        dressShape = null;
        pupilsShape = null;
        eyeballsShape = null;
    }

    public Ghost ghost() {
        return ghost;
    }

    public GhostConfig config() {
        return config;
    }

    public GhostMaterialSet materials() {
        return materialSet;
    }

    public ManagedAnimationsRegistry animations() {
        return animations;
    }

    public Group facingGroup() {
        return facingGroup;
    }

    public void stopAllAnimations() {
        for (AnimationID animationID : AnimationID.values()) {
            animations.optAnimation(animationID.forGhost(ghost)).ifPresent(ManagedAnimation::stop);
        }
    }

    public void setTransformController(Ghost3DTransformController transformController) {
        this.transformController = requireNonNull(transformController);
    }

    public void setAppearanceController(Ghost3DAppearanceController appearanceController) {
        this.appearanceController = requireNonNull(appearanceController);
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

    public void setFlashingLook(int numFlashes) {
        if (numFlashes == 0) {
            setFrightenedLook();
            return;
        }
        setMaterialSet(materialSet.flashingMaterial());
        dressShape.setVisible(true);

        animations.optAnimation(AnimationID.GHOST_FLASHING.forGhost(ghost), GhostFlashingAnimation3D.class).ifPresent(flashing -> {
            // TODO: this is crap
            if (flashing.numFlashes() != numFlashes) {
                flashing.stop();
                flashing.setNumFlashes(numFlashes);
                flashing.setTotalDuration(Duration.millis(1990));
            }
            flashing.playOrContinue();
        });
    }

    public void setEyesOnlyLook() {
        animations.animation(AnimationID.GHOST_FLASHING.forGhost(ghost)).stop();
        animations.animation(AnimationID.GHOST_DRESS.forGhost(ghost)).stop();
        dressShape.setVisible(false);
        setMaterialSet(materialSet.normalMaterial());
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

    public GhostAppearance frightenedAppearance(boolean powerFading) {
        return powerFading ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
    }

    // private area, no trespassing

    private void setMaterialSet(GhostComponentMaterialSet materialSet) {
        dressShape.setMaterial(materialSet.dressMaterial());
        pupilsShape.setMaterial(materialSet.pupilsMaterial());
        eyeballsShape.setMaterial(materialSet.eyeballsMaterial());
    }
}