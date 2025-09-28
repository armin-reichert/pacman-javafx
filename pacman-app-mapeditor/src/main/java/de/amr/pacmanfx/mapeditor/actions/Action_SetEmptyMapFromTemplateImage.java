/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.MessageType;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.model.GameLevel;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;

public class Action_SetEmptyMapFromTemplateImage extends EditorUIAction<Void> {

    private final Image image;

    public Action_SetEmptyMapFromTemplateImage(TileMapEditorUI ui, Image image) {
        super(ui);
        this.image = image;
    }

    @Override
    public Void execute() {
        int numRows = GameLevel.EMPTY_ROWS_OVER_MAZE + GameLevel.EMPTY_ROWS_BELOW_MAZE + (int) (image.getHeight() / TS);
        int numCols = (int) (image.getWidth() / TS);
        WorldMap emptyMap = new WorldMap(numCols, numRows);
        editor.setCurrentWorldMap(emptyMap);
        editor.setEdited(true);
        ui.selectTemplateImageTab();
        ui.messageDisplay().showMessage("Select colors for tile identification!", 10, MessageType.INFO);
        return null;
    }
}