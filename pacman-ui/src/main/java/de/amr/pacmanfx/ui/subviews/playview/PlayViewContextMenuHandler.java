/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.subviews.playview;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import static de.amr.pacmanfx.ui.subviews.ContextMenuSupport.addLocalizedActionItem;
import static de.amr.pacmanfx.ui.subviews.ContextMenuSupport.addLocalizedTitleItem;
import static java.util.Objects.requireNonNull;

public class PlayViewContextMenuHandler implements EventHandler<ContextMenuEvent> {

    private final Game game;
    private final GamePlayView playView;

    public PlayViewContextMenuHandler(Game game, GamePlayView playView) {
        this.game = requireNonNull(game);
        this.playView = requireNonNull(playView);

        //TODO is there a better way to hide the context menu?
        game.ui().view().mainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                playView.contextMenu().hide();
            }
        });
    }

    @Override
    public void handle(ContextMenuEvent event) {
        final ContextMenu menu = playView.contextMenu();
        menu.getItems().clear();

        game.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            // Add 2D play scene-specific entries
            if (game.ui().gameScenes().currentGameSceneHasID(game, CommonSceneID.PLAY_SCENE_2D)) {
                addLocalizedTitleItem(menu, game.ui().translations(), "scene_display");
                addLocalizedActionItem(menu, game, game.ui().translations(), game.commonActions().ACTION_TOGGLE_PLAY_SCENE_2D_3D, "use_3D_scene");
            }
            // Add scene-specific entries
            gameScene.supplyContextMenu().ifPresent(sceneMenu -> menu.getItems().addAll(sceneMenu.getItems()));
        });

        if (!menu.getItems().isEmpty()) {
            menu.show(playView.rootPane(), event.getScreenX(), event.getScreenY());
            menu.requestFocus();
        }
    }
}
