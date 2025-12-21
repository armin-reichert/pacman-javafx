/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.MessageType;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.model.world.WorldMap;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;

public class Action_SetEmptyMapFromTemplateImage extends EditorUIAction<Void> {

    //TODO currently it is assumed that image has this format
    private static final int EMPTY_ROWS_OVER_MAZE = 3;
    private static final int EMPTY_ROWS_BELOW_MAZE = 2;

    private final Image image;

    public Action_SetEmptyMapFromTemplateImage(TileMapEditorUI ui, Image image) {
        super(ui);
        this.image = image;
    }

    @Override
    public Void execute() {
        int numRows = EMPTY_ROWS_OVER_MAZE + EMPTY_ROWS_BELOW_MAZE + (int) (image.getHeight() / TS);
        int numCols = (int) (image.getWidth() / TS);
        WorldMap emptyMap = new WorldMap(numCols, numRows);
        editor.setCurrentWorldMap(emptyMap);
        editor.setEdited(true);
        ui.selectTemplateImageTab();
        ui.messageDisplay().showMessage("Select colors for tile identification!", 10, MessageType.INFO);
        return null;
    }
}