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
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.tinylog.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

    public PacBody createPacBody(double size, Color headColor, Color eyesColor, Color palateColor) {
        return new PacBody(this, size, headColor, eyesColor, palateColor);
    }

    public PacBodyNoEyes createBlindPacBody(double size, Color headColor, Color palateColor) {
        return new PacBodyNoEyes(this, size, headColor, palateColor);
    }
    public MsPacManFemaleParts createFemaleBodyParts(double pacSize, Color hairBowColor, Color hairBowPearlsColor, Color boobsColor) {
        return new MsPacManFemaleParts(pacSize, hairBowColor, hairBowPearlsColor, boobsColor);
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
            createPacBody(size, headColor, eyesColor, palateColor),
            createFemaleBodyParts(size, hairBowColor, hairBowPearlsColor, boobsColor)
        );
    }
}