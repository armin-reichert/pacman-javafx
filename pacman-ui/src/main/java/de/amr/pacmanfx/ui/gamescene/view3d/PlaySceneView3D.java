/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.view3d;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.engine.input.Keyboard;
import de.amr.pacmanfx.ui.gamescene.view3d.camera.DronePerspective;
import de.amr.pacmanfx.ui.gamescene.view3d.camera.PerspectiveManager;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;

public class PlaySceneView3D implements Disposable {

    private final Group root;
    private final Group level3DHolder;
    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final AmbientLight ambientLight;
    private final CoordinateSystem coordinateSystem;
    private final DoubleProperty opacity = new SimpleDoubleProperty(0);
    private final PerspectiveManager perspectiveManager;

    public PlaySceneView3D() {
        root = new Group();
        camera = new PerspectiveCamera(true);
        ambientLight = new AmbientLight();
        coordinateSystem = new CoordinateSystem();
        level3DHolder = new Group();

        root.getChildren().addAll(level3DHolder, coordinateSystem, ambientLight);

        subScene = new SubScene(root, 88, 88, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        perspectiveManager = new PerspectiveManager(camera);
    }

    public void handleScrollEvent(ScrollEvent e) {
        perspectiveManager.currentPerspective().ifPresent(perspective -> {
            if (perspective instanceof DronePerspective dronePerspective) {
                dronePerspective.handleScrollEvent(e);
            }
        });
    }

    // Handle CTRL-PLUS, CTRL_MINUS and CTRL-0 if drone perspective is active
    public void handleKeyboardInput(Keyboard keyboard) {
        perspectiveManager.currentPerspective().ifPresent(perspective -> {
            if (perspective instanceof DronePerspective dronePerspective) {
                dronePerspective.handleKeyPressed(keyboard);
            }
        });
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

    public DoubleProperty opacityProperty() {
        return opacity;
    }

    public PerspectiveManager perspectiveManager() {
        return perspectiveManager;
    }

    @Override
    public void dispose() {
        perspectiveManager.dispose();
    }
}
