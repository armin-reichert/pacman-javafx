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
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.gamescene.GameSceneConfig;
import de.amr.pacmanfx.engine.config.GameVariantUIConfig;
import de.amr.pacmanfx.engine.runtime.action.GameActionContext;
import de.amr.pacmanfx.ui.GameSystems3D;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounterView3DSystem;
import de.amr.pacmanfx.ui.gamescene.d3.animation.PlaySceneFadeInAnimation;
import de.amr.pacmanfx.ui.gamescene.playscene.PlayScene3D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSceneManager {

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

    public void forceGameSceneUpdate(GameActionContext actionContext) {
        updateGameSceneAndForceReload(actionContext, true);
    }

    public void updateGameSceneAndForceReload(GameActionContext actionContext, boolean forceReload) {
        final GameVariantUIConfig uiConfig = actionContext.gameVariantManager().currentRuntime().uiConfig();
        final GameSession session = actionContext.currentGame().session();
        final boolean select3D = actionContext.ui().viewModel().common3DSettings().view3DEnabledProperty().get();
        final GameScene nextGameScene = uiConfig.gameSceneConfig().selectGameScene(actionContext.currentGame(), select3D).orElse(null);

        if (nextGameScene == null) {
            throw new IllegalStateException("Could not determine next game scene");
        }
        if (!(nextGameScene instanceof AbstractGameScene nextScene)) {
            throw new IllegalStateException("Next game scene is no abstract game scene subclass");
        }

        nextScene.setActionContext(actionContext);

        if (nextGameScene == currentGameScene()) {
            if (!forceReload) {
                return;
            }
            Logger.info("No game scene change but reload requested");
        }
        nextGameScene.activate();
        actionContext.ui().viewManager().gamePlayView().replaceGameScene(currentGameScene(), nextGameScene);

        session.optLevel().ifPresent(_ -> handle2D3DSwitch(uiConfig, actionContext.currentGame(), currentGameScene(), nextScene));

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
