/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

/**
 * @author Armin Reichert
 */
public abstract class TileMapRenderer {

    protected float scaling = 1.0f;

    public void setScaling(double scaling) {
        this.scaling = (float) scaling;
    }

    protected float s(float times) {
        return scaling * times;
    }

    public void drawMap(GraphicsContext g, TileMap map) {
        map.tiles().forEach(tile -> drawTile(g, tile, map.content(tile)));
    }

    public abstract void drawTile(GraphicsContext g, Vector2i tile, byte content);

}