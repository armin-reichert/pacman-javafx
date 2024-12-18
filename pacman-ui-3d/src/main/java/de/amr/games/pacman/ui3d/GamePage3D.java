/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.lib.NightMode;
import de.amr.games.pacman.ui2d.page.GamePage;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

import java.time.LocalTime;

import static de.amr.games.pacman.lib.Globals.inClosedRange;
import static de.amr.games.pacman.ui2d.input.Keyboard.alt;
import static de.amr.games.pacman.ui2d.lib.Ufx.coloredBackground;
import static de.amr.games.pacman.ui2d.lib.Ufx.contextMenuTitleItem;
import static de.amr.games.pacman.ui2d.GlobalProperties2d.PY_NIGHT_MODE;
import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_DRAW_MODE;

/**
 * @author Armin Reichert
 */
public class GamePage3D extends GamePage {

    public GamePage3D(GameContext context, Scene parentScene) {
        super(context, parentScene);
        backgroundProperty().bind(Bindings.createObjectBinding(
            () -> {
                if (!context.currentGameSceneHasID("PlayScene3D")) {
                    return context.assets().get("wallpaper.tapestry"); // little Pac-Man tapestry
                }
                if (PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
                    return coloredBackground(Color.rgb(100, 100, 200));
                }
                NightMode nightMode = PY_NIGHT_MODE.get();
                int hour = LocalTime.now().getHour();
                boolean nightTime = inClosedRange(hour, 21, 23) || inClosedRange(hour, 0, 4);
                if (nightMode == NightMode.ON || nightMode == NightMode.AUTO && nightTime) {
                    return context.assets().get("wallpaper.night");
                }
                return context.assets().get("wallpaper.day");
            }, PY_3D_DRAW_MODE, PY_NIGHT_MODE, context.gameSceneProperty()
        ));
        setOnContextMenuRequested(this::handleContextMenuRequest);
    }

    @Override
    public void bindGameActions() {
        super.bindGameActions();
        bind(GameActions3D.TOGGLE_PIP_VISIBILITY, KeyCode.F2);
        bind(GameActions3D.TOGGLE_PLAY_SCENE_2D_3D, alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3));
    }

    private void handleContextMenuRequest(ContextMenuEvent event) {
        gameScenePy.get().supplyContextMenu(event).ifPresent(menu -> {
            contextMenu = new ContextMenu();
            if (context.currentGameSceneHasID("PlayScene2D")) {
                contextMenu.getItems().add(contextMenuTitleItem(context.locText("scene_display")));
                var item = new MenuItem(context.locText("use_3D_scene"));
                item.setOnAction(ae -> GameActions3D.TOGGLE_PLAY_SCENE_2D_3D.execute(context));
                contextMenu.getItems().add(item);
            }
            contextMenu.getItems().addAll(menu.getItems());
            if (actionOpenEditor != null) {
                contextMenu.getItems().add(new SeparatorMenuItem());
                var miOpenMapEditor = new MenuItem(context.locText("open_editor"));
                miOpenMapEditor.setOnAction(ae -> actionOpenEditor.execute(context));
                miOpenMapEditor.setDisable(!actionOpenEditor.isEnabled(context));
                contextMenu.getItems().add(miOpenMapEditor);
            }
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
            contextMenu.requestFocus();
        });
        event.consume();
    }
}