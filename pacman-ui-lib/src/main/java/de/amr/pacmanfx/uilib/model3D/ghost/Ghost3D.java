/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostBrakeAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostDressAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostFlashingAnimation3D;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

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
        BRAKING, DRESS, FLASHING;

        public AnimationKey key(Ghost ghost) {
            requireNonNull(ghost);
            return new AnimationKey(this, ghost.personality());
        }
    }

    public record AnimationKey(AnimationID animationID, byte ghostID) {}

    private final AnimationRegistry animations;
    private final Ghost ghost;
    private final GhostConfig config;

    private GhostMaterialSet materialSet;

    private Group dressGroup;

    private MeshView dressMeshView;
    private MeshView pupilsMeshView;
    private MeshView eyeballsMeshView;

    private final Rotate facingRotation = new Rotate(0, Rotate.Z_AXIS);

    private Ghost3DTransformController transformController;
    private Ghost3DAppearanceController appearanceController;

    public Ghost3D(
        AnimationRegistry animations,
        Ghost ghost,
        GhostConfig config,
        GhostMeshSet meshSet,
        GhostMaterialSet materialSet)
    {
        this.animations = requireNonNull(animations);
        this.ghost = requireNonNull(ghost);
        this.config = requireNonNull(config);
        this.materialSet = requireNonNull(materialSet);

        buildHierarchy(meshSet);
        registerAnimations();
    }

    @Override
    public void init(GameLevel level) {
        assertControllersAssigned();
        transformController.init(this, level.worldMap());
        appearanceController.init(this);
    }

    @Override
    public void update(GameLevel level) {
        assertControllersAssigned();
        transformController.update(this, level.worldMap());
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
        dressMeshView = null;
        pupilsMeshView = null;
        eyeballsMeshView = null;
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

    public AnimationRegistry animations() {
        return animations;
    }

    public Rotate facingRotation() {
        return facingRotation;
    }

    public Group dressGroup() {
        return dressGroup;
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
        dressMeshView.setVisible(true);
        setShapeMaterials(materialSet.normalMaterial());
    }

    public void lookFrightened() {
        flashingDressAnimation().stop();
        normalDressAnimation().playOrContinue();
        dressMeshView.setVisible(true);
        setShapeMaterials(materialSet.frightenedMaterial());
    }

    public void lookFlashing(int numFlashes) {
        if (numFlashes == 0) {
            lookFrightened();
            return;
        }
        setShapeMaterials(materialSet.flashingMaterial());
        dressMeshView.setVisible(true);

        final GhostFlashingAnimation3D flashing = flashingDressAnimation();
        flashing.setNumFlashes(numFlashes);
        flashing.playOrContinue();
    }

    public void lookEyesOnly() {
        stopAllAnimations();
        dressMeshView.setVisible(false);
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

    /*
        this (Group)
           facingGroup (facing rotation, model orientation adaption)
              dressGroup (dress rotation animation)
                 dressMeshView
              eyesGroup
                 pupilsMeshView
                 eyeballsMeshView
     */
    private void buildHierarchy(GhostMeshSet meshSet) {
        requireNonNull(meshSet);

        // 1. Create meshes
        dressMeshView    = new MeshView(meshSet.dress());
        pupilsMeshView   = new MeshView(meshSet.pupils());
        eyeballsMeshView = new MeshView(meshSet.eyeballs());

        // 2. Create groups
        dressGroup = new Group(dressMeshView);
        Group eyesGroup = new Group(pupilsMeshView, eyeballsMeshView);
        Group facingGroup = new Group(dressGroup, eyesGroup);

        // 3. Apply transforms to the correct groups
        facingGroup.getTransforms().addAll(
            facingRotation,
            PacManWorld3D.ORIENTATION_ADJUSTMENT
        );

        // 4. Center meshes
        Bounds dressBounds = dressMeshView.getBoundsInLocal();
        Translate center = new Translate(
            -dressBounds.getCenterX(),
            -dressBounds.getCenterY(),
            -dressBounds.getCenterZ()
        );
        dressMeshView.getTransforms().add(center);
        eyesGroup.getTransforms().add(center);

        // 5. Add scaling to the root node
        final Scale scaling = new Scale(
            config().size3D() / dressBounds.getWidth(),
            config().size3D() / dressBounds.getHeight(),
            config().size3D() / dressBounds.getDepth());
        getTransforms().add(scaling);

        // 6. Add the facing group as the only child
        getChildren().setAll(facingGroup);
    }

    private void setShapeMaterials(GhostComponentMaterialSet materialSet) {
        dressMeshView.setMaterial(materialSet.dressMaterial());
        pupilsMeshView.setMaterial(materialSet.pupilsMaterial());
        eyeballsMeshView.setMaterial(materialSet.eyeballsMaterial());
    }

    private void registerAnimations() {
        animations.register(AnimationID.DRESS.key(ghost), new GhostDressAnimation3D(this, 30));
        animations.register(AnimationID.FLASHING.key(ghost), new GhostFlashingAnimation3D(this));
        animations.register(AnimationID.BRAKING.key(ghost), new GhostBrakeAnimation3D(this));
    }

    private ManagedAnimation normalDressAnimation() {
        return animations.animation(AnimationID.DRESS.key(ghost));
    }

    private GhostFlashingAnimation3D flashingDressAnimation() {
        return animations.optAnimation(AnimationID.FLASHING.key(ghost), GhostFlashingAnimation3D.class).orElseThrow();
    }
}