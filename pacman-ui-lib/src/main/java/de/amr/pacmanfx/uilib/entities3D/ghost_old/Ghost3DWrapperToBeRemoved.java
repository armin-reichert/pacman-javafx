/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost_old;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.uilib.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.PacMan3DModel;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.ghost.anim.GhostDressAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.ghost.anim.GhostFlashingAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostAppearanceMaterialSet;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostSettings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Represents the 3D appearance of a ghost.
 */
public class Ghost3DWrapperToBeRemoved extends GameEntity implements DisposableGraphicsObject {

    public record AnimationKey(Ghost3DAnimationID animationID, GhostPersonality ghostID) {}

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);

    private final Ghost ghost;

    private final Group root = new Group();

    private final AnimationRegistry animations;

    private final GhostSettings settings;

    private GhostAppearanceMaterialSet materialSet;

    private Group dressGroup;

    private MeshView dressMeshView;
    private MeshView pupilsMeshView;
    private MeshView eyeballsMeshView;

    private final Rotate facingRotate = new Rotate(0, Rotate.Z_AXIS);

    private Ghost3DTransformController transformController;
    private Ghost3DAppearanceController appearanceController;

    public Ghost3DWrapperToBeRemoved(
        AnimationRegistry animations,
        Ghost ghost,
        GhostSettings settings,
        GhostMeshSet meshSet,
        GhostAppearanceMaterialSet materialSet)
    {
        this.animations = requireNonNull(animations);
        this.ghost = requireNonNull(ghost);
        this.settings = requireNonNull(settings);
        this.materialSet = requireNonNull(materialSet);

        buildHierarchy(meshSet);
        registerAnimations();
    }

    public Group root() {
        return root;
    }

    public void init(GameContext game) {
        assertControllersAssigned();
        transformController.init(this, game);
        appearanceController.init(this);
    }

    public void update(GameContext game) {
        assertControllersAssigned();
        transformController.update(this, game);
        appearanceController.update(this, game);
    }

    @Override
    public void dispose() {
        for (Ghost3DAnimationID animationID : Ghost3DAnimationID.values()) {
            animations.optAnimation(animationID.key(ghost)).ifPresent(ManagedAnimation::dispose);
        }
        cleanupGroup(root, true);

        transformController = null;
        appearanceController = null;
        materialSet = null;
        dressMeshView = null;
        pupilsMeshView = null;
        eyeballsMeshView = null;
    }

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawMode;
    }

    public Ghost ghost() {
        return ghost;
    }

    public GhostSettings settings() {
        return settings;
    }

    public GhostAppearanceMaterialSet materials() {
        return materialSet;
    }

    public AnimationRegistry animations() {
        return animations;
    }

    public Rotate facingRotate() {
        return facingRotate;
    }

    public Group dressGroup() {
        return dressGroup;
    }

    public MeshView dressMeshView() {
        return dressMeshView;
    }

    public MeshView eyeballsMeshView() {
        return eyeballsMeshView;
    }

    public MeshView pupilsMeshView() {
        return pupilsMeshView;
    }

    public void stopAllAnimations() {
        for (Ghost3DAnimationID animationID : Ghost3DAnimationID.values()) {
            animations.optAnimation(animationID.key(ghost)).ifPresent(ManagedAnimation::stop);
        }
    }

    public void setTransformController(Ghost3DTransformController transformController) {
        this.transformController = requireNonNull(transformController);
    }

    public void setAppearanceController(Ghost3DAppearanceController appearanceController) {
        this.appearanceController = requireNonNull(appearanceController);
    }

    public Optional<GhostDressAnimation3D> dressAnimation() {
        return animations.optAnimation(Ghost3DAnimationID.DRESS.key(ghost), GhostDressAnimation3D.class);
    }

    public Optional<GhostFlashingAnimation3D> dressColorFlashingAnimation() {
        return animations.optAnimation(Ghost3DAnimationID.FLASHING.key(ghost), GhostFlashingAnimation3D.class);
    }

    // Private Area, no trespassing!

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
            facingRotate,
            PacMan3DModel.ORIENTATION_ADJUSTMENT
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
            settings().size3D() / dressBounds.getWidth(),
            settings().size3D() / dressBounds.getHeight(),
            settings().size3D() / dressBounds.getDepth());
        root.getTransforms().add(scaling);

        // 6. Add the facing group as the only child
        root.getChildren().setAll(facingGroup);

        // 7. Bind draw mode
        dressMeshView.drawModeProperty().bind(drawMode);
        pupilsMeshView.drawModeProperty().bind(drawMode);
        eyeballsMeshView.drawModeProperty().bind(drawMode);
    }

    private void registerAnimations() {
        //animations.register(Ghost3DAnimationID.DRESS.key(ghost), new GhostDressAnimation3D(this, 30));
        //animations.register(Ghost3DAnimationID.FLASHING.key(ghost), new GhostFlashingAnimation3D(this));
        //animations.register(Ghost3DAnimationID.BRAKING.key(ghost), new GhostBrakeAnimation3D(this));
    }

    private void assertControllersAssigned() {
        requireNonNull(transformController, "No transform controller has been assigned");
        requireNonNull(appearanceController, "No appearance controller has been assigned");
    }
}