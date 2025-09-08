/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.mapeditor.EditorUI;
import de.amr.pacmanfx.mapeditor.MessageType;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;

public class Action_SetEmptyMapFromTemplateImage extends AbstractEditorUIAction<Void> {

    private final Image image;

    public Action_SetEmptyMapFromTemplateImage(EditorUI ui, Image image) {
        super(ui);
        this.image = image;
    }

    @Override
    public Void execute() {
        int numRows = GameLevel.EMPTY_ROWS_OVER_MAZE + GameLevel.EMPTY_ROWS_BELOW_MAZE + (int) (image.getHeight() / TS);
        int numCols = (int) (image.getWidth() / TS);
        WorldMap emptyMap = WorldMap.emptyMap(numCols, numRows);
        editor.setCurrentWorldMap(emptyMap);
        editor.setEdited(true);
        ui.selectTemplateImageTab();
        ui.messageDisplay().showMessage("Select colors for tile identification!", 10, MessageType.INFO);
        return null;
    }
}