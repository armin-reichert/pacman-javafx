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

public class PacManModel3DRepository implements Disposable {

    private static final String MESH_ID_PAC_MAN_EYES   = "PacMan.Eyes";
    private static final String MESH_ID_PAC_MAN_HEAD   = "PacMan.Head";
    private static final String MESH_ID_PAC_MAN_PALATE = "PacMan.Palate";

    private static final String MESH_ID_GHOST_DRESS    = "Sphere.004_Sphere.034_light_blue_ghost";
    private static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
    private static final String MESH_ID_GHOST_PUPILS   = "Sphere.010_Sphere.039_grey_wall";

    private static final String MESH_ID_PELLET         = "Pellet";

    private final Model3D model3D_PacMan;
    private final Model3D model3D_Ghost;
    private final Model3D model3D_Pellet;

    public PacManModel3DRepository() {
        model3D_PacMan = loadModel("/de/amr/pacmanfx/uilib/model3D/pacman.obj");
        model3D_Ghost  = loadModel("/de/amr/pacmanfx/uilib/model3D/ghost.obj");
        model3D_Pellet = loadModel("/de/amr/pacmanfx/uilib/model3D/pellet.obj");
    }

    private Model3D loadModel(String path) {
        ResourceManager rm = () -> PacManModel3DRepository.class;
        URL url = rm.url(path);
        if (url == null) {
            throw new IllegalArgumentException("Could not access model3D with resource path '%s'".formatted(path));
        }
        try {
            Model3D model3D = new Model3D(url);
            Logger.info("3D model loaded from URL '{}'", url);
            return model3D;
        } catch (Exception x) {
            throw new IllegalArgumentException("Could not load 3D model from URL '%s'".formatted(url), x);
        }
    }

    public Mesh pacEyesMesh()       { return model3D_PacMan.mesh(MESH_ID_PAC_MAN_EYES); }
    public Mesh pacHeadMesh()       { return model3D_PacMan.mesh(MESH_ID_PAC_MAN_HEAD); }
    public Mesh pacPalateMesh()     { return model3D_PacMan.mesh(MESH_ID_PAC_MAN_PALATE); }

    public Mesh ghostDressMesh()    { return model3D_Ghost.mesh(MESH_ID_GHOST_DRESS); }
    public Mesh ghostEyeballsMesh() { return model3D_Ghost.mesh(MESH_ID_GHOST_EYEBALLS); }
    public Mesh ghostPupilsMesh()   { return model3D_Ghost.mesh(MESH_ID_GHOST_PUPILS); }

    public Mesh pelletMesh()        { return model3D_Pellet.mesh(MESH_ID_PELLET); }

    public PacBody createPacBody(double size, Color headColor, Color eyesColor, Color palateColor) {
        return new PacBody(size, pacHeadMesh(), headColor, pacEyesMesh(), eyesColor, pacPalateMesh(), palateColor);
    }

    public PacBodyNoEyes createBlindPacBody(double size, Color headColor, Color palateColor) {
        return new PacBodyNoEyes(size, pacHeadMesh(), headColor, pacPalateMesh(), palateColor);
    }

    public MsPacManFemaleParts createFemaleBodyParts(double pacSize, Color hairBowColor, Color hairBowPearlsColor, Color boobsColor) {
        return new MsPacManFemaleParts(pacSize, hairBowColor, hairBowPearlsColor, boobsColor);
    }

    public MsPacManBody createMsPacManBody(double size,
        Color headColor, Color eyesColor, Color palateColor,
        Color hairBowColor, Color hairBowPearlsColor, Color boobsColor)
    {
        var body = createPacBody(size, headColor, eyesColor, palateColor);
        var femaleParts = createFemaleBodyParts(size, hairBowColor, hairBowPearlsColor, boobsColor);
        return new MsPacManBody(body, femaleParts);
    }

    public GhostBody createGhostBody(double size, Color dressColor, double rotateY) {
        return new GhostBody(ghostDressMesh(), ghostPupilsMesh(), ghostEyeballsMesh(), size, dressColor, rotateY);
    }

    @Override
    public void dispose() {
        model3D_PacMan.dispose();
        model3D_Ghost.dispose();
        model3D_Pellet.dispose();
    }
}