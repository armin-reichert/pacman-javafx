/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.basics.math.RandomNumberSupport;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.CommonSceneID;
import de.amr.pacmanfx.ui.subviews.SubView;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public class GameMainScene extends Scene {

    private final StackPane subViewHolder = new StackPane();

    public GameMainScene(double width, double height) {
        super(new StackPane(), width, height, Color.BLACK);
    }

    public void connect(Game game) {
        // Delegate mouse scroll events to current game scene
        setOnScroll(e -> game.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

        rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> selectBackground(game),
            game.ui().subViews().currentSubViewProperty(),
            game.ui().gameScenes().currentGameSceneProperty()
        ));
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }

    public StackPane subViewHolder() {
        return subViewHolder;
    }

    public void replaceSubView(SubView subView) {
        requireNonNull(subView);
        subViewHolder.getChildren().setAll(subView.rootPane());
    }

    private Background selectBackground(Game game) {
        return game.ui().gameScenes().currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D)
            ? GameUI_Constants.WALLPAPERS[RandomNumberSupport.randomInt(0, GameUI_Constants.WALLPAPERS.length)]
            : GameUI_Constants.BACKGROUND_PAC_MAN_WALLPAPER;
    }
}