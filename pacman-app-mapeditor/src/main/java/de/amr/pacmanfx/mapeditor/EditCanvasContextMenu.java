/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.mapeditor;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.mapeditor.actions.Action_CarveTunnel;
import de.amr.pacmanfx.mapeditor.actions.Action_ClearFoodAroundHouse;
import de.amr.pacmanfx.mapeditor.actions.Action_FloodWithPellets;
import de.amr.pacmanfx.mapeditor.actions.Action_MoveArcadeHouse;
import de.amr.pacmanfx.model.world.WorldMap;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;

import static de.amr.pacmanfx.mapeditor.Globals_MapEditor.translated;

public class EditCanvasContextMenu extends ContextMenu {

    private final TileMapEditorUI ui;
    private final EditCanvas editCanvas;

    public EditCanvasContextMenu(EditCanvas editCanvas, TileMapEditorUI ui) {
        this.editCanvas = editCanvas;
        this.ui = ui;

        ui.editModeProperty().addListener((_,_,mode) -> {
            if (mode == EditMode.INSPECT || mode == EditMode.ERASE) {
                hide();
            }
        });
    }

    public void update(ContextMenuEvent menuEvent) {
        final TileMapEditor editor = ui.editor();
        final Vector2i tile = editCanvas.tileAt(menuEvent.getX(), menuEvent.getY());
        final WorldMap worldMap = editCanvas.worldMap();

        var miCarveTunnel = new MenuItem(translated("menu.edit.carve_tunnel"));
        miCarveTunnel.setOnAction(_ -> new Action_CarveTunnel(editor, tile).execute());

        var miPlaceHouse = new MenuItem(translated("menu.edit.place_house"));
        miPlaceHouse.setOnAction(_ -> new Action_MoveArcadeHouse(editor, tile).execute());

        var miClearFoodAroundHouse = new MenuItem(translated("menu.edit.clear_food_around_house"));
        miClearFoodAroundHouse.setOnAction(_ -> new Action_ClearFoodAroundHouse(editor, worldMap).execute());

        var miInsertRow = new MenuItem(translated("menu.edit.insert_row"));
        miInsertRow.setOnAction(_ -> {
            int rowIndex = tile.y();
            editor.setCurrentWorldMap(worldMap.insertRowBeforeIndex(rowIndex));
        });

        var miDeleteRow = new MenuItem(translated("menu.edit.delete_row"));
        miDeleteRow.setOnAction(_ -> {
            int rowIndex = tile.y();
            editor.setCurrentWorldMap(worldMap.deleteRowAtIndex(rowIndex));
        });

        var miFloodWithPellets = new MenuItem(translated("menu.edit.flood_with_pellets"));
        miFloodWithPellets.setOnAction(_ -> new Action_FloodWithPellets(editor, tile).execute());
        miFloodWithPellets.setDisable(!UfxMapEditor.canPlaceFoodAtTile(worldMap, tile));

        getItems().setAll(
            miInsertRow,
            miDeleteRow,
            new SeparatorMenuItem(),
            miCarveTunnel,
            miPlaceHouse,
            miClearFoodAroundHouse,
            new SeparatorMenuItem(),
            miFloodWithPellets);
    }
}
