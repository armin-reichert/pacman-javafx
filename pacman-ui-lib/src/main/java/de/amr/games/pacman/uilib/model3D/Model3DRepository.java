/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib.model3D;

import de.amr.games.pacman.uilib.Ufx;
import de.amr.games.pacman.uilib.assets.ResourceManager;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.tinylog.Logger;

import java.util.stream.Stream;

public final class Model3DRepository {

    private static final String PAC_MAN_MESH_ID_EYES   = "PacMan.Eyes";
    private static final String PAC_MAN_MESH_ID_HEAD   = "PacMan.Head";
    private static final String PAC_MAN_MESH_ID_PALATE = "PacMan.Palate";

    private static final String GHOST_MESH_ID_DRESS    = "Sphere.004_Sphere.034_light_blue_ghost";
    private static final String GHOST_MESH_ID_PUPILS   = "Sphere.010_Sphere.039_grey_wall";
    private static final String GHOST_MESH_ID_EYEBALLS = "Sphere.009_Sphere.036_white";

    public static final String PELLET_MESH_ID          = "Fruit";

    private static Model3DRepository instance;

    public static Model3DRepository get() {
        if (instance == null) {
            instance = new Model3DRepository();
            Logger.info("3D model repository loaded");
        }
        return instance;
    }

    private Model3D pacManModel;
    private Model3D ghostModel;
    private Model3D pelletModel;

    private Model3DRepository() {
        ResourceManager rm = () -> Model3DRepository.class;
        try {
            pacManModel = new Model3D(rm.url("/de/amr/games/pacman/uilib/model3D/pacman.obj"));
            ghostModel = new Model3D(rm.url("/de/amr/games/pacman/uilib/model3D/ghost.obj"));
            pelletModel = new Model3D(rm.url("/de/amr/games/pacman/uilib/model3D/fruit.obj"));
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("3D models could not be loaded properly");
        }
    }

    public Model3D pacManModel3D() { return pacManModel; }

    public Model3D ghostModel3D()  { return ghostModel; }
    public Mesh ghostDressMesh()   { return ghostModel.mesh("Sphere.004_Sphere.034_light_blue_ghost"); }
    public Mesh ghostPupilsMesh()  { return ghostModel.mesh("Sphere.010_Sphere.039_grey_wall"); }
    public Mesh ghostEyeballsMesh() { return ghostModel.mesh("Sphere.009_Sphere.036_white"); }

    public Model3D pelletModel3D() { return pelletModel; }
    public Mesh pelletMesh() { return pelletModel.mesh(PELLET_MESH_ID); }


    public Group createPacShape(double size, Color headColor, Color eyesColor, Color palateColor) {
        var head = new MeshView(pacManModel.mesh(PAC_MAN_MESH_ID_HEAD));
        head.setId(Model3D.toCSS_ID(PAC_MAN_MESH_ID_HEAD));
        head.setMaterial(Ufx.coloredPhongMaterial(headColor));

        var eyes = new MeshView(pacManModel.mesh(PAC_MAN_MESH_ID_EYES));
        eyes.setId(Model3D.toCSS_ID(PAC_MAN_MESH_ID_EYES));
        eyes.setMaterial(Ufx.coloredPhongMaterial(eyesColor));

        var palate = new MeshView(pacManModel.mesh(PAC_MAN_MESH_ID_PALATE));
        palate.setId(Model3D.toCSS_ID(PAC_MAN_MESH_ID_PALATE));
        palate.setMaterial(Ufx.coloredPhongMaterial(palateColor));

        var root = new Group(head, eyes, palate);

        var bounds = head.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
        Stream.of(head, eyes, palate).forEach(node -> node.getTransforms().add(centeredOverOrigin));

        // TODO check/fix Pac-Man mesh position and rotation in OBJ file
        root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        var rootBounds = root.getBoundsInLocal();
        root.getTransforms().add(new Scale(size / rootBounds.getWidth(), size / rootBounds.getHeight(), size / rootBounds.getDepth()));

        return root;
    }

