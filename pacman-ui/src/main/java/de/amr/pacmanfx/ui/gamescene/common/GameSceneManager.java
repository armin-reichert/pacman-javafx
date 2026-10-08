/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Named;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.GameSystems3D;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.core.PacManGamesEngine;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounterView3DSystem;
import de.amr.pacmanfx.ui.gamescene.playscene.PlayScene3D;
import de.amr.pacmanfx.ui.gamescene.d3.animation.PlaySceneFadeInAnimation;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSceneManager {

    private final ObjectProperty<GameScene> currentGameScene = new SimpleObjectProperty<>();

    private GameSceneConfig gameSceneConfig;

    public GameSceneManager() {}

    public void setGameSceneConfig(GameSceneConfig gameSceneConfig) {
        this.gameSceneConfig = requireNonNull(gameSceneConfig);
    }

    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return currentGameScene;
    }

    public Optional<GameScene> optCurrentGameScene() {
        return Optional.ofNullable(currentGameScene.get());
    }

    public GameScene currentGameScene() {
        return currentGameScene.get();
    }

    public void forceGameSceneUpdate(PacManGamesEngine engine) {
        updateGameSceneAndForceReload(engine, true);
    }

    public void updateGameSceneAndForceReload(PacManGamesEngine engine, boolean forceReload) {
        final GameUI ui = engine.ui();
        final GameVariantUIConfig uiConfig = engine.gameVariantManager().currentRuntime().uiConfig();
        final GameContext game = engine.currentGame();
        final GameSession session = game.session();
        final boolean select3D = ui.viewModel().common3DSettings().view3DEnabledProperty().get();

        final GameScene nextGameScene = uiConfig.gameSceneConfig().selectGameScene(game, select3D).orElse(null);

        if (nextGameScene == null) {
            throw new IllegalStateException("Could not determine next game scene");
        }

        nextGameScene.setEngine(engine);

        if (nextGameScene == currentGameScene()) {
            if (!forceReload) {
                return;
            }
            Logger.info("No game scene change but reload requested");
        }
        nextGameScene.activate();
        ui.viewManager().gamePlayView().replaceGameScene(currentGameScene(), nextGameScene);

        //TODO rethink this
        if (!(nextGameScene instanceof AbstractGameScene nextScene)) {
            Logger.error("Next game scene is not an AbstractGameScene");
            return;
        }
        session.optLevel().ifPresent(_ -> handle2D3DSwitch(uiConfig, game, currentGameScene(), nextScene));

        currentGameSceneProperty().set(nextGameScene);
    }

    public boolean gameSceneHasID(GameSceneConfig gameSceneConfig, GameScene gameScene, Named sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        requireNonNull(sceneID);
        return gameSceneConfig.gameSceneHasID(gameScene, sceneID);
    }

    /**
     * Checks whether the current game scene matches the given ID.
     *
     * @param id scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    public boolean currentGameSceneHasID(Named id) {
        requireNonNull(id);
        final GameScene gameScene = currentGameSceneProperty().get();
        return gameScene != null && gameSceneHasID(gameSceneConfig, gameScene, id);
    }

    public void removeCurrentGameScene() {
        currentGameSceneProperty().set(null);
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
        final GameLevel level = session.level();
        final Pac pac = level.entitySet().pac();

        playScene3D.replaceGameLevel3D(game, level);
        playScene3D.replaceActionBindings(session, level);
        playScene3D.initFood3D(level, true);
        playScene3D.updateHUD3D(game);

        // Lives counter shapes follow Pac location
        final LivesCounter livesCounter = session.hud().livesCounter();
        final LivesCounterView3DSystem livesCounterView3DSystem = GameSystems3D.reqSystem(LivesCounterView3DSystem.class);
        livesCounterView3DSystem.startTrackingPac(livesCounter, pac);

        if (pac.power().isActive()) {
            uiConfig.optSoundEffects().ifPresent(PacManGameSoundEffects::playPacPowerSound);
        }

        playScene3D.animations3D().registry().optAnimation(PlaySceneFadeInAnimation.NAME)
            .ifPresent(ManagedAnimation::replay);

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
