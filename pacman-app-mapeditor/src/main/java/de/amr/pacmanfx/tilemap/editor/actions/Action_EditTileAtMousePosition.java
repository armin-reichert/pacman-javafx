package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.EditMode;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import org.tinylog.Logger;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;

public class Action_EditTileAtMousePosition extends AbstractEditorAction<Void> {

    private final double mouseX;
    private final double mouseY;
    private final boolean erase;

    public Action_EditTileAtMousePosition(TileMapEditor editor, double mouseX, double mouseY, boolean erase) {
        super(editor);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.erase = erase;
    }

    @Override
    public Void execute() {
        Vector2i tile = editor.editCanvas().tileAt(mouseX, mouseY);
        if (editor.editModeIs(EditMode.INSPECT)) {
            new Action_IdentifyObstacle(editor, tile).execute();
            return null;
        }
        switch (editor.selectedPaletteID()) {
            case PALETTE_ID_TERRAIN -> {
                if (erase) {
                    new Action_ClearTerrainTile(editor, tile).execute();
                } else if (editor.selectedPalette().isToolSelected()) {
                    editor.selectedPalette().selectedTool().editor().accept(LayerID.TERRAIN, tile);
                }
            }
            case PALETTE_ID_FOOD -> {
                if (erase) {
                    new Action_ClearFoodTile(editor, tile).execute();
                } else if (editor.selectedPalette().isToolSelected()) {
                    editor.selectedPalette().selectedTool().editor().accept(LayerID.FOOD, tile);
                }
            }
            case PALETTE_ID_ACTORS -> {
                if (editor.selectedPalette().isToolSelected()) {
                    editor.selectedPalette().selectedTool().editor().accept(LayerID.TERRAIN, tile);
                }
            }
            default -> Logger.error("Unknown palette ID " + editor.selectedPaletteID());
        }
        return null;
    }
}
