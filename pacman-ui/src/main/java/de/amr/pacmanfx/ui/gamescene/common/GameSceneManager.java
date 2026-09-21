/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounter3DViewSystem;
import de.amr.pacmanfx.ui.gamescene.d3.PlayScene3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSceneManager {

    private final ObjectProperty<GameScene> currentGameScene = new SimpleObjectProperty<>();

    private GameVariantGameSceneConfig gameSceneConfig;

    private final GameApp app;

    public GameSceneManager(GameApp app) {
        this.app = requireNonNull(app);
    }

    public void setGameSceneConfig(GameVariantGameSceneConfig gameSceneConfig) {
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

    public void forceGameSceneUpdate(GameUI ui, GameVariantUIConfig variantUIConfig, GameContext game) {
        updateGameSceneAndForceReload(ui, variantUIConfig, game, true);
    }

    public void updateGameSceneAndForceReload(GameUI ui, GameVariantUIConfig variantUIConfig, GameContext game, boolean forceReload) {
        final boolean select3D = ui.viewModel().common3DSettings().view3DEnabledProperty().get();
        final GameSession session = game.session();

        final GameScene nextGameScene = variantUIConfig.gameSceneConfig().selectGameScene(game, select3D).orElse(null);

        if (nextGameScene == null) {
            throw new IllegalStateException("Could not determine next game scene");
        }

        nextGameScene.setApp(app);

        if (nextGameScene == currentGameScene()) {
            if (!forceReload) {
                return;
            }
            Logger.info("No game scene change but reload requested");
        }
        ui.viewManager().gamePlayView().replaceGameScene(currentGameScene(), nextGameScene);

        //TODO rethink this
        if (!(nextGameScene instanceof AbstractGameScene nextScene)) {
            Logger.error("Next game scene is not an AbstractGameScene");
            return;
        }
        session.optLevel().ifPresent(_ -> handle2D3DSwitch(variantUIConfig, game, currentGameScene(), nextScene));

        currentGameSceneProperty().set(nextGameScene);
    }

    public boolean hasGameSceneID(GameVariantGameSceneConfig gameSceneConfig, GameScene gameScene, Named sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        requireNonNull(sceneID);
        return gameSceneConfig.gameSceneHasID(gameScene, sceneID);
    }

    /**
     * Checks whether the current game scene matches the given ID.
     *
     * @param sceneID scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    public boolean currentGameSceneHasID(Named sceneID) {
        requireNonNull(sceneID);

        final GameScene currentGameScene = currentGameSceneProperty().get();
        return currentGameScene != null && hasGameSceneID(gameSceneConfig, currentGameScene, sceneID);
    }

    public void removeCurrentGameScene() {
        currentGameSceneProperty().set(null);
    }

    // 2D-3D scene switch

    private void handle2D3DSwitch(
        GameVariantUIConfig variantConfig,
        GameContext game,
        GameScene currentGameScene,
        GameScene nextGameScene)
    {
        final GameSceneSwitchType switchType = identifySwitchType(currentGameScene, nextGameScene);
        switch (switchType) {
            case FROM_2D_TO_3D -> switchPlaySceneTo3D(variantConfig, game, currentGameScene, nextGameScene);
            case FROM_3D_TO_2D -> switchPlaySceneTo2D(currentGameScene, nextGameScene);
            case NONE -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + switchType);
        }
    }

    private void switchPlaySceneTo3D(
        GameVariantUIConfig variantConfig,
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

        LivesCounter3DViewSystem.startTracking(session.hud().livesCounter(), pac);

        if (pac.power().isActive()) {
            variantConfig.optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }
        playScene3D.fadeIn();

        Logger.info("3D scene {} entered from 2D game scene {}", playScene3D.getClass().getSimpleName(), currentGameScene.getClass().getSimpleName());
    }

    private void switchPlaySceneTo2D(GameScene currentGameScene, GameScene nextGameScene) {
        requireNonNull(currentGameScene);
        requireNonNull(nextGameScene);

        if (!(nextGameScene instanceof AbstractGameScene abstractGameScene)) {
            Logger.error("Current game scene is not an AbstractGameScene");
            return;
        }

        if (abstractGameScene.optCanvasRendering().isPresent()) {
            nextGameScene.onEnteredFrom3DScene();
            Logger.info("2D scene {} entered from 3D scene {}",
                nextGameScene.getClass().getSimpleName(), currentGameScene.getClass().getSimpleName());
        }
        else {
            Logger.error("Scene {} has no canvas rendering support?", nextGameScene.getClass().getSimpleName());
        }
    }

    private GameSceneSwitchType identifySwitchType(GameScene currentGameScene, GameScene nextGameScene) {
        requireNonNull(currentGameScene);
        requireNonNull(nextGameScene);

        if (!(currentGameScene instanceof AbstractGameScene current)) {
            throw new IllegalArgumentException("Current game scene is not an AbstractGameScene");
        }
        final boolean currentIs2D = current.optCanvasRendering().isPresent();

        if (!(nextGameScene instanceof AbstractGameScene next)) {
            throw new IllegalArgumentException("Next game scene is not an AbstractGameScene");
        }
        final boolean nextIs2D = next.optCanvasRendering().isPresent();

        if (currentIs2D == nextIs2D) {
            return GameSceneSwitchType.NONE;
        }
        return currentIs2D ? GameSceneSwitchType.FROM_2D_TO_3D : GameSceneSwitchType.FROM_3D_TO_2D;
    }
}
