/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.tinylog.Logger;

import static de.amr.pacmanfx.ui.views.ContextMenuSupport.addLocalizedActionItem;
import static de.amr.pacmanfx.ui.views.ContextMenuSupport.addLocalizedTitleItem;
import static java.util.Objects.requireNonNull;

public class ContextMenuManager implements EventHandler<ContextMenuEvent> {

    private final ContextMenu contextMenu = new ContextMenu();

    private final GameEngineContext engineContext;

    public ContextMenuManager(GameEngineContext engineContext) {
        this.engineContext = requireNonNull(engineContext);
        engineContext.ui().window().mainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
            }
        });
    }

    @Override
    public void handle(ContextMenuEvent e) {
        Logger.info("Received context menu event {}", e);

        contextMenu.getItems().clear();

        engineContext.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> {
            final TranslationManager translations = engineContext.translationManager();
            // Add 2D play scene-specific entries
            if (engineContext.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D)) {
                addLocalizedTitleItem(contextMenu, translations, "context_menu.scene_display");
                addLocalizedActionItem(
                    engineContext,
                    contextMenu,
                    translations,
                    CommonGameActions.instance().uiSettingsActions().actionTogglePlayScene2D3D(),
                    "context_menu.use_3D_scene");
            }
            // Add game scene provided menu entries
            gameScene.optContextMenu().ifPresent(sceneMenu -> contextMenu.getItems().addAll(sceneMenu.getItems()));
        });

        if (!contextMenu.getItems().isEmpty()) {
            contextMenu.show(engineContext.ui().window().mainScene().rootPane(), e.getScreenX(), e.getScreenY());
            contextMenu.requestFocus();
        }
    }

    public void hideContextMenu() {
        contextMenu.hide();
    }
}
