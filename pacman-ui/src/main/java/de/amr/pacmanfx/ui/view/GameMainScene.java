/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.view;

import de.amr.basics.math.RandomNumberSupport;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.action.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.action.GameActionBindingsMap;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.CommonSceneID;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.subviews.SubView;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public class GameMainScene extends Scene {

    /** Index in the main scene's root pane child list where the active view is embedded. */
    public static final int SUBVIEW_CHILD_INDEX = 0;

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Main Scene Action Bindings");

    public GameMainScene(double width, double height) {
        super(new StackPane(), width, height, Color.BLACK);
    }

    public void connect(Game game) {
        final SubViewManager subViews = game.ui().subViews();
        final Keyboard keyboard = game.input().keyboard();

        // Keyboard should not be sensitive to any key events triggered inside the map editor
        keyboard.enabledProperty().bind(subViews.selectedSubViewProperty().map(
            subView -> isGlobalKeyboardAvailableFor(subViews, subView)
        ));

        keyboard.addStateListener(_ -> {
            // Check for "global" action first, if no one matches, let current sub view handle the keyboard state change
            actionBindings.findActionMatchingPressedKeys(game.input().keyboard()).ifPresentOrElse(
                GameAction::execute,
                () -> {
                    final SubView currentSubView = game.ui().subViews().currentView();
                    if (isGlobalKeyboardAvailableFor(subViews, currentSubView)) {
                        currentSubView.onInput(game, game.input());
                    }
                });
        });

        keyboard.filterKeyEventsFrom(this);

        // Delegate mouse scroll events to current game scene
        setOnScroll(e -> game.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

        rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> selectMainSceneBackground(game),
            game.ui().subViews().selectedSubViewProperty(),
            game.ui().gameScenes().gameSceneProperty()
        ));
    }

    public ActionBindingsRegistry actionBindings() {
        return actionBindings;
    }

    public StackPane rootPane() {
        return (StackPane) getRoot();
    }

    public void replaceSubView(SubView subView) {
        requireNonNull(subView);
        if (rootPane().getChildren().isEmpty()) {
            throw new IllegalStateException("Root pane has no placeholder for embedding view");
        }
        rootPane().getChildren().set(SUBVIEW_CHILD_INDEX, subView.rootPane());
    }

    private Background selectMainSceneBackground(Game game) {
        return game.ui().gameScenes().currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_3D)
            ? GameUI_Constants.WALLPAPERS[RandomNumberSupport.randomInt(0, GameUI_Constants.WALLPAPERS.length)]
            : GameUI_Constants.BACKGROUND_PAC_MAN_WALLPAPER;
    }

    private boolean isGlobalKeyboardAvailableFor(SubViewManager subViews, SubView subView) {
        return subView == subViews.startView() || subView == subViews.gamePlayView();
    }
}