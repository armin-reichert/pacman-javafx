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
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class FoodMapRenderer extends BaseRenderer implements TileMapRenderer {

    private static final double PELLET_SIZE = 2;
    private static final double ENERGIZER_SIZE = 8;

    private final ObjectProperty<Color> pelletColor = new SimpleObjectProperty<>(Color.PINK);
    private final ObjectProperty<Color> energizerColor = new SimpleObjectProperty<>(Color.YELLOW);

    public ObjectProperty<Color> pelletColorProperty() { return pelletColor; }
    public ObjectProperty<Color> energizerColorProperty() { return energizerColor; }

    public FoodMapRenderer(Canvas canvas) {
        super(canvas);
    }
    public void setEnergizerColor(Color color) {
        energizerColor.set(requireNonNull(color));
    }

    public void setPelletColor(Color color) {
        pelletColor.set(requireNonNull(color));
    }

    @Override
    public void drawTile(Vector2i tile, byte content) {
        if      (content == FoodTile.PELLET.code()) drawPellet(tile);
        else if (content == FoodTile.ENERGIZER.code()) drawEnergizer(tile);
    }

    public void drawPellet(Vector2i tile) {
        double offset = 0.5 * (TS - PELLET_SIZE);
        ctx().save();
        ctx().scale(scaling(), scaling());
        ctx().setFill(pelletColor.get());
        ctx().fillRect(tile.x() * TS + offset, tile.y() * TS + offset, PELLET_SIZE, PELLET_SIZE);
        ctx().restore();
    }

    public void drawEnergizer(Vector2i tile) {
        double offset = 0.5 * HTS;
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx().save();
        ctx().scale(scaling(), scaling());
        ctx().setFill(energizerColor.get());
        // draw pixelated "circle"
        ctx().fillRect(x + offset, y, HTS, ENERGIZER_SIZE);
        ctx().fillRect(x, y + offset, ENERGIZER_SIZE, HTS);
        ctx().fillRect(x + 1, y + 1, ENERGIZER_SIZE - 2, ENERGIZER_SIZE - 2);
        ctx().restore();
    }
}