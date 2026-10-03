/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.scene.*;

public class GameScene3DComp {

    private final Group root;
    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final AmbientLight ambientLight;
    private final CoordinateSystem coordinateSystem;

    public GameScene3DComp() {
        root = new Group();
        camera = new PerspectiveCamera(true);
        ambientLight = new AmbientLight();
        coordinateSystem = new CoordinateSystem();

        root.getChildren().addAll(coordinateSystem, ambientLight);

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

    public AmbientLight ambientLight() {
        return ambientLight;
    }

    public CoordinateSystem coordinateSystem() {
        return coordinateSystem;
    }
}
