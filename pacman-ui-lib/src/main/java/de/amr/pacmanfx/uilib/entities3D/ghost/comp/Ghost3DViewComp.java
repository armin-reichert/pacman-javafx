package de.amr.pacmanfx.uilib.entities3D.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.PacMan3DModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
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

    private final Group dressGroup = new Group();

    private GhostAppearanceMaterialSet appearanceMaterialSet;

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

    public Group dressGroup() {
        return dressGroup;
    }

    public GhostAppearance appearance() {
        return appearance;
    }

    public void setAppearance(GhostAppearance appearance) {
        this.appearance = requireNonNull(appearance);
    }

    public MeshView dressMeshView() {
        return dressMeshView;
    }

    public PhongMaterial dressMaterial() {
        return requirePhongMaterial(dressMeshView);
    }

    public MeshView eyeballsMeshView() {
        return eyeballsMeshView;
    }

    public PhongMaterial eyeballsMaterial() {
        return requirePhongMaterial(eyeballsMeshView);
    }

    public MeshView pupilsMeshView() {
        return pupilsMeshView;
    }

    public PhongMaterial pupilsMaterial() {
        return requirePhongMaterial(pupilsMeshView);
    }

    private PhongMaterial requirePhongMaterial(MeshView meshView) {
        return (PhongMaterial) meshView.getMaterial();
    }

    public Rotate facingRotate() {
        return facingRotate;
    }

    public void setAppearanceMaterialSet(GhostAppearanceMaterialSet appearanceMaterialSet) {
        this.appearanceMaterialSet = requireNonNull(appearanceMaterialSet);
    }

    public GhostAppearanceMaterialSet appearanceMaterialSet() {
        return appearanceMaterialSet;
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

        dressGroup.getChildren().add(dressMeshView);
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
        dressMeshView   .setVisible(true);
        eyeballsMeshView.setVisible(true);
        pupilsMeshView  .setVisible(true);

        applyMaterials(appearanceMaterialSet.normal());
    }

    public void lookFrightened() {
        dressMeshView   .setVisible(true);
        eyeballsMeshView.setVisible(true);
        pupilsMeshView  .setVisible(true);

        applyMaterials(appearanceMaterialSet.frightened());
    }

    public void lookEyesOnly() {
        dressMeshView   .setVisible(false);
        eyeballsMeshView.setVisible(true);
        pupilsMeshView  .setVisible(true);

        applyMaterials(appearanceMaterialSet.normal());
    }

    private void applyMaterials(Ghost3DMaterialSet materials) {
        dressMeshView   .setMaterial(materials.dress());
        pupilsMeshView  .setMaterial(materials.pupils());
        eyeballsMeshView.setMaterial(materials.eyeballs());
    }
}
