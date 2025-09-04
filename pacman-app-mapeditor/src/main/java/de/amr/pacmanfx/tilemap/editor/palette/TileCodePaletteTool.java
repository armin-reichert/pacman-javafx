/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.actions.Action_SetTileCode;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import de.amr.pacmanfx.uilib.tilemap.TileRenderer;
import javafx.scene.paint.Color;

import java.util.function.BiConsumer;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.TOOL_SIZE;

public class TileCodePaletteTool implements PaletteTool {

    private final byte code;
    private final String description;

    private final BiConsumer<LayerID, Vector2i> editor;

    public TileCodePaletteTool(EditorUI ui, byte code, String description) {
        this.code = code;
        this.description = description;
        editor = (layerID, tile) -> new Action_SetTileCode(ui, layerID, tile, code).execute();
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public BiConsumer<LayerID, Vector2i> editor() {
        return editor;
    }

    @Override
    public void draw(CanvasRenderer renderer, int row, int col) {
        renderer.ctx().setFill(Color.BLACK);
        renderer.ctx().fillRect(col * TOOL_SIZE, row * TOOL_SIZE, TOOL_SIZE, TOOL_SIZE);
        if (renderer instanceof TileRenderer tileRenderer) {
            tileRenderer.drawTile(new Vector2i(col, row), code);
        }
    }
}
