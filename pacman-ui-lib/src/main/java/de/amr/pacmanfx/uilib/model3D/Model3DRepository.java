/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.uilib.Ufx.toCSS_ID;
import static java.util.Objects.requireNonNull;

public final class Model3DRepository {

    public static final String PAC_MAN_MODEL = "PacMan";
    public static final String GHOST_MODEL   = "Ghost";
    public static final String PELLET_MODEL  = "Pellet";

    private static final String PAC_MAN_MESH_ID_EYES   = "PacMan.Eyes";
    private static final String PAC_MAN_MESH_ID_HEAD   = "PacMan.Head";
    private static final String PAC_MAN_MESH_ID_PALATE = "PacMan.Palate";

    private static final String GHOST_MESH_ID_DRESS    = "Sphere.004_Sphere.034_light_blue_ghost";
    private static final String GHOST_MESH_ID_PUPILS   = "Sphere.010_Sphere.039_grey_wall";
    private static final String GHOST_MESH_ID_EYEBALLS = "Sphere.009_Sphere.036_white";

    public static final String PELLET_MESH_ID          = "Pellet";

    private final Map<String, Model3D> modelMap = new HashMap<>();

    public Model3DRepository() {}

    public Model3D getModel(String modelID) {
        requireNonNull(modelID);
        return switch (modelID) {
            case PAC_MAN_MODEL -> {
                if (!modelMap.containsKey(modelID)) {
                    modelMap.put(modelID, loadModel("/de/amr/pacmanfx/uilib/model3D/pacman.obj"));
                }
                yield modelMap.get(modelID);
            }
            case GHOST_MODEL -> {
                if (!modelMap.containsKey(modelID)) {
                    modelMap.put(modelID, loadModel("/de/amr/pacmanfx/uilib/model3D/ghost.obj"));
                }
                yield modelMap.get(modelID);
            }
            case PELLET_MODEL -> {
                if (!modelMap.containsKey(modelID)) {
                    modelMap.put(modelID, loadModel("/de/amr/pacmanfx/uilib/model3D/pellet.obj"));
                }
                yield modelMap.get(modelID);
            }
            default -> throw new IllegalArgumentException("Illegal 3D model ID %s".formatted(modelID));
        };
    }

    private Model3D loadModel(String path) {
        ResourceManager rm = () -> Model3DRepository.class;
        try {
            return new Model3D(rm.url(path));
        } catch (Exception x) {
            throw new IllegalArgumentException("Could not load 3D model from resource path %s".formatted(path));
        }
    }

    public Mesh ghostDressMesh()   { return getModel(GHOST_MODEL).mesh("Sphere.004_Sphere.034_light_blue_ghost"); }
    public Mesh ghostPupilsMesh()  { return getModel(GHOST_MODEL).mesh("Sphere.010_Sphere.039_grey_wall"); }
    public Mesh ghostEyeballsMesh() { return getModel(GHOST_MODEL).mesh("Sphere.009_Sphere.036_white"); }

    public Mesh pelletMesh() { return getModel(PELLET_MODEL).mesh(PELLET_MESH_ID); }

    public Group createPacShape(double size, Color headColor, Color eyesColor, Color palateColor) {
        var head = new MeshView(getModel(PAC_MAN_MODEL).mesh(PAC_MAN_MESH_ID_HEAD));
        head.setId(toCSS_ID(PAC_MAN_MESH_ID_HEAD));
        head.setMaterial(Ufx.coloredPhongMaterial(headColor));

        var eyes = new MeshView(getModel(PAC_MAN_MODEL).mesh(PAC_MAN_MESH_ID_EYES));
        eyes.setId(toCSS_ID(PAC_MAN_MESH_ID_EYES));
        eyes.setMaterial(Ufx.coloredPhongMaterial(eyesColor));

        var palate = new MeshView(getModel(PAC_MAN_MODEL).mesh(PAC_MAN_MESH_ID_PALATE));
        palate.setId(toCSS_ID(PAC_MAN_MESH_ID_PALATE));
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
        var head = new MeshView(getModel(PAC_MAN_MODEL).mesh(PAC_MAN_MESH_ID_HEAD));
        head.setId(toCSS_ID(PAC_MAN_MESH_ID_HEAD));
        head.setMaterial(Ufx.coloredPhongMaterial(headColor));

        var palate = new MeshView(getModel(PAC_MAN_MODEL).mesh(PAC_MAN_MESH_ID_PALATE));
        palate.setId(toCSS_ID(PAC_MAN_MESH_ID_PALATE));
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
        MeshView dress = new MeshView(getModel(GHOST_MODEL).mesh(GHOST_MESH_ID_DRESS));
        dress.setMaterial(Ufx.coloredPhongMaterial(dressColor));
        Bounds dressBounds = dress.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());
        dress.getTransforms().add(centeredOverOrigin);

        MeshView pupils = new MeshView(getModel(GHOST_MODEL).mesh(GHOST_MESH_ID_PUPILS));
        pupils.setMaterial(Ufx.coloredPhongMaterial(Color.BLUE));

        MeshView eyeballs = new MeshView(getModel(GHOST_MODEL).mesh(GHOST_MESH_ID_EYEBALLS));
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
