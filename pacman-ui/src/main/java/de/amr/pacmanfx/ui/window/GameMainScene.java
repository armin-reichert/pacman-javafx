/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.window;

import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.basics.math.RandomNumberSupport.randomArrayEntry;
import static java.util.Objects.requireNonNull;

/**
 * The main scene of the game.
 *
 * <p>Handles all key events and forwards them to the global keyboard
 * instance where the current key state is stored and can be queried by game scenes.</p>
 *
 * <p>Also stores the key bindings for global actions like fullscreen on/off, mute on/off.</p>
 */
public class GameMainScene extends Scene {

    private final FlashMessageManager flashMessageManager;

    private final StackPane gameViewHolder = new StackPane();

    private final StatusIconBox statusIconBox;
    private final KeyboardInfoPopup keyboardInfoPopup;

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Global Action Bindings");

    private static boolean isUsingGlobalKeyboard(GameViewID viewID) {
        return viewID == GameViewID.START_PAGES || viewID == GameViewID.GAMEPLAY;
    }

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

        connectKeyboard(game);
        registerGlobalActions(game);
    }

    public FlashMessageManager flashMessageManager() {
        return flashMessageManager;
    }

    public void replaceGameView(GameView gameView) {
        requireNonNull(gameView);
        gameViewHolder.getChildren().setAll(gameView.rootPane());
    }

    private void connectKeyboard(Game game) {
        final Keyboard keyboard = game.machine().input().keyboard();
        final GameViewManager views = game.ui().viewManager();

        keyboard.filterKeyEventsFrom(this);
        keyboard.enabledProperty().bind(views.currentViewIDProperty().map(GameMainScene::isUsingGlobalKeyboard));
        keyboard.addStateListener(_ -> {
            if (keyboard.anyNormalKeyPressed()) { // ignore modifier state change
                final GameViewID currentViewID = views.currentViewID();
                if (isUsingGlobalKeyboard(currentViewID)) {
                    // Check for matching "global" action first, if none, let current view handle it.
                    if (actionBindings.executeMatchingAction(game.machine().input()).isEmpty()) {
                        views.assertView(currentViewID).onInput(game.machine().input());
                    }
                }
            }
        });
    }

    private void registerGlobalActions(Game game) {
        final CommonActions actions = game.actions();
        final Set<ActionKeyBinding> bindings = actions.bindings();
        actionBindings.selectAnyMatchingBinding(actions.uiSettingsActions().actionToggleKeyboardMonitor(), bindings);
        actionBindings.selectAnyMatchingBinding(actions.uiSettingsActions().actionEnterFullScreen(), bindings);
        actionBindings.selectAnyMatchingBinding(actions.simulationActions().actionToggleMuted(), bindings);
        actionBindings.selectAnyMatchingBinding(actions.editorActions().actionOpenEditor(), bindings);
        Logger.info(actionBindings);
    }

    private Background selectBackground(Game game) {
        return game.ui().gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_3D)
            ? randomArrayEntry(GlobalAssets.GRADIENT_BACKGROUNDS)
            : GlobalAssets.BACKGROUND_PAC_MAN_WALLPAPER;
    }
}