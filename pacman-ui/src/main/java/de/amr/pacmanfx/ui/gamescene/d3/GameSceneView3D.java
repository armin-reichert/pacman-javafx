/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.scene.*;

public class GameSceneView3D {

    private final Group root;
    private final Group level3DHolder;
    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final AmbientLight ambientLight;
    private final CoordinateSystem coordinateSystem;

    public GameSceneView3D() {
        root = new Group();
        camera = new PerspectiveCamera(true);
        ambientLight = new AmbientLight();
        coordinateSystem = new CoordinateSystem();
        level3DHolder = new Group();

        root.getChildren().addAll(level3DHolder, coordinateSystem, ambientLight);

        subScene = new SubScene(root, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
    }

    public PerspectiveCamera camera() {
        return camera;
    }

    public SubScene subScene() {
        return subScene;
    }

    public Group root() {
        return root;
    }

    public Group level3DHolder() {
        return level3DHolder;
    }

    public AmbientLight ambientLight() {
        return ambientLight;
    }

    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }
}
