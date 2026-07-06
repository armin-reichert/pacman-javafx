/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelEntity;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostBrakeAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostDressAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostFlashingAnimation3D;
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
public class Ghost3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID implements Identifier {
        BRAKING, DRESS, FLASHING;

        public AnimationKey key(Ghost ghost) {
            requireNonNull(ghost);
            return new AnimationKey(this, ghost.personality());
        }
    }

    public record AnimationKey(AnimationID animationID, byte ghostID) {}

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);

    private final AnimationRegistry animations;
    private final Ghost ghost;
    private final GhostSettings settings;

    private GhostMaterialSet materialSet;

    private Group dressGroup;

    private MeshView dressMeshView;
    private MeshView pupilsMeshView;
    private MeshView eyeballsMeshView;

    private final Rotate facingRotate = new Rotate(0, Rotate.Z_AXIS);

    private Ghost3DTransformController transformController;
    private Ghost3DAppearanceController appearanceController;

    public Ghost3D(
        AnimationRegistry animations,
        Ghost ghost,
        GhostSettings settings,
        GhostMeshSet meshSet,
        GhostMaterialSet materialSet)
    {
        this.animations = requireNonNull(animations);
        this.ghost = requireNonNull(ghost);
        this.settings = requireNonNull(settings);
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
    public void update(GameLevel level, GameEventManager eventManager) {
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

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawMode;
    }

    public Ghost ghost() {
        return ghost;
    }

    public GhostSettings settings() {
        return settings;
    }

    public GhostMaterialSet materials() {
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

    public Optional<GhostDressAnimation3D> dressAnimation() {
        return animations.optAnimation(AnimationID.DRESS.key(ghost), GhostDressAnimation3D.class);
    }

    public Optional<GhostFlashingAnimation3D> dressColorFlashingAnimation() {
        return animations.optAnimation(AnimationID.FLASHING.key(ghost), GhostFlashingAnimation3D.class);
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
            settings().size3D() / dressBounds.getWidth(),
            settings().size3D() / dressBounds.getHeight(),
            settings().size3D() / dressBounds.getDepth());
        getTransforms().add(scaling);

        // 6. Add the facing group as the only child
        getChildren().setAll(facingGroup);

        // 7. Bind draw mode
        dressMeshView.drawModeProperty().bind(drawMode);
        pupilsMeshView.drawModeProperty().bind(drawMode);
        eyeballsMeshView.drawModeProperty().bind(drawMode);
    }

    private void registerAnimations() {
        animations.register(AnimationID.DRESS.key(ghost), new GhostDressAnimation3D(this, 30));
        animations.register(AnimationID.FLASHING.key(ghost), new GhostFlashingAnimation3D(this));
        animations.register(AnimationID.BRAKING.key(ghost), new GhostBrakeAnimation3D(this));
    }

    private void assertControllersAssigned() {
        requireNonNull(transformController, "No transform controller has been assigned");
        requireNonNull(appearanceController, "No appearance controller has been assigned");
    }
}