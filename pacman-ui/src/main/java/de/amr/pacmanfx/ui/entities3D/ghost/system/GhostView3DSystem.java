package de.amr.pacmanfx.ui.entities3D.ghost.system;

import de.amr.basics.ui.animation.ManagedAnimation;
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

public class GhostView3DSystem {

    public GhostView3DSystem() {}

    public GhostView3D build(GhostSettings settings, Mesh dressMesh, Mesh pupilsMesh, Mesh eyeballsMesh) {
        return buildView3D(settings, dressMesh, pupilsMesh, eyeballsMesh);
    }

    public void update(Ghost ghost) {
        final GhostStateComp state = ghost.state();

        final GhostAppearance appearance = switch (state.enumValue()) {
            case LOCKED -> state.hasPacPower() ? appearFrightenedOrFlashing(state) : GhostAppearance.NORMAL;
            case EATEN -> GhostAppearance.EATEN;
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            case FRIGHTENED -> appearFrightenedOrFlashing(state);
            case HUNTING_PAC, LEAVING_HOUSE -> GhostAppearance.NORMAL;
        };
        setAppearance(ghost, appearance);
    }

    private void lookNormal(GhostView3D ghostView3D) {
        ghostView3D.dressMeshView()   .setVisible(true);
        ghostView3D.eyeballsMeshView().setVisible(true);
        ghostView3D.pupilsMeshView()  .setVisible(true);

        applyMaterials(ghostView3D, ghostView3D.appearanceMaterialSet().normal());
    }

    private void lookFrightened(GhostView3D ghostView3D) {
        ghostView3D.dressMeshView()   .setVisible(true);
        ghostView3D.eyeballsMeshView().setVisible(true);
        ghostView3D.pupilsMeshView()  .setVisible(true);

        applyMaterials(ghostView3D, ghostView3D.appearanceMaterialSet().frightened());
    }

    private void lookEyesOnly(GhostView3D ghostView3D) {
        ghostView3D.dressMeshView()   .setVisible(false);
        ghostView3D.eyeballsMeshView().setVisible(true);
        ghostView3D.pupilsMeshView()  .setVisible(true);

        applyMaterials(ghostView3D, ghostView3D.appearanceMaterialSet().normal());
    }

    private void applyMaterials(GhostView3D ghostView3D, Ghost3DMaterialSet materials) {
        ghostView3D.dressMeshView()   .setMaterial(materials.dress());
        ghostView3D.pupilsMeshView()  .setMaterial(materials.pupils());
        ghostView3D.eyeballsMeshView().setMaterial(materials.eyeballs());
    }

    private GhostAppearance appearFrightenedOrFlashing(GhostStateComp state) {
        return state.isPacPowerFading() ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
    }

    private void setAppearance(Ghost ghost, GhostAppearance appearance) {
        final GhostView3D view3D = ghost.assertComponent(GhostView3D.class);
        final Ghost3DAnimationComp animation3D = ghost.assertComponent(Ghost3DAnimationComp.class);

        view3D.setAppearance(appearance);
        switch (appearance) {
            case EATEN, EYES -> {
                lookEyesOnly(view3D);
                animation3D.lookEyesOnly();
            }
            case FLASHING -> {
                lookFrightened(view3D);
                ensureFlashingPlays(animation3D.flashing());
            }
            case FRIGHTENED -> {
                lookFrightened(view3D);
                animation3D.lookFrightened();
            }
            case NORMAL -> {
                lookNormal(view3D);
                animation3D.lookNormal();
            }
        }
    }

    private void ensureFlashingPlays(ManagedAnimation flashing) {
        if (!flashing.isRunning()) {
            flashing.playOrContinue();
        }
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
    private GhostView3D buildView3D(GhostSettings settings, Mesh dressMesh, Mesh pupilsMesh, Mesh eyeballsMesh) {
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
}