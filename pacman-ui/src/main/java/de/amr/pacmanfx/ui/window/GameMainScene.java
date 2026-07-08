/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.views.GameView;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.basics.math.RandomNumberSupport.randomArrayEntry;
import static java.util.Objects.requireNonNull;

/**
 * The main scene of the game.
 */
public class GameMainScene extends Scene {

    private final FlashMessageManager flashMessageManager;

    private final StackPane gameViewHolder = new StackPane();

    private final StatusIconBox statusIconBox;

    private final KeyboardInfoPopup keyboardInfoPopup;

    public GameMainScene(double width, double height) {
        super(new StackPane(), width, height, Color.BLACK);

        getStylesheets().add(GlobalAssets.GAME_STYLESHEET);

        flashMessageManager = new FlashMessageManager();

        statusIconBox = new StatusIconBox();
        StackPane.setAlignment(statusIconBox.rootPane(), Pos.BOTTOM_LEFT);

        keyboardInfoPopup = new KeyboardInfoPopup();
        keyboardInfoPopup.rootPane().setAlignment(Pos.TOP_CENTER);

        rootPane().setOnMouseClicked(e -> Logger.info("Mouse clicked: {}", e));
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }

    public void connect(Game game) {
        // Delegate mouse scroll events to current game scene
        setOnScroll(e -> game.ui().gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

        rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> selectBackground(game),
            game.ui().viewManager().currentViewIDProperty(),
            game.ui().gameSceneManager().currentGameSceneProperty()
        ));

        statusIconBox.connect(game);
        keyboardInfoPopup.connect(game);

        rootPane().getChildren().addAll(
            gameViewHolder,
            statusIconBox.rootPane(),
            flashMessageManager.messageView().rootPane(),
            keyboardInfoPopup.rootPane()
        );
    }

    public FlashMessageManager flashMessageManager() {
        return flashMessageManager;
    }

    public void replaceGameView(GameView gameView) {
        requireNonNull(gameView);
        gameViewHolder.getChildren().setAll(gameView.rootPane());
    }

    private Background selectBackground(Game game) {
        return game.ui().gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D)
            ? randomArrayEntry(GlobalAssets.GRADIENT_BACKGROUNDS)
            : GlobalAssets.BACKGROUND_PAC_MAN_WALLPAPER;
    }
}