package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import org.tinylog.Logger;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;
import static java.util.Objects.requireNonNull;

public class Action_EditTile extends AbstractEditorAction<Void> {

    private final Vector2i tile;
    private final boolean erase;

    public Action_EditTile(TileMapEditor editor, Vector2i tile, boolean erase) {
        super(editor);
        this.tile = requireNonNull(tile);
        this.erase = erase;
    }

    @Override
    public Void execute() {
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
