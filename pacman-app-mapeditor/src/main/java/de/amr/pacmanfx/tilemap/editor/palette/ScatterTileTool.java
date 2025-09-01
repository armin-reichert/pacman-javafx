/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.actions.Action_SetTerrainProperty;
import de.amr.pacmanfx.tilemap.editor.rendering.TerrainTileMapRenderer;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.tilemap.WorldMapFormatter.formatTile;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.TOOL_SIZE;

public class ScatterTileTool extends PropertyValueEditorTool {

    protected ScatterTileTool(EditorUI ui, String propertyName, String description) {
        super(propertyName, description);
        editor = (layerID, tile) -> new Action_SetTerrainProperty(ui.editor(), propertyName, formatTile(tile)).execute();
    }

    @Override
    public void draw(CanvasRenderer renderer, int row, int col) {
        GraphicsContext g = renderer.ctx();
        g.setFill(Color.BLACK);
        g.fillRect(col * TOOL_SIZE, row * TOOL_SIZE, TOOL_SIZE, TOOL_SIZE);
        if (renderer instanceof TerrainTileMapRenderer tr) {
            g.save();
            g.setImageSmoothing(true);
            g.scale(TOOL_SIZE / (double) TS, TOOL_SIZE / (double) TS);
            Vector2i tile = new Vector2i(col, row);
            switch (propertyName) {
                case WorldMapProperty.POS_SCATTER_RED_GHOST -> tr.drawScatterTarget(tile, Color.RED);
                case WorldMapProperty.POS_SCATTER_PINK_GHOST -> tr.drawScatterTarget(tile, Color.PINK);
                case WorldMapProperty.POS_SCATTER_CYAN_GHOST -> tr.drawScatterTarget(tile, Color.CYAN);
                case WorldMapProperty.POS_SCATTER_ORANGE_GHOST -> tr.drawScatterTarget(tile, Color.ORANGE);
                default -> {}
            }
            g.restore();
        }
    }

}
