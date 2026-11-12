/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.ghost.system;

import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostStateComp;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.*;
import de.amr.pacmanfx.uilib.view3d.PacManMeshes3D;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import static java.util.Objects.requireNonNull;

public class GhostView3DSystem {

    public GhostView3DSystem() {}

    public void updateAppearance(Ghost ghost) {
        requireNonNull(ghost);
        final var view3D = ghost.assertComponent(GhostView3D.class);

        final GhostStateComp state = ghost.state();
        final GhostAppearance appearance = switch (state.enumValue()) {
            case LOCKED -> state.hasPacPower() ? frightenedAppearance(state) : GhostAppearance.NORMAL;
            case EATEN -> GhostAppearance.EATEN;
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            case FRIGHTENED -> frightenedAppearance(state);
            case HUNTING_PAC, LEAVING_HOUSE -> GhostAppearance.NORMAL;
        };
        setAppearance(view3D, ghost, appearance);
    }

    /*
    root (tf: scaling)
        facingGroup (tf: facing-rotate, model-orientation-adjustment)
            dressGroup (tf: dress-rotation-animation)
            dressMeshView (tf: centering)
        eyesGroup (tf: centering)
             pupilsMeshView
            eyeballsMeshView
    */
    public GhostView3D buildGhostView3D(GhostSettings settings, Mesh dressMesh, Mesh pupilsMesh, Mesh eyeballsMesh) {
        requireNonNull(settings);
        requireNonNull(dressMesh);
        requireNonNull(pupilsMesh);
        requireNonNull(eyeballsMesh);

        final GhostView3D view3D = new GhostView3D();

        view3D.setRoot(new Group());
        view3D.setDressGroup(new Group());

        final MeshView dressMeshView = new MeshView(dressMesh);
        final MeshView pupilsMeshView = new MeshView(pupilsMesh);
        final MeshView eyeballsMeshView = new MeshView(eyeballsMesh);

        view3D.setDressMeshView(dressMeshView);
        view3D.setPupilsMeshView(pupilsMeshView);
        view3D.setEyeballsMeshView(eyeballsMeshView);

        view3D.dressGroup().getChildren().add(dressMeshView);
        final var eyesGroup   = new Group(pupilsMeshView, eyeballsMeshView);
        final var facingGroup = new Group(view3D.dressGroup(), eyesGroup);

        view3D.root().getChildren().add(facingGroup);

        facingGroup.getTransforms().addAll(view3D.facingRotate(), PacManMeshes3D.ORIENTATION_ADJUSTMENT);

        // Center meshes
        final Bounds db = dressMeshView.getBoundsInLocal();
        final var centering = new Translate(-db.getCenterX(), -db.getCenterY(), -db.getCenterZ());
        dressMeshView.getTransforms().add(centering);
        eyesGroup.getTransforms().add(centering);

        // Scaling of root node
        final float size = settings.size3D();
        view3D.root().getTransforms().add(new Scale(size / db.getWidth(), size / db.getHeight(), size / db.getDepth()));

        dressMeshView   .drawModeProperty().bind(view3D.drawModeProperty());
        pupilsMeshView  .drawModeProperty().bind(view3D.drawModeProperty());
        eyeballsMeshView.drawModeProperty().bind(view3D.drawModeProperty());

        return view3D;
    }

    private void setNormalAppearance(GhostView3D view3D) {
        view3D.dressMeshView()   .setVisible(true);
        view3D.eyeballsMeshView().setVisible(true);
        view3D.pupilsMeshView()  .setVisible(true);

        setSkin(view3D, view3D.appearanceMaterialSet().normalMaterials());
    }

    private void setFrightenedAppearance(GhostView3D view3D) {
        view3D.dressMeshView()   .setVisible(true);
        view3D.eyeballsMeshView().setVisible(true);
        view3D.pupilsMeshView()  .setVisible(true);

        setSkin(view3D, view3D.appearanceMaterialSet().frightenedMaterials());
    }

    private void setEyesOnlyAppearance(GhostView3D view3D) {
        view3D.dressMeshView()   .setVisible(false);
        view3D.eyeballsMeshView().setVisible(true);
        view3D.pupilsMeshView()  .setVisible(true);

        setSkin(view3D, view3D.appearanceMaterialSet().normalMaterials());
    }

    private void setSkin(GhostView3D view3D, GhostView3DPartsMaterials materials) {
        view3D.dressMeshView()   .setMaterial(materials.dressMaterial());
        view3D.pupilsMeshView()  .setMaterial(materials.pupilsMaterial());
        view3D.eyeballsMeshView().setMaterial(materials.eyeballsMaterial());
    }

    private GhostAppearance frightenedAppearance(GhostStateComp state) {
        return state.isPacPowerFading() ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
    }

    private void setAppearance(GhostView3D view3D, Ghost ghost, GhostAppearance appearance) {
        final GhostView3DAnimations animation3D = ghost.assertComponent(GhostView3DAnimations.class);

        switch (appearance) {
            case EATEN, EYES -> {
                setEyesOnlyAppearance(view3D);
                animation3D.lookEyesOnly();
            }
            case FLASHING -> {
                setFrightenedAppearance(view3D);
                animation3D.flashing().playOrContinue();
            }
            case FRIGHTENED -> {
                setFrightenedAppearance(view3D);
                animation3D.lookFrightened();
            }
            case NORMAL -> {
                setNormalAppearance(view3D);
                animation3D.lookNormal();
            }
        }
    }
}