    public Group createPacSkull(double size, Color headColor, Color palateColor) {
        var head = new MeshView(pacManModel.mesh(PAC_MAN_MESH_ID_HEAD));
        head.setId(Model3D.toCSS_ID(PAC_MAN_MESH_ID_HEAD));
        head.setMaterial(Ufx.coloredPhongMaterial(headColor));

        var palate = new MeshView(pacManModel.mesh(PAC_MAN_MESH_ID_PALATE));
        palate.setId(Model3D.toCSS_ID(PAC_MAN_MESH_ID_PALATE));
        palate.setMaterial(Ufx.coloredPhongMaterial(palateColor));

        Bounds bounds = head.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
        head.getTransforms().add(centeredOverOrigin);
        palate.getTransforms().add(centeredOverOrigin);

        var root = new Group(head, palate);

        Bounds rootBounds = root.getBoundsInLocal();
        // TODO check/fix Pac-Man mesh position and rotation in .obj file
        root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
        root.getTransforms().add(new Scale(size / rootBounds.getWidth(), size / rootBounds.getHeight(), size / rootBounds.getDepth()));

        return root;
    }

    public Group createFemaleBodyParts(double pacSize, Color hairBowColor, Color hairBowPearlsColor, Color boobsColor) {
        var bowMaterial = Ufx.coloredPhongMaterial(hairBowColor);
        int divisions = 16; // 64 is default

        var bowLeft = new Sphere(1.2, divisions);
        bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -pacSize * 0.55));
        bowLeft.setMaterial(bowMaterial);

        var bowRight = new Sphere(1.2, divisions);
        bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -pacSize * 0.55));
        bowRight.setMaterial(bowMaterial);

        var pearlMaterial = Ufx.coloredPhongMaterial(hairBowPearlsColor);

        var pearlLeft = new Sphere(0.4, divisions);
        pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -pacSize * 0.58));
        pearlLeft.setMaterial(pearlMaterial);

        var pearlRight = new Sphere(0.4, divisions);
        pearlRight.getTransforms().addAll(new Translate(2, -0.5, -pacSize * 0.58));
        pearlRight.setMaterial(pearlMaterial);

        var beautySpot = new Sphere(0.5, divisions);
        beautySpot.getTransforms().addAll(new Translate(-0.33 * pacSize, -0.4 * pacSize, -0.14 * pacSize));
        beautySpot.setMaterial(Ufx.coloredPhongMaterial(Color.rgb(120, 120, 120)));

        var silicone = Ufx.coloredPhongMaterial(boobsColor);

        double bx = -0.2 * pacSize; // forward
        double by = 1.6; // or - 1.6 // sidewards
        double bz = 0.4 * pacSize; // up/down
        var boobLeft = new Sphere(1.8, divisions);
        boobLeft.setMaterial(silicone);
        boobLeft.getTransforms().addAll(new Translate(bx, -by, bz));

        var boobRight = new Sphere(1.8, divisions);
        boobRight.setMaterial(silicone);
        boobRight.getTransforms().addAll(new Translate(bx, by, bz));

        return new Group(bowLeft, bowRight, pearlLeft, pearlRight, boobLeft, boobRight, beautySpot);
    }

    public Node createGhostShape3D(double size, Color dressColor, double rotate) {
        MeshView dress = new MeshView(ghostModel.mesh(GHOST_MESH_ID_DRESS));
        dress.setMaterial(Ufx.coloredPhongMaterial(dressColor));
        Bounds dressBounds = dress.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());
        dress.getTransforms().add(centeredOverOrigin);

        MeshView pupils = new MeshView(ghostModel.mesh(GHOST_MESH_ID_PUPILS));
        pupils.setMaterial(Ufx.coloredPhongMaterial(Color.BLUE));

        MeshView eyeballs = new MeshView(ghostModel.mesh(GHOST_MESH_ID_EYEBALLS));
        eyeballs.setMaterial(Ufx.coloredPhongMaterial(Color.WHITE));
        var eyesGroup = new Group(pupils, eyeballs);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        var dressGroup = new Group(dress);

        Group root = new Group(dressGroup, eyesGroup);
        root.getTransforms().add(new Rotate(270, Rotate.X_AXIS));
        root.getTransforms().add(new Rotate(rotate, Rotate.Y_AXIS));
        Bounds b = root.getBoundsInLocal();
        root.getTransforms().add(new Scale(size / b.getWidth(), size / b.getHeight(), size / b.getDepth()));

        return root;
    }
}
