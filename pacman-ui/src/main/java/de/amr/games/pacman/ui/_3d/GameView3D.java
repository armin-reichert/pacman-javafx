/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._2d.GameView;
import javafx.beans.binding.Bindings;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_DEBUG_INFO_VISIBLE;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.PY_3D_ENABLED;
import static de.amr.games.pacman.uilib.Keyboard.alt;
import static de.amr.games.pacman.uilib.Ufx.contextMenuTitleItem;

/**
 * @author Armin Reichert
 */
public class GameView3D extends GameView {

    public GameView3D(PacManGamesUI ui) {
        super(ui);

        titleExpression = Bindings.createStringBinding(
            () -> {
                String sceneName = ui.currentGameScene().map(gameScene -> gameScene.getClass().getSimpleName()).orElse(null);
                String sceneNameText = sceneName != null && PY_DEBUG_INFO_VISIBLE.get() ? " [%s]".formatted(sceneName) : "";
                String assetNamespace = ui.configurations().current().assetNamespace();
                String key = "app.title." + assetNamespace;
                if (ui.clock().isPaused()) {
                    key += ".paused";
                }
                String modeKey = ui.assets().text(PY_3D_ENABLED.get() ? "threeD" : "twoD");
                if (ui.currentGameScene().isPresent() && ui.currentGameScene().get() instanceof GameScene2D gameScene2D) {
                    return ui.assets().text(key, modeKey) + sceneNameText + " (%.2fx)".formatted(gameScene2D.scaling());
                }
                return ui.assets().text(key, modeKey) + sceneNameText;
            },
            ui.clock().pausedProperty(), gameScenePy, PY_3D_ENABLED, PY_DEBUG_INFO_VISIBLE);
    }

    @Override
    public void bindGameActions() {
        super.bindGameActions();
        bind(GameActions3D.TOGGLE_PIP_VISIBILITY, KeyCode.F2);
        bind(GameActions3D.TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3));
    }

    protected List<MenuItem> createContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> menuItems = super.createContextMenuItems(e);
        if (THE_UI.configurations().currentGameSceneIsPlayScene2D()) {
            menuItems.add(contextMenuTitleItem(THE_UI.assets().text("scene_display")));
            var item = new MenuItem(THE_UI.assets().text("use_3D_scene"));
            item.setOnAction(ae -> GameActions3D.TOGGLE_PLAY_SCENE_2D_3D.execute());
            menuItems.addFirst(item);
        }
        return menuItems;
    }
}