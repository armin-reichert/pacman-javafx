/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.Tiles;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.Optional;

import static de.amr.games.pacman.lib.tilemap.TileMap.parseVector2i;
import static de.amr.games.pacman.mapeditor.TileMapEditor.*;
import static de.amr.games.pacman.mapeditor.TileMapUtil.TILE_SIZE;

/**
 * @author Armin Reichert
 */
public class TerrainMapEditRenderer implements TileMapRenderer {

    public DoubleProperty scalingPy = new SimpleDoubleProperty(this, "scaling", 1);

    protected Color wallFillColor = Color.BLACK;
    protected Color wallStrokeColor = Color.GREEN;
    protected Color doorColor = Color.PINK;

    @Override
    public void setScaling(double scaling) {
        scalingPy.set((float) scaling);
    }

    public double scaling() {
        return scalingPy.get();
    }

    public void setWallStrokeColor(Color color) {
        wallStrokeColor = color;
    }

    public void setWallFillColor(Color wallFillColor) {
        this.wallFillColor = wallFillColor;
    }

    public void setDoorColor(Color doorColor) {
        this.doorColor = doorColor;
    }

    @Override
    public void drawMap(GraphicsContext g, TileMap terrainMap) {
        g.save();
        g.scale(scaling(), scaling());
        terrainMap.tiles().forEach(tile -> drawTileUnscaled(g, tile, terrainMap.get(tile)));
        specialTile(terrainMap, PROPERTY_POS_PAC).ifPresent(tile -> drawPacHome(g, tile));
        specialTile(terrainMap, PROPERTY_POS_RED_GHOST).ifPresent(tile -> drawGhostHome(g, tile, Color.RED));
        specialTile(terrainMap, PROPERTY_POS_PINK_GHOST).ifPresent(tile -> drawGhostHome(g, tile, Color.PINK));
        specialTile(terrainMap, PROPERTY_POS_CYAN_GHOST).ifPresent(tile -> drawGhostHome(g, tile, Color.CYAN));
        specialTile(terrainMap, PROPERTY_POS_ORANGE_GHOST).ifPresent(tile -> drawGhostHome(g, tile, Color.ORANGE));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_RED_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.RED));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_PINK_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.PINK));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_CYAN_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.CYAN));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_ORANGE_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.ORANGE));
        g.restore();
    }

    @Override
    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        g.save();
        g.scale(scaling(), scaling());
        drawTileUnscaled(g, tile, content);
        g.restore();
    }

    public void drawSpecialTile(GraphicsContext g, String propertyName, Vector2i tile) {
        switch (propertyName) {
            case PROPERTY_POS_PAC -> drawPacHome(g, tile);
            case PROPERTY_POS_RED_GHOST -> drawGhostHome(g, tile, Color.RED);
            case PROPERTY_POS_PINK_GHOST -> drawGhostHome(g, tile, Color.PINK);
            case PROPERTY_POS_CYAN_GHOST -> drawGhostHome(g, tile, Color.CYAN);
            case PROPERTY_POS_ORANGE_GHOST -> drawGhostHome(g, tile, Color.ORANGE);
            case PROPERTY_POS_SCATTER_RED_GHOST -> drawScatterTarget(g, tile, Color.RED);
            case PROPERTY_POS_SCATTER_PINK_GHOST -> drawScatterTarget(g, tile, Color.PINK);
            case PROPERTY_POS_SCATTER_CYAN_GHOST -> drawScatterTarget(g, tile, Color.CYAN);
            case PROPERTY_POS_SCATTER_ORANGE_GHOST -> drawScatterTarget(g, tile, Color.ORANGE);
            default -> {}
        }
    }

    private Optional<Vector2i> specialTile(TileMap terrainMap, String propertyName) {
        if (terrainMap.hasProperty(propertyName)) {
            Vector2i tile = parseVector2i(terrainMap.getProperty(propertyName));
            return Optional.ofNullable(tile);
        }
        return Optional.empty();
    }

    private void drawTileUnscaled(GraphicsContext g, Vector2i tile, byte content) {
        switch (content) {
            case Tiles.WALL -> drawWall(g, tile);
            case Tiles.WALL_H -> drawWallH(g, tile);
            case Tiles.WALL_V -> drawWallV(g, tile);
            case Tiles.DWALL_H -> drawDWallH(g, tile);
            case Tiles.DWALL_V -> drawDWallV(g, tile);
            case Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE -> drawCorner(g, tile, content);
            case Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE ->
                    drawDCorner(g, tile, content);
            case Tiles.DOOR -> drawDoor(g, tile, doorColor);
            case Tiles.TUNNEL -> drawTunnel(g, tile);
            default -> {}
        }
    }

    private void drawDoor(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        double height = TILE_SIZE * 0.25;
        g.setFill(color);
        g.fillRect(x, y + 0.5 * (TILE_SIZE - height), TILE_SIZE, height);
    }

    private void drawScatterTarget(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(color);
        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        g.setStroke(Color.WHITE);
        g.setLineWidth(0.5);
        g.strokeOval(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        g.strokeLine(x + 0.5 * TILE_SIZE, y, x + 0.5 * TILE_SIZE, y + TILE_SIZE);
        g.strokeLine(x, y + 0.5 * TILE_SIZE, x + TILE_SIZE, y + 0.5 * TILE_SIZE);
    }

    private void drawPacHome(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(Color.YELLOW);
        g.fillOval(x, y, TILE_SIZE, TILE_SIZE);
    }

    private void drawGhostHome(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(color);
        g.fillOval(x, y, TILE_SIZE, TILE_SIZE);
    }

    private void drawTunnel(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(Color.GRAY);
        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    private void drawWall(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(wallFillColor);
        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
    }

    private void drawWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(wallStrokeColor);
        g.fillRect(x, y + 3.5f, TILE_SIZE, 1);
    }

    private void drawDWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(wallStrokeColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + 2.5f, TILE_SIZE, 1);
        g.fillRect(x, y + 4.5f, TILE_SIZE, 1);
    }

    private void drawWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(wallStrokeColor);
        // add 1 pixel to avoid gaps
        g.fillRect(x + 3.5f, y, 1, TILE_SIZE);
    }

    private void drawDWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setFill(wallStrokeColor);
        g.fillRect(x + 2.5f, y, 1, TILE_SIZE);
        g.fillRect(x + 4.5f, y, 1, TILE_SIZE);
    }

    private void drawCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setStroke(wallStrokeColor);
        g.setLineWidth(1);
        switch (cornerType) {
            case Tiles.CORNER_NW -> g.strokeArc(x + 4, y + 4, TILE_SIZE, TILE_SIZE, 90, 90,  ArcType.OPEN);
            case Tiles.CORNER_NE -> g.strokeArc(x - 4, y + 4, TILE_SIZE, TILE_SIZE, 0, 90,   ArcType.OPEN);
            case Tiles.CORNER_SE -> g.strokeArc(x - 4, y - 4, TILE_SIZE, TILE_SIZE, 270, 90, ArcType.OPEN);
            case Tiles.CORNER_SW -> g.strokeArc(x + 4, y - 4, TILE_SIZE, TILE_SIZE, 180, 90, ArcType.OPEN);
            default -> {}
        }
    }

    private void drawDCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * TILE_SIZE, y = tile.y() * TILE_SIZE;
        g.setStroke(wallStrokeColor);
        g.setLineWidth(1);
        switch (cornerType) {
            case Tiles.DCORNER_NW -> {
                g.strokeArc(x + 5, y + 5, 6, 6, 90, 90, ArcType.OPEN);
                g.strokeArc(x + 3, y + 3, 10, 10, 90, 90, ArcType.OPEN);
            }
            case Tiles.DCORNER_NE -> {
                g.strokeArc(x - 3, y + 5, 6, 6, 0, 90, ArcType.OPEN);
                g.strokeArc(x - 5, y + 3, 10, 10, 0, 90, ArcType.OPEN);
            }
            case Tiles.DCORNER_SE -> {
                g.strokeArc(x - 3, y - 3, 6, 6, 270, 90, ArcType.OPEN);
                g.strokeArc(x - 5, y - 5, 10, 10, 270, 90, ArcType.OPEN);
            }
            case Tiles.DCORNER_SW -> {
                g.strokeArc(x + 5, y - 3, 6, 6, 180, 90, ArcType.OPEN);
                g.strokeArc(x + 3, y - 5, 10, 10, 180, 90, ArcType.OPEN);
            }
            default -> {}
        }
    }
}