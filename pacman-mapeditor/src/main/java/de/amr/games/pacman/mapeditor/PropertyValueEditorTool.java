/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;

/**
 * @author Armin Reichert
 */
public class PropertyValueEditorTool implements Tool {
    private final TileMapRenderer renderer;
    private final double size;
    private final String propertyName;
    private final String description;

    public PropertyValueEditorTool(TileMapRenderer renderer, double size, String propertyName, String description) {
        this.renderer = renderer;
        this.size = size;
        this.propertyName = propertyName;
        this.description = description;
    }

    @Override
    public TileMapRenderer renderer() {
        return renderer;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public void apply(TileMap tileMap, Vector2i tile) {
        tileMap.setProperty(propertyName, TileMap.formatTile(tile));
    }

    @Override
    public void draw(GraphicsContext g, int row, int col) {
        g.setFill(Color.BLACK);
        g.fillRect(col * size, row * size, size, size);
        if (renderer instanceof TerrainMapEditRenderer tr) {
            g.save();
            g.scale(size / (double) TS, size / (double) TS);
            tr.drawSpecialTile(g, propertyName, new Vector2i(col, row));
            g.restore();
        }
    }
}
