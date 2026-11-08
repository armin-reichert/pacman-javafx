/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.Disposable;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.pacmanfx.engine.input.Keyboard;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.PlaySceneView3D_FadeInAnimation;
import de.amr.pacmanfx.ui.gamescene.view3d.camera.DronePerspective;
import de.amr.pacmanfx.ui.gamescene.view3d.camera.PerspectiveManager;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

public class PlaySceneView3D implements Disposable {

    private final Group root;
    private final Group level3DHolder;
    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final AmbientLight ambientLight;
    private final CoordinateSystem coordinateSystem;
    private final DoubleProperty opacity = new SimpleDoubleProperty(0);

    private PerspectiveManager perspectiveManager;

    private AnimationRegistry animationRegistry;
    private PlaySceneView3DAnimationSystem animationSystem;
    private PlaySceneView3D_FadeInAnimation fadeInAnimation;

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

        animationRegistry = new AnimationRegistry();
        animationSystem = new PlaySceneView3DAnimationSystem(animationRegistry);

        fadeInAnimation = new PlaySceneView3D_FadeInAnimation(Duration.seconds(3), this);
        animationRegistry.register(fadeInAnimation.name(), fadeInAnimation);
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

    public AnimationRegistry animationRegistry() {
        return animationRegistry;
    }

    public PlaySceneView3DAnimationSystem animationSystem() {
        return animationSystem;
    }

    @Override
    public void dispose() {
        if (perspectiveManager != null) {
            perspectiveManager.dispose();
            perspectiveManager = null;
        }
        if (fadeInAnimation != null) {
            fadeInAnimation.dispose();
            fadeInAnimation = null;
        }
        if (animationRegistry != null) {
            animationRegistry.dispose();
            animationRegistry = null;
        }
        if (animationSystem != null) {
            animationSystem.dispose();
            animationSystem = null;
        }
    }
}
