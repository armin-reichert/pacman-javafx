package de.amr.pacmanfx.uilib.entities3D.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.PacMan3DModel;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostMaterialSet;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostSettings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import static java.util.Objects.requireNonNull;

public class Ghost3DViewComp implements GameEntityComponent {

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);

    private final Group root = new Group();

    private GhostMaterialSet materialSet;

    private MeshView dressMeshView;
    
    private MeshView pupilsMeshView;
    
    private MeshView eyeballsMeshView;
    
    private final Rotate facingRotate = new Rotate(0, Rotate.Z_AXIS);

    private GhostAppearance appearance;

    public Ghost3DViewComp() {}

    public void build(GhostSettings settings, Mesh dressMesh, Mesh pupilsMesh, Mesh eyeballsMesh) {
        buildTree(settings, dressMesh, pupilsMesh, eyeballsMesh);
    }

    @Override
    public void reset() {}

    public Group root() {
        return root;
    }

    public GhostAppearance appearance() {
        return appearance;
    }

    public void setAppearance(GhostAppearance appearance) {
        this.appearance = requireNonNull(appearance);
    }

    public Rotate facingRotate() {
        return facingRotate;
    }

    public void setMaterialSet(GhostMaterialSet materialSet) {
        this.materialSet = requireNonNull(materialSet);
    }

    public GhostMaterialSet materialSet() {
        return materialSet;
    }

    // Private Area, no trespassing!

    /*
        root (tf: scaling)
           facingGroup (tf: facing-rotate, model-orientation-adjustment)
              dressGroup (tf: dress-rotation-animation)
                 dressMeshView (tf: centering)
              eyesGroup (tf: centering)
                 pupilsMeshView
                 eyeballsMeshView
     */
    private void buildTree(GhostSettings settings, Mesh dressMesh, Mesh pupilsMesh, Mesh eyeballsMesh) {
        dressMeshView    = new MeshView(dressMesh);
        pupilsMeshView   = new MeshView(pupilsMesh);
        eyeballsMeshView = new MeshView(eyeballsMesh);

        final var dressGroup  = new Group(dressMeshView);
        final var eyesGroup   = new Group(pupilsMeshView, eyeballsMeshView);
        final var facingGroup = new Group(dressGroup, eyesGroup);

        root.getChildren().add(facingGroup);

        facingGroup.getTransforms().addAll(facingRotate, PacMan3DModel.ORIENTATION_ADJUSTMENT);

        // Center meshes
        final Bounds db = dressMeshView.getBoundsInLocal();
        final var centering = new Translate(-db.getCenterX(), -db.getCenterY(), -db.getCenterZ());
        dressMeshView.getTransforms().add(centering);
        eyesGroup.getTransforms().add(centering);

        // Scaling of root node
        final float size = settings.size3D();
        root.getTransforms().add(new Scale(size / db.getWidth(), size / db.getHeight(), size / db.getDepth()));

        dressMeshView   .drawModeProperty().bind(drawMode);
        pupilsMeshView  .drawModeProperty().bind(drawMode);
        eyeballsMeshView.drawModeProperty().bind(drawMode);
    }

    public void lookNormal() {
        root         .setVisible(true);
        dressMeshView.setVisible(true);
        applyMaterials(materialSet.normalMaterial());
    }

    public void lookFlashing() {
        root.setVisible(true);
        lookFrightened();
    }

    public void lookFrightened() {
        root         .setVisible(true);
        dressMeshView.setVisible(true);
        applyMaterials(materialSet.frightenedMaterial());
    }

    public void lookEyesOnly() {
        root            .setVisible(true);
        pupilsMeshView  .setVisible(true);
        eyeballsMeshView.setVisible(true);
        dressMeshView   .setVisible(false);
    }

    public void lookEaten() {
        root().setVisible(false);
    }

    private void applyMaterials(Ghost3DMaterials materials) {
        dressMeshView   .setMaterial(materials.dressMaterial());
        pupilsMeshView  .setMaterial(materials.pupilsMaterial());
        eyeballsMeshView.setMaterial(materials.eyeballsMaterial());
    }
}
