/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.entities3D.livescounter.system.LivesCounter3DViewSystem;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d3.PlayScene3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSceneManager {

    private GameAppContext app;

    private final ObjectProperty<GameScene> currentGameScene = new SimpleObjectProperty<>();

    public GameSceneManager() {
        currentGameScene.addListener((_, _, newGameScene) -> {
            if (newGameScene != null) {
                app.ui().views().gamePlayView().embedGameScene(newGameScene);
            }
        });
    }

    public void setGameApp(GameAppContext app) {
        this.app = requireNonNull(app);
    }

    public Optional<GameScene> optCurrentGameScene() {
        return Optional.ofNullable(currentGameScene.get());
    }

    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return currentGameScene;
    }

    public void forceGameSceneUpdate() {
        updateGameSceneAndForceReload(true);
    }

    public void updateGameSceneAndForceReload(boolean forceReload) {
        final GameVariantConfig variantConfig = app.gameVariants().currentGameVariant().config();
        final GameContext game = app.currentGame();
        final GameSession session = game.session();
        final GameScene currentGameScene = optCurrentGameScene().orElse(null);
        final GameScene nextGameScene = variantConfig.gameSceneConfig().selectGameScene(app).orElse(null);

        if (nextGameScene == null) {
            throw new IllegalStateException("Could not determine next game scene");
        }

        if (nextGameScene == currentGameScene) {
            if (!forceReload) {
                return;
            }
            Logger.info("No game scene change but reload requested");
        }

        app.ui().views().gamePlayView().replaceGameScene(currentGameScene, nextGameScene);

        //TODO rethink this
        session.optLevel().ifPresent(_ -> handle2D3DSwitch(variantConfig, game, currentGameScene, nextGameScene));

        currentGameSceneProperty().set(nextGameScene);
    }

    /**
     * Checks whether the given game scene matches the given ID.
     *
     * @param gameScene game scene
     * @param sceneID scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    public boolean hasGameSceneID(GameScene gameScene, Named sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);

        final GameVariantConfig config = app.gameVariants().currentGameVariant().config();
        return config.gameSceneConfig().gameSceneHasID(gameScene, sceneID);
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
        return currentGameScene != null && hasGameSceneID(currentGameScene, sceneID);
    }

    // 2D-3D scene switch

    private void handle2D3DSwitch(
        GameVariantConfig variantConfig,
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
        GameVariantConfig variantConfig,
        GameContext game,
        GameScene currentGameScene,
        GameScene nextGameScene)
    {
        if (!(nextGameScene instanceof PlayScene3D playScene3D)) {
            throw new IllegalArgumentException("Expected PlayScene3D, but scene has class %s"
                .formatted(nextGameScene.getClass().getSimpleName()));
        }

        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        final Pac pac = level.entities().pac();

        playScene3D.replaceGameLevel3D(game, level);
        playScene3D.replaceActionBindings(session, level);
        playScene3D.initFood3D(level, true);
        playScene3D.updateHUD3D(game);

        LivesCounter3DViewSystem.startTracking(session.livesCounter(), pac);

        if (pac.power().isActive()) {
            variantConfig.optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }
        playScene3D.fadeInAnimation().playFromStart();

        Logger.info("3D scene {} entered from 2D game scene {}", playScene3D.getClass().getSimpleName(), currentGameScene.getClass().getSimpleName());
    }

    private void switchPlaySceneTo2D(GameScene currentGameScene, GameScene nextGameScene) {
        if (!(nextGameScene instanceof AbstractGameScene2D playScene2D)) {
            throw new IllegalArgumentException("Expected GameScene2D, but scene has class %s"
                .formatted(nextGameScene.getClass().getSimpleName()));
        }
        playScene2D.onEnteredFrom3DScene();

        Logger.info("2D scene {} entered from 3D scene {}", playScene2D.getClass().getSimpleName(), currentGameScene.getClass().getSimpleName());
    }

    private GameSceneSwitchType identifySwitchType(GameScene currentGameScene, GameScene nextGameScene) {
        if (currentGameScene == null && nextGameScene == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (currentGameScene) {
            case AbstractGameScene2D _ when nextGameScene instanceof PlayScene3D         -> GameSceneSwitchType.FROM_2D_TO_3D;
            case PlayScene3D         _ when nextGameScene instanceof AbstractGameScene2D -> GameSceneSwitchType.FROM_3D_TO_2D;
            case null, default                                                           -> GameSceneSwitchType.NONE;
        };
    }
}
