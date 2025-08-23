/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class FoodMapRenderer extends BaseRenderer implements TileMapRenderer {

    private static final double PELLET_SIZE = 2;
    private static final double ENERGIZER_SIZE = 8;

    private final ObjectProperty<Color> pelletColorPy = new SimpleObjectProperty<>(Color.PINK);
    private final ObjectProperty<Color> energizerColorPy = new SimpleObjectProperty<>(Color.YELLOW);

    public ObjectProperty<Color> pelletColorProperty() { return pelletColorPy; }
    public ObjectProperty<Color> energizerColorProperty() { return energizerColorPy; }

    public FoodMapRenderer(Canvas canvas) {
        super(canvas);
    }
    public void setEnergizerColor(Color color) {
        energizerColorPy.set(requireNonNull(color));
    }

    public void setPelletColor(Color color) {
        pelletColorPy.set(requireNonNull(color));
    }

    @Override
    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        if      (content == FoodTile.PELLET.code()) drawPellet(g, tile);
        else if (content == FoodTile.ENERGIZER.code()) drawEnergizer(g, tile);
    }

    public void drawPellet(GraphicsContext g, Vector2i tile) {
        double offset = 0.5 * (TS - PELLET_SIZE);
        g.save();
        g.scale(scaling(), scaling());
        g.setFill(pelletColorPy.get());
        g.fillRect(tile.x() * TS + offset, tile.y() * TS + offset, PELLET_SIZE, PELLET_SIZE);
        g.restore();
    }

    public void drawEnergizer(GraphicsContext g, Vector2i tile) {
        double offset = 0.5 * HTS;
        double x = tile.x() * TS, y = tile.y() * TS;
        g.save();
        g.scale(scaling(), scaling());
        g.setFill(energizerColorPy.get());
        // draw pixelated "circle"
        g.fillRect(x + offset, y, HTS, ENERGIZER_SIZE);
        g.fillRect(x, y + offset, ENERGIZER_SIZE, HTS);
        g.fillRect(x + 1, y + 1, ENERGIZER_SIZE - 2, ENERGIZER_SIZE - 2);
        g.restore();
    }
}