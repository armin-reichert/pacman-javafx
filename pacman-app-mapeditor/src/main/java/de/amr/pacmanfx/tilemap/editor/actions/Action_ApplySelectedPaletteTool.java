package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.Palette;
import de.amr.pacmanfx.tilemap.editor.TileMapEditorUI;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class Action_ApplySelectedPaletteTool extends AbstractEditorUIAction<Void> {

    private final Vector2i tile;
    private final boolean erase;

    public Action_ApplySelectedPaletteTool(TileMapEditorUI ui, Vector2i tile, boolean erase) {
        super(ui);
        this.tile = requireNonNull(tile);
        this.erase = erase;
    }

    @Override
    public Void execute() {
        Palette palette = ui.selectedPalette();
        switch (ui.selectedPaletteID()) {
            case TERRAIN -> {
                if (erase) {
                    new Action_ClearTerrainTile(editor, tile).execute();
                } else if (palette.isToolSelected()) {
                    palette.selectedTool().editor().accept(LayerID.TERRAIN, tile);
                }
            }
            case FOOD -> {
                if (erase) {
                    new Action_ClearFoodTile(editor, tile).execute();
                } else if (palette.isToolSelected()) {
                    palette.selectedTool().editor().accept(LayerID.FOOD, tile);
                }
            }
            case ACTORS -> {
                if (palette.isToolSelected()) {
                    palette.selectedTool().editor().accept(LayerID.TERRAIN, tile);
                }
            }
            default -> Logger.error("Unknown palette ID " + ui.selectedPaletteID());
        }
        return null;
    }
}