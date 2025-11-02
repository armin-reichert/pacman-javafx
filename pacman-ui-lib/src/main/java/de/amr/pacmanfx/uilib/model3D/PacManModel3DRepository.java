/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;
import org.tinylog.Logger;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class PacManModel3DRepository implements Disposable {

    public enum ModelID {
        PAC_MAN("/de/amr/pacmanfx/uilib/model3D/pacman.obj"),
        GHOST("/de/amr/pacmanfx/uilib/model3D/ghost.obj"),
        PELLET("/de/amr/pacmanfx/uilib/model3D/pellet.obj");

        ModelID(String objFilePath) {
            this.objFilePath = objFilePath;
        }

        public String objFile() {
            return objFilePath;
        }

        private final String objFilePath;
    }

    private static final String MESH_ID_PAC_MAN_EYES   = "PacMan.Eyes";
    private static final String MESH_ID_PAC_MAN_HEAD   = "PacMan.Head";
    private static final String MESH_ID_PAC_MAN_PALATE = "PacMan.Palate";

    private static final String MESH_ID_GHOST_DRESS    = "Sphere.004_Sphere.034_light_blue_ghost";
    private static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
    private static final String MESH_ID_GHOST_PUPILS   = "Sphere.010_Sphere.039_grey_wall";

    private static final String MESH_ID_PELLET         = "Pellet";

    private final Map<ModelID, Model3D> models = new EnumMap<>(ModelID.class);

    public PacManModel3DRepository() {}

    /**
     * @param id one of {@link ModelID#PAC_MAN}, {@link ModelID#GHOST}, {@link ModelID#PELLET}.
     * @return meshes and materials of loaded OBJ file
     */
    public Model3D model3D(ModelID id) {
        requireNonNull(id);
        if (models.get(id) == null) {
            models.put(id, loadModel(id.objFile()));
        }
        return models.get(id);
    }

    private Model3D loadModel(String path) {
        ResourceManager rm = () -> PacManModel3DRepository.class;
        URL url = rm.url(path);
        try {
            if (url != null) {
                Model3D model3D = new Model3D(url);
                Logger.info("3D model loaded from URL {}", url);
                return model3D;
            }
            throw new IllegalArgumentException("Could not access resource at path %s".formatted(path));
        } catch (Exception x) {
            throw new IllegalArgumentException("Could not load 3D model from URL %s".formatted(url), x);
        }
    }

    public Mesh pacEyesMesh()       { return model3D(ModelID.PAC_MAN).mesh(MESH_ID_PAC_MAN_EYES); }
    public Mesh pacHeadMesh()       { return model3D(ModelID.PAC_MAN).mesh(MESH_ID_PAC_MAN_HEAD); }
    public Mesh pacPalateMesh()     { return model3D(ModelID.PAC_MAN).mesh(MESH_ID_PAC_MAN_PALATE); }

    public Mesh ghostDressMesh()    { return model3D(ModelID.GHOST).mesh(MESH_ID_GHOST_DRESS); }
    public Mesh ghostEyeballsMesh() { return model3D(ModelID.GHOST).mesh(MESH_ID_GHOST_EYEBALLS); }
    public Mesh ghostPupilsMesh()   { return model3D(ModelID.GHOST).mesh(MESH_ID_GHOST_PUPILS); }

    public Mesh pelletMesh()        { return model3D(ModelID.PELLET).mesh(MESH_ID_PELLET); }

    public PacBody createPacBody(double size, Color headColor, Color eyesColor, Color palateColor) {
        return new PacBody(this, size, headColor, eyesColor, palateColor);
    }

    public PacBodyNoEyes createBlindPacBody(double size, Color headColor, Color palateColor) {
        return new PacBodyNoEyes(this, size, headColor, palateColor);
    }

    public MsPacManFemaleParts createFemaleBodyParts(double pacSize, Color hairBowColor, Color hairBowPearlsColor, Color boobsColor) {
        return new MsPacManFemaleParts(pacSize, hairBowColor, hairBowPearlsColor, boobsColor);
    }

    public MsPacManBody createMsPacManBody(
            double size,
            Color headColor, Color eyesColor, Color palateColor,
            Color hairBowColor, Color hairBowPearlsColor, Color boobsColor) {
        return new MsPacManBody(this, size, headColor, eyesColor, palateColor, hairBowColor, hairBowPearlsColor, boobsColor);
    }

    public GhostBody createGhostBody(double size, Color dressColor, double rotateY) {
        return new GhostBody(this, size, dressColor, rotateY);
    }

    @Override
    public void dispose() {
        models.values().forEach(Model3D::dispose);
    }
}