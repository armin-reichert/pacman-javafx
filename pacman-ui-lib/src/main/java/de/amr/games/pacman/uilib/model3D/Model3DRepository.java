/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib.model3D;

import de.amr.games.pacman.uilib.assets.ResourceManager;
import javafx.scene.shape.Mesh;
import org.tinylog.Logger;

public final class Model3DRepository {

    private static Model3DRepository instance;

    public static Model3DRepository instance() {
        if (instance == null) {
            instance = new Model3DRepository();
            Logger.info("3D models loaded");
        }
        return instance;
    }

    private Model3D pacManModel;
    private Model3D ghostModel;
    private Model3D pelletModel;

    public Model3DRepository() {
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
    public Mesh pelletMesh() { return pelletModel.mesh("Fruit"); }
}
