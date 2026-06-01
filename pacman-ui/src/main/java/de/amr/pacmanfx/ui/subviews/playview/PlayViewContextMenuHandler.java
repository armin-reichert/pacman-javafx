/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.subviews.playview;

import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.action.CommonActions;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import static de.amr.pacmanfx.ui.subviews.ContextMenuSupport.addLocalizedActionItem;
import static de.amr.pacmanfx.ui.subviews.ContextMenuSupport.addLocalizedTitleItem;
import static java.util.Objects.requireNonNull;

public class PlayViewContextMenuHandler implements EventHandler<ContextMenuEvent> {

    private final AppContext context;
    private final GamePlay_SubView playView;

    public PlayViewContextMenuHandler(AppContext context, GamePlay_SubView playView) {
        this.context = requireNonNull(context);
        this.playView = requireNonNull(playView);

        //TODO is there a better way to hide the context menu?
        context.view().mainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                playView.contextMenu().hide();
            }
        });
    }

    @Override
    public void handle(ContextMenuEvent event) {
        final ContextMenu menu = playView.contextMenu();
        menu.getItems().clear();

        context.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            // Add 2D play scene-specific entries
            if (context.ui().gameScenes().currentGameSceneHasID(context, CommonSceneID.PLAY_SCENE_2D)) {
                addLocalizedTitleItem(menu, context.ui().translations(), "scene_display");
                addLocalizedActionItem(menu, context, context.ui().translations(), CommonActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D, "use_3D_scene");
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
