package de.amr.pacmanfx.uilib.entities3D.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.PacMan3DModel;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostAppearance;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostMaterialSet;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostSettings;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class Ghost3DViewComp implements GameEntityComponent {

    // Root node containing all variants
    private final Group root = new Group();

    private AnimationRegistry animations;

    private GhostMaterialSet materialSet;

    private Group dressGroup;

    private MeshView dressMeshView;

    private MeshView pupilsMeshView;

    private MeshView eyeballsMeshView;

    private final Rotate facingRotate = new Rotate(0, Rotate.Z_AXIS);

    private GhostAppearance activeVariant;

    public Ghost3DViewComp() {
    }

    public void build(GhostSettings settings, Mesh dressMesh, Mesh pupilsMesh, Mesh eyeballsMesh) {
        buildHierarchy(settings, dressMesh, pupilsMesh, eyeballsMesh);
    }

    @Override
    public void reset() {}

    public Group root() {
        return root;
    }

    public GhostAppearance activeVariant() {
        return activeVariant;
    }

    public void setActiveVariant(GhostAppearance activeVariant) {
        this.activeVariant = activeVariant;
    }

    public AnimationRegistry animations() {
        return animations;
    }

    public void setAnimations(AnimationRegistry animations) {
        this.animations = animations;
    }

    public GhostMaterialSet materialSet() {
        return materialSet;
    }

    public void setMaterialSet(GhostMaterialSet materialSet) {
        this.materialSet = materialSet;
    }

    public Group dressGroup() {
        return dressGroup;
    }

    public MeshView dressMeshView() {
        return dressMeshView;
    }

    public MeshView pupilsMeshView() {
        return pupilsMeshView;
    }

    public MeshView eyeballsMeshView() {
        return eyeballsMeshView;
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
    private void buildHierarchy(GhostSettings settings, Mesh dressMesh, Mesh pupilsMesh, Mesh eyeballsMesh) {

        // 1. Create meshes
        dressMeshView    = new MeshView(dressMesh);
        pupilsMeshView   = new MeshView(pupilsMesh);
        eyeballsMeshView = new MeshView(eyeballsMesh);

        // 2. Create groups
        dressGroup = new Group(dressMeshView);

        final Group eyesGroup = new Group(pupilsMeshView, eyeballsMeshView);
        final Group facingGroup = new Group(dressGroup, eyesGroup);

        // 3. Apply transforms to the correct groups
        facingGroup.getTransforms().addAll(
            facingRotate,
            PacMan3DModel.ORIENTATION_ADJUSTMENT
        );

        // 4. Center meshes
        final Bounds dressBounds = dressMeshView.getBoundsInLocal();
        final Translate center = new Translate(
            -dressBounds.getCenterX(),
            -dressBounds.getCenterY(),
            -dressBounds.getCenterZ()
        );
        dressMeshView.getTransforms().add(center);
        eyesGroup.getTransforms().add(center);

        // 5. Add scaling to the root node
        final Scale scaling = new Scale(
            settings.size3D() / dressBounds.getWidth(),
            settings.size3D() / dressBounds.getHeight(),
            settings.size3D() / dressBounds.getDepth());
        root.getTransforms().add(scaling);

        // 6. Add the facing group as the only child
        root.getChildren().setAll(facingGroup);

        // 7. Bind draw mode
//        dressMeshView.drawModeProperty().bind(drawMode);
//        pupilsMeshView.drawModeProperty().bind(drawMode);
//        eyeballsMeshView.drawModeProperty().bind(drawMode);
    }

}
