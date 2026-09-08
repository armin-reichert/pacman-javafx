/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import static de.amr.pacmanfx.ui.views.ContextMenuSupport.addLocalizedActionItem;
import static de.amr.pacmanfx.ui.views.ContextMenuSupport.addLocalizedTitleItem;
import static java.util.Objects.requireNonNull;

public class ContextMenuManager implements EventHandler<ContextMenuEvent> {

    private final ContextMenu contextMenu = new ContextMenu();

    private final GameAppContext app;

    private final GameMainScene mainScene;

    public ContextMenuManager(GameAppContext app, GameMainScene mainScene) {
        this.app = requireNonNull(app);
        this.mainScene = requireNonNull(mainScene);
        mainScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
            }
        });
    }

    @Override
    public void handle(ContextMenuEvent event) {
        contextMenu.getItems().clear();

        app.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            final TranslationManager translations = app.ui().translations();
            // Add 2D play scene-specific entries
            if (app.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D)) {
                addLocalizedTitleItem(contextMenu, translations, "context_menu.scene_display");
                addLocalizedActionItem(
                    app,
                    contextMenu,
                    translations,
                    app.commonActions().uiSettingsActions().actionTogglePlayScene2D3D(),
                    "context_menu.use_3D_scene");
            }
            // Add scene-specific entries
            gameScene.optContextMenu().ifPresent(sceneMenu -> contextMenu.getItems().addAll(sceneMenu.getItems()));
        });

        if (!contextMenu.getItems().isEmpty()) {
            contextMenu.show(mainScene.rootPane(),/*rootPane,*/ event.getScreenX(), event.getScreenY());
            contextMenu.requestFocus();
        }
    }

    public void hideContextMenu() {
        contextMenu.hide();
    }
}
