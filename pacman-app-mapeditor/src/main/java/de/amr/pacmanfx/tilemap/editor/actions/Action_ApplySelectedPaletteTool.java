package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.palette.Palette;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class Action_ApplySelectedPaletteTool extends AbstractEditorUIAction<Void> {

    private final Palette palette;
    private final Vector2i tile;

    public Action_ApplySelectedPaletteTool(EditorUI ui, Palette palette, Vector2i tile) {
        super(ui);
        this.palette = requireNonNull(palette);
        this.tile = requireNonNull(tile);
    }

    @Override
    public Void execute() {
        switch (palette.id()) {
            case ACTORS, TERRAIN -> {
                if (palette.isToolSelected()) {
                    palette.selectedTool().editor().accept(LayerID.TERRAIN, tile);
                }
            }
            case FOOD -> {
                if (palette.isToolSelected()) {
                    palette.selectedTool().editor().accept(LayerID.FOOD, tile);
                }
            }
            default -> Logger.error("Unknown palette ID " + ui.selectedPaletteID());
        }
        return null;
    }
}