/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.playscene;

import de.amr.basics.Disposable;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.engine.input.Keyboard;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.PlaySceneView3D_FadeInAnimation;
import de.amr.pacmanfx.ui.gamescene.view3d.camera.DronePerspective;
import de.amr.pacmanfx.ui.gamescene.view3d.camera.PerspectiveManager;
import de.amr.pacmanfx.ui.viewmodel.Game3DSettingsViewModel;
import de.amr.pacmanfx.uilib.widgets.CoordinateSystem;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

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

    private GameLevelView3D level3D;

    private final Game3DSettingsViewModel settingsViewModel;

    public PlaySceneView3D(Game3DSettingsViewModel settingsViewModel) {
        this.settingsViewModel = requireNonNull(settingsViewModel);

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

    public void removeLevel3D() {
        if (level3D != null) {
            settingsViewModel.drawModeProperty().removeListener(level3D);
            level3D.dispose();
            level3D = null;
        }
    }

    public void createLevel3D(EngineUIContext engine, GameContext game, GameLevel level) {
        // Create a new 3D game level representation
        level3D = new GameLevelView3D(game, level, engine.ui().viewModel(), engine.uiConfig(game.runtime()));
        level3D.replaceLevelCounter3D(game, game.session().hud().levelCounter());
        level3DHolder.getChildren().setAll(level3D.root());
        animationSystem.createAnimations(engine, game.runtime(), game.session(), level3D);
        settingsViewModel.drawModeProperty().addListener(level3D);
    }

    public void handleScrollEvent(ScrollEvent e) {
        perspectiveManager.currentPerspective().ifPresent(perspective -> {
            if (perspective instanceof DronePerspective dronePerspective) {
                dronePerspective.handleScrollEvent(e);
            }
        });
    }

    public void initFood3D(GameLevel level, boolean startEnergizerPumping) {
        level3D.pellets3D().forEach(pellet3D ->
            pellet3D.root().setVisible(!level.food().hasEatenFoodAtTile(pellet3D.tile())));

        level3D.energizers3D().forEach(energizer3D ->
            energizer3D.root().setVisible(!level.food().hasEatenFoodAtTile(energizer3D.tile())));

        if (startEnergizerPumping) {
            animationSystem.startEnergizerPumping(level3D);
        }
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

    public Optional<GameLevelView3D> optGameLevel3D() {
        return Optional.ofNullable(level3D);
    }

    public GameLevelView3D assertLevel3D() {
        return optGameLevel3D().orElseThrow();
    }

    @Override
    public void dispose() {
        removeLevel3D();
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
