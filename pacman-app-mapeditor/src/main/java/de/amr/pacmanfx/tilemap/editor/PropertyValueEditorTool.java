/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites;
import de.amr.pacmanfx.tilemap.editor.rendering.TerrainTileMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TileRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites.PAC_MAN;
import static de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites.SPRITE_SHEET;

public class PropertyValueEditorTool implements EditorTool {
    private final double size;
    private final String propertyName;
    private final String description;

    public PropertyValueEditorTool(double size, String propertyName, String description) {
        this.size = size;
        this.propertyName = propertyName;
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public void apply(TileMapEditor editor, LayerID layerID, Vector2i tile) {
        editor.currentWorldMap().properties(layerID).put(propertyName, WorldMapFormatter.formatTile(tile));
    }

    @Override
    public void draw(TileRenderer renderer, int row, int col) {
        GraphicsContext g = renderer.ctx();
        g.setFill(Color.BLACK);
        g.fillRect(col * size, row * size, size, size);
        if (renderer instanceof TerrainTileMapRenderer tr) {
            g.save();
            g.setImageSmoothing(true);
            g.scale(size / (double) TS, size / (double) TS);
            Vector2i tile = new Vector2i(col, row);
            double x = col * TS, y = row * TS;
            switch (propertyName) {
                case WorldMapProperty.POS_PAC -> drawSprite(g, x, y, PAC_MAN);
                case WorldMapProperty.POS_RED_GHOST -> drawSprite(g, x, y, ArcadeSprites.RED_GHOST);
                case WorldMapProperty.POS_PINK_GHOST -> drawSprite(g, x, y, ArcadeSprites.PINK_GHOST);
                case WorldMapProperty.POS_CYAN_GHOST -> drawSprite(g, x, y, ArcadeSprites.CYAN_GHOST);
                case WorldMapProperty.POS_ORANGE_GHOST -> drawSprite(g, x, y, ArcadeSprites.ORANGE_GHOST);
                case WorldMapProperty.POS_BONUS -> drawSprite(g, x, y, ArcadeSprites.STRAWBERRY);
                case WorldMapProperty.POS_SCATTER_RED_GHOST -> tr.drawScatterTarget(tile, Color.RED);
                case WorldMapProperty.POS_SCATTER_PINK_GHOST -> tr.drawScatterTarget(tile, Color.PINK);
                case WorldMapProperty.POS_SCATTER_CYAN_GHOST -> tr.drawScatterTarget(tile, Color.CYAN);
                case WorldMapProperty.POS_SCATTER_ORANGE_GHOST -> tr.drawScatterTarget(tile, Color.ORANGE);
                default -> {}
            }
            g.restore();
        }
    }

    private void drawSprite(GraphicsContext g, double x, double y, RectShort sprite) {
        g.drawImage(SPRITE_SHEET,
            sprite.x(), sprite.y(), sprite.width(), sprite.height(),
            x + 1, y + 1, TS - 2, TS - 2);
    }
}
