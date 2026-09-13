/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
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

    public void forceGameSceneUpdate(GameAppContext app) {
        updateGameSceneAndForceReload(app, true);
    }

    public void updateGameSceneAndForceReload(GameAppContext app, boolean forceReload) {
        final GameVariantUIConfig uiConfig = app.variantManager().currentVariantConfig().uiConfig();
        final GameContext game = app.game();
        final GameSession session = game.session();
        final GameScene nextGameScene = uiConfig.gameSceneConfig().selectGameScene(app).orElse(null);

        if (nextGameScene == null) {
            throw new IllegalStateException("Could not determine next game scene");
        }

        if (nextGameScene == currentGameScene()) {
            if (!forceReload) {
                return;
            }
            Logger.info("No game scene change but reload requested");
        }

        app.ui().viewManager().gamePlayView().replaceGameScene(currentGameScene(), nextGameScene);

        //TODO rethink this
        session.optLevel().ifPresent(_ -> handle2D3DSwitch(uiConfig, game, currentGameScene(), nextGameScene));

        currentGameSceneProperty().set(nextGameScene);
    }

    public boolean hasGameSceneID(GameSceneConfig gameSceneConfig, GameScene gameScene, Named sceneID) {
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
        final Pac pac = level.entities().pac();

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

        if (nextGameScene.optCanvasRendering().isPresent()) {
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

        final boolean src2D = currentGameScene.optCanvasRendering().isPresent();
        final boolean tgt2D = nextGameScene.optCanvasRendering().isPresent();

        if (src2D == tgt2D) {
            return GameSceneSwitchType.NONE;
        }
        return src2D ? GameSceneSwitchType.FROM_2D_TO_3D : GameSceneSwitchType.FROM_3D_TO_2D;
    }
}
