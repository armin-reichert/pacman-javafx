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
public class GameMainScene extends Scene {

    private final StackPane gameViewRoot = new StackPane();

    private final StatusIconBox statusIconBox;

    private final KeyboardInfoPopup keyboardInfoPopup;

    public GameMainScene(double width, double height, Pane flashMessagePane) {
        super(new StackPane(), width, height, Color.BLACK);

        statusIconBox = new StatusIconBox();
        keyboardInfoPopup = new KeyboardInfoPopup();

        rootPane().getChildren().addAll(
            gameViewRoot,
            statusIconBox.rootPane(),
            flashMessagePane,
            keyboardInfoPopup.rootPane()
        );

        StackPane.setAlignment(statusIconBox.rootPane(), Pos.BOTTOM_LEFT);
        keyboardInfoPopup.rootPane().setAlignment(Pos.TOP_CENTER);

        getStylesheets().add(GlobalAssets.GAME_STYLESHEET);
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }

    public void setEngineContext(EngineUIContext engine) {
        // Delegate mouse scroll events to current game scene
        setOnScroll(e -> engine.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

        rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> selectBackground(engine.gameSceneManager()),
            engine.viewManager().currentViewIDProperty(),
            engine.gameSceneManager().currentGameSceneProperty()
        ));

        statusIconBox.setEngineContext(engine);
        keyboardInfoPopup.setEngineContext(engine);
    }

    public void setGameSession(GameSession session) {
        requireNonNull(session);
        statusIconBox.bind(session.cheats());
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