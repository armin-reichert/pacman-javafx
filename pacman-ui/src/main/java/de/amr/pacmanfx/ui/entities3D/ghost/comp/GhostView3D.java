package de.amr.pacmanfx.ui.entities3D.ghost.comp;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

import static java.util.Objects.requireNonNull;

public class GhostView3D {

    private final ObjectProperty<DrawMode> drawMode = new SimpleObjectProperty<>(DrawMode.FILL);

    private Group root;

    private Group dressGroup;

    private GhostAppearanceMaterialSet appearanceMaterialSet;

    private MeshView dressMeshView;
    
    private MeshView pupilsMeshView;
    
    private MeshView eyeballsMeshView;
    
    private final Rotate facingRotate = new Rotate(0, Rotate.Z_AXIS);

    private GhostAppearance appearance;

    public GhostView3D() {}

    public ObjectProperty<DrawMode> drawModeProperty() {
        return drawMode;
    }

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

    public void setDressMeshView(MeshView dressMeshView) {
        this.dressMeshView = dressMeshView;
    }

    public void setPupilsMeshView(MeshView pupilsMeshView) {
        this.pupilsMeshView = pupilsMeshView;
    }

    public void setEyeballsMeshView(MeshView eyeballsMeshView) {
        this.eyeballsMeshView = eyeballsMeshView;
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

    public void setRoot(Group root) {
        this.root = root;
    }

    public void setDressGroup(Group dressGroup) {
        this.dressGroup = dressGroup;
    }
}
