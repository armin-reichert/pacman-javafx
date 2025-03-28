/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._3d;

import de.amr.games.pacman.ui._2d.GameView;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.uilib.Keyboard.alt;
import static de.amr.games.pacman.uilib.Ufx.contextMenuTitleItem;

/**
 * @author Armin Reichert
 */
public class GameView3D extends GameView {

    public GameView3D(Scene parentScene) {
        super(parentScene);
    }

    @Override
    public void bindGameActions() {
        super.bindGameActions();
        bind(GameActions3D.TOGGLE_PIP_VISIBILITY, KeyCode.F2);
        bind(GameActions3D.TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3));
    }

    protected List<MenuItem> createContextMenuItems(ContextMenuEvent event) {
        List<MenuItem> menuItems = new ArrayList<>();
        if (THE_UI.configurations().currentGameSceneIsPlayScene2D()) {
            menuItems.add(contextMenuTitleItem(THE_UI.assets().text("scene_display")));
            var item = new MenuItem(THE_UI.assets().text("use_3D_scene"));
            item.setOnAction(ae -> GameActions3D.TOGGLE_PLAY_SCENE_2D_3D.execute());
            menuItems.add(item);
        }
        menuItems.addAll(super.createContextMenuItems(event));
        return menuItems;
    }
}