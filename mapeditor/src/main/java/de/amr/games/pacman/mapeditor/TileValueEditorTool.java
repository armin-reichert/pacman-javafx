/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class TileValueEditorTool implements Tool {
    private final TileMapRenderer renderer;
    private final int size;
    private final byte value;
    private final String description;

    public TileValueEditorTool(TileMapRenderer renderer, int size, byte value, String description) {
        this.renderer = renderer;
        this.size = size;
        this.value = value;
        this.description = description;
    }

    @Override
    public int size() {
        return size;
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
        tileMap.set(tile, value);
    }

    @Override
    public void draw(GraphicsContext g, int row, int col) {
        g.setFill(Color.BLACK);
        g.fillRect(col * size, row * size, size, size);
        renderer.drawTile(g, new Vector2i(col, row), value);
    }
}
