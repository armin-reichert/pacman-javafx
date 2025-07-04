/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.tinylog.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.pacmanfx.uilib.Ufx.toCSS_ID;
import static java.util.Objects.requireNonNull;

public final class Model3DRepository {

    public  static final String MODEL_ID_PAC_MAN       = "PacManModel";
    private static final String MESH_ID_PAC_MAN_EYES   = "PacMan.Eyes";
    private static final String MESH_ID_PAC_MAN_HEAD   = "PacMan.Head";
    private static final String MESH_ID_PAC_MAN_PALATE = "PacMan.Palate";

    public  static final String MODEL_ID_GHOST         = "GhostModel";
    private static final String MESH_ID_GHOST_DRESS    = "Sphere.004_Sphere.034_light_blue_ghost";
    private static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
    private static final String MESH_ID_GHOST_PUPILS   = "Sphere.010_Sphere.039_grey_wall";

    public  static final String MODEL_ID_PELLET        = "PelletModel";
    private static final String MESH_ID_PELLET         = "Pellet";

    private static final Map<String, String> OBJ_FILE_PATHS = Map.of(
        MODEL_ID_PAC_MAN, "/de/amr/pacmanfx/uilib/model3D/pacman.obj",
        MODEL_ID_GHOST,   "/de/amr/pacmanfx/uilib/model3D/ghost.obj",
        MODEL_ID_PELLET,  "/de/amr/pacmanfx/uilib/model3D/pellet.obj"
    );

    private final Map<String, Model3D> model3DMap = new HashMap<>();

    /**
     * @param id one of {@link #MODEL_ID_PAC_MAN}, {@link #MODEL_ID_GHOST}, {@link #MODEL_ID_PELLET}.
     * @return meshes and materials of loaded OBJ file
     */
    public Model3D model3D(String id) {
        requireNonNull(id);
        if (!OBJ_FILE_PATHS.containsKey(id)) {
            throw new IllegalArgumentException("Illegal 3D model ID %s".formatted(id));
        }
        if (!model3DMap.containsKey(id)) {
            model3DMap.put(id, loadModel(OBJ_FILE_PATHS.get(id)));
        }
        return model3DMap.get(id);
    }

    private Model3D loadModel(String path) {
        ResourceManager rm = () -> Model3DRepository.class;
        URL url = rm.url(path);
        try {
            if (url != null) {
                Model3D model3D = new Model3D(url);
                Logger.info("3D model loaded from URL {}", url);
                return model3D;
            }
            throw new IllegalArgumentException("Could not access resource at path %s".formatted(path));
        } catch (Exception x) {
            throw new IllegalArgumentException("Could not load 3D model from URL %s".formatted(url));
        }
    }

    public Mesh pacEyesMesh()       { return model3D(MODEL_ID_PAC_MAN).mesh(MESH_ID_PAC_MAN_EYES); }
    public Mesh pacHeadMesh()       { return model3D(MODEL_ID_PAC_MAN).mesh(MESH_ID_PAC_MAN_HEAD); }
    public Mesh pacPalateMesh()     { return model3D(MODEL_ID_PAC_MAN).mesh(MESH_ID_PAC_MAN_PALATE); }

    public Mesh ghostDressMesh()    { return model3D(MODEL_ID_GHOST).mesh(MESH_ID_GHOST_DRESS); }
    public Mesh ghostEyeballsMesh() { return model3D(MODEL_ID_GHOST).mesh(MESH_ID_GHOST_EYEBALLS); }
    public Mesh ghostPupilsMesh()   { return model3D(MODEL_ID_GHOST).mesh(MESH_ID_GHOST_PUPILS); }

    public Mesh pelletMesh() { return model3D(MODEL_ID_PELLET).mesh(MESH_ID_PELLET); }

    public Group createPacMan(double size, Color headColor, Color eyesColor, Color palateColor) {
        var headMeshView = new MeshView(pacHeadMesh());
        headMeshView.setMaterial(Ufx.coloredPhongMaterial(headColor));

        var eyesMeshView = new MeshView(pacEyesMesh());
        eyesMeshView.setMaterial(Ufx.coloredPhongMaterial(eyesColor));

        var palateMeshView = new MeshView(pacPalateMesh());
        palateMeshView.setMaterial(Ufx.coloredPhongMaterial(palateColor));

        var root = new Group(headMeshView, eyesMeshView, palateMeshView);

        var bounds = headMeshView.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
        Stream.of(headMeshView, eyesMeshView, palateMeshView).forEach(node -> node.getTransforms().add(centeredOverOrigin));

        // TODO check/fix Pac-Man mesh position and rotation in OBJ file
        root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        var rootBounds = root.getBoundsInLocal();
        root.getTransforms().add(new Scale(size / rootBounds.getWidth(), size / rootBounds.getHeight(), size / rootBounds.getDepth()));

        return root;
    }

    public Group createPacManWithoutEyes(double size, Color headColor, Color palateColor) {
        var headMeshView = new MeshView(pacHeadMesh());
        headMeshView.setMaterial(Ufx.coloredPhongMaterial(headColor));

        var palateMeshView = new MeshView(pacPalateMesh());
        palateMeshView.setMaterial(Ufx.coloredPhongMaterial(palateColor));

        var bounds = headMeshView.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
        Stream.of(headMeshView, palateMeshView).forEach(node -> node.getTransforms().add(centeredOverOrigin));

        var root = new Group(headMeshView, palateMeshView);

        // TODO check/fix Pac-Man mesh position and rotation in OBJ file
        root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

        var rootBounds = root.getBoundsInLocal();
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

    public Group createGhost(double size, Color dressColor, double rotate) {
        MeshView dress = new MeshView(model3D(MODEL_ID_GHOST).mesh(MESH_ID_GHOST_DRESS));
        dress.setMaterial(Ufx.coloredPhongMaterial(dressColor));
        Bounds dressBounds = dress.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());
        dress.getTransforms().add(centeredOverOrigin);

        MeshView pupils = new MeshView(model3D(MODEL_ID_GHOST).mesh(MESH_ID_GHOST_PUPILS));
        pupils.setMaterial(Ufx.coloredPhongMaterial(Color.BLUE));

        MeshView eyeballs = new MeshView(model3D(MODEL_ID_GHOST).mesh(MESH_ID_GHOST_EYEBALLS));
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

    public Group createMsPacMan(
        double size,
        Color headColor, Color eyesColor, Color palateColor,
        Color hairBowColor, Color hairBowPearlsColor, Color boobsColor)
    {
        return new Group(
            createPacMan(size, headColor, eyesColor, palateColor),
            createFemaleBodyParts(size, hairBowColor, hairBowPearlsColor, boobsColor)
        );
    }
}