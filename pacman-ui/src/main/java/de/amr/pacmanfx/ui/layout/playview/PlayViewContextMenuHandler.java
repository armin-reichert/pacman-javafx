/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.ui.GameSceneConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonActions;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import static de.amr.pacmanfx.ui.layout.ContextMenuSupport.addLocalizedActionItem;
import static de.amr.pacmanfx.ui.layout.ContextMenuSupport.addLocalizedTitleItem;
import static java.util.Objects.requireNonNull;

public class PlayViewContextMenuHandler implements EventHandler<ContextMenuEvent> {

    private final GameUI ui;
    private final PlayView playView;

    public PlayViewContextMenuHandler(GameUI ui, PlayView playView) {
        this.ui = requireNonNull(ui);
        this.playView = requireNonNull(playView);

        //TODO is there a better way to hide the context menu?
        ui.scene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                playView.contextMenu().hide();
            }
        });
    }

    @Override
    public void handle(ContextMenuEvent event) {
        final ContextMenu menu = playView.contextMenu();
        menu.getItems().clear();

        ui.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> {
            // Add 2D play scene-specific entries
            if (ui.currentGameSceneHasID(GameSceneConfig.CommonSceneID.PLAY_SCENE_2D)) {
                addLocalizedTitleItem(menu, ui.translationManager(), "scene_display");
                addLocalizedActionItem(menu, ui, ui.translationManager(), CommonActions.ACTION_TOGGLE_PLAY_SCENE_2D_3D, "use_3D_scene");
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
