/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Named;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.gamescene.GameSceneConfig;
import de.amr.pacmanfx.engine.gamescene.GameSceneManager;
import de.amr.pacmanfx.engine.gamescene.GameSceneSwitchType;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.GameVariantUIConfig;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3D;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3D_System;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D;
import de.amr.pacmanfx.ui.gamescene.playscene.PlayScene3D;
import de.amr.pacmanfx.ui.gamescene.view3d.animation.PlaySceneView3D_FadeInAnimation;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class DefaultGameSceneManager implements GameSceneManager {

    public static Named cutSceneID(int n) {
        return switch (n) {
            case 1 -> CommonGameSceneID.CUTSCENE_1;
            case 2 -> CommonGameSceneID.CUTSCENE_2;
            case 3 -> CommonGameSceneID.CUTSCENE_3;
            case 4 -> CommonGameSceneID.CUTSCENE_4;
            default -> throw new IllegalArgumentException("Illegal cut scene number " + n);
        };
    }

    private final ObjectProperty<GameScene> currentGameScene = new SimpleObjectProperty<>();

    private GameSceneConfig gameSceneConfig;

    public DefaultGameSceneManager() {}

    public void reloadGameScene(EngineUIContext engine) {
        updateGameSceneWithReload(engine, engine.uiConfig(engine.gameVariantManager().currentRuntime()), true);
    }

    public void updateGameSceneWithReload(EngineUIContext engine, GameVariantUIConfig uiConfig, boolean forceReload) {
        if (currentGameScene() != null && !(currentGameScene() instanceof AbstractGameScene)) {
            throw new IllegalStateException("Current game scene is no abstract game scene subclass");
        }
        final AbstractGameScene currentScene = (AbstractGameScene) currentGameScene();

        final GameContext game = engine.optCurrentGame().orElseThrow();
        final boolean select3D = engine.ui().viewModel().common3DSettings().view3DEnabledProperty().get();

        final GameScene next = uiConfig.gameSceneConfig().selectGameScene(game, select3D).orElse(null);
        if (next == null) {
            throw new IllegalStateException("Could not determine next game scene");
        }
        if (!(next instanceof AbstractGameScene nextScene)) {
            throw new IllegalStateException("Next game scene is no abstract game scene subclass");
        }

        nextScene.setEngine(engine);

        if (nextScene == currentScene) {
            if (!forceReload) {
                return;
            }
            Logger.info("No game scene change but reload requested");
        }
        nextScene.activate(game);

        engine.viewManager().gamePlayView().replaceGameScene(currentScene, nextScene);
        game.session().optLevel().ifPresent(_ -> handle2D3DSwitch(uiConfig, game, currentScene, nextScene));

        currentGameSceneProperty().set(next);
    }

    // --- GameSceneManager interface

    @Override
    public void setGameSceneConfig(GameSceneConfig gameSceneConfig) {
        this.gameSceneConfig = requireNonNull(gameSceneConfig);
    }

    @Override
    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return currentGameScene;
    }

    @Override
    public Optional<GameScene> optCurrentGameScene() {
        return Optional.ofNullable(currentGameScene.get());
    }

    @Override
    public GameScene currentGameScene() {
        return currentGameScene.get();
    }

    /**
     * Checks whether the current game scene matches the given ID.
     *
     * @param id scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    @Override
    public boolean currentGameSceneHasID(Named id) {
        requireNonNull(id);
        final GameScene gameScene = currentGameSceneProperty().get();
        return gameScene != null && gameSceneHasID(gameSceneConfig, gameScene, id);
    }

    @Override
    public void removeCurrentGameScene() {
        currentGameSceneProperty().set(null);
    }

    // --- Private area

    private boolean gameSceneHasID(GameSceneConfig gameSceneConfig, GameScene gameScene, Named sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        requireNonNull(sceneID);
        return gameSceneConfig.gameSceneHasID(gameScene, sceneID);
    }

    // 2D-3D scene switch

    private void handle2D3DSwitch(
        GameVariantUIConfig uiConfig,
        GameContext game,
        GameScene currentGameScene,
        GameScene nextGameScene)
    {
        final GameSceneSwitchType switchType = identifySwitchType(currentGameScene, nextGameScene);
        switch (switchType) {
            case FROM_2D_TO_3D -> switchPlaySceneTo3D(uiConfig, game, currentGameScene, nextGameScene);
            case FROM_3D_TO_2D -> switchPlaySceneTo2D(currentGameScene, nextGameScene);
            case NONE -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + switchType);
        }
    }

    private void switchPlaySceneTo3D(
        GameVariantUIConfig uiConfig,
        GameContext game,
        GameScene currentGameScene,
        GameScene nextGameScene)
    {
        if (!(nextGameScene instanceof PlayScene3D playScene3D)) {
            throw new IllegalArgumentException("Expected PlayScene3D, but scene has class %s"
                .formatted(nextGameScene.getClass().getSimpleName()));
        }

        final GameSession session = game.session();
        final GameLevel level = session.optLevel().orElseThrow();
        final Pac pac = level.entitySet().pac();

        playScene3D.optView3D().ifPresent(view3D -> {
            playScene3D.replaceLevel3D(game, level);
            playScene3D.replaceActionBindings(session, level);
            view3D.initFood3D(level, true);
            view3D.animationRegistry().optAnimation(PlaySceneView3D_FadeInAnimation.ANIMATION_NAME).ifPresent(ManagedAnimation::replay);
        });

        // Lives counter shapes follow Pac location
        session.hud().livesCounter().optComponent(LivesCounterView3D.class).ifPresent(livesCounterView3D -> {
            if (pac.hasComponent(PacView3D.class)) {
                final LivesCounterView3D_System system = game.systems().assertSystem(LivesCounterView3D_System.class);
                system.startTrackingPac(livesCounterView3D, pac.assertComponent(PacView3D.class));
            }
        });

        if (pac.power().isActive()) {
            uiConfig.optSoundEffects().ifPresent(PacManGameSoundEffects::playPacPowerSound);
        }

        Logger.info("3D scene {} entered from 2D game scene {}", playScene3D.getClass().getSimpleName(), currentGameScene.getClass().getSimpleName());
    }

    private void switchPlaySceneTo2D(GameScene currentGameScene, GameScene nextGameScene) {
        requireNonNull(currentGameScene);
        requireNonNull(nextGameScene);

        nextGameScene.onEnteredFrom3DScene();
        Logger.info("2D scene {} entered from 3D scene {}", nextGameScene.getClass().getSimpleName(), currentGameScene.getClass().getSimpleName());
    }

    private GameSceneSwitchType identifySwitchType(GameScene currentGameScene, GameScene nextGameScene) {
        requireNonNull(currentGameScene);
        requireNonNull(nextGameScene);

        final boolean currentIs2D = isGameScene2D(currentGameScene);
        final boolean nextIs2D = isGameScene2D(nextGameScene);
        if (currentIs2D == nextIs2D) {
            return GameSceneSwitchType.NONE;
        }
        return currentIs2D ? GameSceneSwitchType.FROM_2D_TO_3D : GameSceneSwitchType.FROM_3D_TO_2D;
    }

    private boolean isGameScene2D(GameScene gameScene) {
        return !(gameScene instanceof PlayScene3D);
    }
}
