/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.scene.*;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;

import static de.amr.games.pacman.ui2d.GameParameters.PY_DEBUG_INFO;
import static de.amr.games.pacman.ui3d.GameParameters3D.PY_3D_ENABLED;

/**
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private final PacManGames3dUI ui = new PacManGames3dUI();

    @Override
    public void start(Stage stage) {
        Logger.info("JavaFX version:   {}", System.getProperty("javafx.runtime.version"));
        GameController.it().selectGameVariant(GameVariant.PACMAN);
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        double width = aspect * height;
        ui.loadAssets(true);
        ui.createLayout(stage, width, height);
        ui.setGameScenes(createGameScenes(ui));
        GameSounds.init(ui.assets());
        ui.start();
        PY_3D_ENABLED.set(true);
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.gameClock().stop();
    }

    private Map<GameVariant, Map<GameSceneID, GameScene>> createGameScenes(PacManGames3dUI ui) {
        Map<GameVariant, Map<GameSceneID, GameScene>> gameScenesForVariant = new EnumMap<>(GameVariant.class);
        for (GameVariant variant : GameVariant.values()) {
            switch (variant) {
                case MS_PACMAN ->
                    gameScenesForVariant.put(variant, new EnumMap<>(Map.of(
                        GameSceneID.BOOT_SCENE,   new BootScene(),
                        GameSceneID.INTRO_SCENE,  new MsPacManIntroScene(),
                        GameSceneID.CREDIT_SCENE, new CreditScene(),
                        GameSceneID.PLAY_SCENE,   new PlayScene2D(),
                        GameSceneID.CUT_SCENE_1,  new MsPacManCutScene1(),
                        GameSceneID.CUT_SCENE_2,  new MsPacManCutScene2(),
                        GameSceneID.CUT_SCENE_3,  new MsPacManCutScene3()
                    )));
                case PACMAN, PACMAN_XXL ->
                    gameScenesForVariant.put(variant, new EnumMap<>(Map.of(
                        GameSceneID.BOOT_SCENE,   new BootScene(),
                        GameSceneID.INTRO_SCENE,  new PacManIntroScene(),
                        GameSceneID.CREDIT_SCENE, new CreditScene(),
                        GameSceneID.PLAY_SCENE,   new PlayScene2D(),
                        GameSceneID.CUT_SCENE_1,  new PacManCutScene1(),
                        GameSceneID.CUT_SCENE_2,  new PacManCutScene2(),
                        GameSceneID.CUT_SCENE_3,  new PacManCutScene3()
                    )));
            }
            gameScenesForVariant.get(variant).values().forEach(gameScene -> {
                if (gameScene instanceof GameScene2D gameScene2D) {
                    gameScene2D.setContext(ui);
                    gameScene2D.infoVisiblePy.bind(PY_DEBUG_INFO);
                }
            });
        }
        for (GameVariant variant : GameVariant.values()) {
            var playScene3D = new PlayScene3D();
            playScene3D.setContext(ui);
            playScene3D.widthProperty().bind(ui.widthProperty());
            playScene3D.heightProperty().bind(ui.heightProperty());
            gameScenesForVariant.get(variant).put(GameSceneID.PLAY_SCENE_3D, playScene3D);
            Logger.info("Added 3D play scene for variant " + variant);
        }
        return gameScenesForVariant;
    }
}