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
        BRAKING, NORMAL, FLASHING;

        public AnimationKey key(Ghost ghost) {
            requireNonNull(ghost);
            return new AnimationKey(this, ghost.personality());
        }
    }

    public record AnimationKey(AnimationID animationID, byte ghostID) {}

    private final ManagedAnimationsRegistry animations;
    private final Ghost ghost;
    private final GhostConfig config;

    private GhostMaterialSet materialSet;

    private Group facingGroup;
    private Group dressGroup;
    private Group eyesGroup;

    private MeshView dressShape;
    private MeshView pupilsShape;
    private MeshView eyeballsShape;

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
        this.materialSet = requireNonNull(materialSet);

        buildTransformHierarchy(requireNonNull(meshSet));

        animations.register(AnimationID.NORMAL.key(ghost), new GhostDressAnimation3D(ghost, dressGroup));
        animations.register(AnimationID.FLASHING.key(ghost), new GhostFlashingAnimation3D(ghost, materialSet, config.colors()));
        animations.register(AnimationID.BRAKING.key(ghost), new GhostBrakeAnimation3D(this));
    }

    @Override
    public void init(GameLevel level) {
        assertControllersAssigned();
        transformController.update(level.worldMap());
        appearanceController.init(this);
    }

    @Override
    public void update(GameLevel level) {
        assertControllersAssigned();
        transformController.update(level.worldMap());
        appearanceController.update(this, level);
    }

    @Override
    public void dispose() {
        for (AnimationID animationID : AnimationID.values()) {
            animations.optAnimation(animationID.key(ghost)).ifPresent(ManagedAnimation::dispose);
        }
        cleanupGroup(this, true);

        transformController = null;
        appearanceController = null;
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
            animations.optAnimation(animationID.key(ghost)).ifPresent(ManagedAnimation::stop);
        }
    }

    public void setTransformController(Ghost3DTransformController transformController) {
        this.transformController = requireNonNull(transformController);
    }

    public void setAppearanceController(Ghost3DAppearanceController appearanceController) {
        this.appearanceController = requireNonNull(appearanceController);
    }

    public void lookNormal() {
        flashingDressAnimation().stop();
        normalDressAnimation().playOrContinue();
        dressShape.setVisible(true);
        setShapeMaterials(materialSet.normalMaterial());
    }

    public void lookFrightened() {
        flashingDressAnimation().stop();
        normalDressAnimation().playOrContinue();
        dressShape.setVisible(true);
        setShapeMaterials(materialSet.frightenedMaterial());
    }

    public void lookFlashing(int numFlashes) {
        if (numFlashes == 0) {
            lookFrightened();
            return;
        }
        setShapeMaterials(materialSet.flashingMaterial());
        dressShape.setVisible(true);

        final GhostFlashingAnimation3D flashing = (GhostFlashingAnimation3D) flashingDressAnimation();
        // TODO: this is total crap and needs to be reimplemented
        if (flashing.numFlashes() != numFlashes) {
            flashing.stop();
            flashing.setNumFlashes(numFlashes);
            flashing.setTotalDuration(Duration.millis(1990));
        }
        flashing.playOrContinue();
    }

    public void lookEyesOnly() {
        stopAllAnimations();
        dressShape.setVisible(false);
        setShapeMaterials(materialSet.normalMaterial());
    }

    public void animateNormalDress(boolean on) {
        if (on) {
            normalDressAnimation().playOrContinue();
        } else {
            normalDressAnimation().stop();
        }
    }

    // Private Area, no trespassing!

    private void assertControllersAssigned() {
        requireNonNull(transformController, "No transform controller has been assigned");
        requireNonNull(appearanceController, "No appearance controller has been assigned");
    }

    private void buildTransformHierarchy(GhostMeshSet meshSet) {
        dressShape    = new MeshView(meshSet.dress());
        pupilsShape   = new MeshView(meshSet.pupils());
        eyeballsShape = new MeshView(meshSet.eyeballs());
        dressGroup    = new Group(dressShape);
        eyesGroup     = new Group(pupilsShape, eyeballsShape);
        facingGroup   = new Group(dressGroup, eyesGroup);
        getChildren().setAll(facingGroup);

        // Set transforms

        final Bounds dressBounds = dressShape.getBoundsInLocal();

        final Translate centerAtOrigin = new Translate(
            -dressBounds.getCenterX(),
            -dressBounds.getCenterY(),
            -dressBounds.getCenterZ());

        dressShape.getTransforms().add(centerAtOrigin);
        eyesGroup .getTransforms().add(centerAtOrigin);

        final Scale scale = new Scale(
            config().size3D() / dressBounds.getWidth(),
            config().size3D() / dressBounds.getHeight(),
            config().size3D() / dressBounds.getDepth());

        getTransforms().add(scale);
    }

    private void setShapeMaterials(GhostComponentMaterialSet materialSet) {
        dressShape.setMaterial(materialSet.dressMaterial());
        pupilsShape.setMaterial(materialSet.pupilsMaterial());
        eyeballsShape.setMaterial(materialSet.eyeballsMaterial());
    }

    private ManagedAnimation normalDressAnimation() {
        return animations.animation(AnimationID.NORMAL.key(ghost));
    }

    private ManagedAnimation flashingDressAnimation() {
        return animations.animation(AnimationID.FLASHING.key(ghost));
    }
}