/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.engine.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.assets.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.views.GameView;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import static de.amr.basics.math.RandomNumbers.randomArrayEntry;
import static java.util.Objects.requireNonNull;

/**
 * The main scene of the game.
 */
public class MainScene {

    private final Scene scene;

    private final StackPane sceneRoot;

    private final StackPane gameViewRoot;

    private final StatusIconBox statusIconBox;

    private final KeyboardInfoPopup keyboardInfoPopup;

    public MainScene(double width, double height, Pane flashMessagePane) {
        gameViewRoot = new StackPane();
        statusIconBox = new StatusIconBox();
        keyboardInfoPopup = new KeyboardInfoPopup();

        sceneRoot = new StackPane(
            gameViewRoot,
            statusIconBox.rootPane(),
            flashMessagePane,
            keyboardInfoPopup.rootPane()
        );

        StackPane.setAlignment(statusIconBox.rootPane(), Pos.BOTTOM_LEFT);
        keyboardInfoPopup.rootPane().setAlignment(Pos.TOP_CENTER);

        scene = new Scene(sceneRoot, width, height, Color.BLACK);
        scene.getStylesheets().add(GlobalAssets.GAME_STYLESHEET);
    }

    public Scene scene() {
        return scene;
    }

    public StackPane rootPane() {
        return sceneRoot;
    }

    public void setEngine(EngineUIContext engine) {
        // Delegate mouse scroll events to current game scene
        scene.setOnScroll(e -> engine.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

        sceneRoot.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> selectBackground(engine.gameSceneManager()),
            engine.viewManager().currentViewProperty(),
            engine.gameSceneManager().currentGameSceneProperty()
        ));

        statusIconBox.setEngine(engine);
        keyboardInfoPopup.setEngine(engine);
    }

    public void onSessionStart(GameSession session) {
        requireNonNull(session);
        statusIconBox.onSessionStart(session);
    }

    public void onSessionEnd() {
        statusIconBox.onSessionEnd();
    }

    public void replaceGameView(GameView gameView) {
        requireNonNull(gameView);
        gameViewRoot.getChildren().setAll(gameView.rootPane());
    }

    private Background selectBackground(GameSceneManager gameSceneManager) {
        return gameSceneManager.currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D)
            ? randomArrayEntry(GlobalAssets.GRADIENT_BACKGROUNDS)
            : GlobalAssets.BACKGROUND_PAC_MAN_WALLPAPER;
    }
}