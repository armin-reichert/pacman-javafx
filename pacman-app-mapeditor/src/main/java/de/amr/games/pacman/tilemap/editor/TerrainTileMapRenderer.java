/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.*;
import de.amr.games.pacman.uilib.tilemap.TerrainMapRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.tilemap.editor.ArcadeMap.SPRITE_SHEET;

/**
 * Tile-based renderer used in editor. At runtime and in the 2D editor preview, the path based renderer is used.
 */
public class TerrainTileMapRenderer extends TerrainMapRenderer {

    private static final Color[] RANDOM_COLORS = new Color[30];
    static {
        for (int i = 0; i < RANDOM_COLORS.length; ++i) {
            RANDOM_COLORS[i] = Color.rgb(randomInt(0, 256),randomInt(0, 256), randomInt(0, 256));
        }
    }
    private static final Color SEGMENT_NUMBER_FILL_COLOR = Color.LIGHTGRAY;
    private static final Color SEGMENT_NUMBER_STROKE_COLOR = Color.BLACK;
    private static final double SEGMENT_NUMBER_FONT_SIZE = 4;
    private static final Font SEGMENT_NUMBER_FONT = Font.font("Sans", FontWeight.BOLD, SEGMENT_NUMBER_FONT_SIZE);

    private boolean segmentNumbersDisplayed;
    private boolean obstacleInnerAreaDisplayed;

    private final double[] xp = new double[3];
    private final double[] yp = new double[3];

    public TerrainTileMapRenderer() {}

    public void setSegmentNumbersDisplayed(boolean segmentNumbersDisplayed) {
        this.segmentNumbersDisplayed = segmentNumbersDisplayed;
    }

    public void setObstacleInnerAreaDisplayed(boolean obstacleInnerAreaDisplayed) {
        this.obstacleInnerAreaDisplayed = obstacleInnerAreaDisplayed;
    }

    @Override
    public void drawTerrain(GraphicsContext g, WorldMap worldMap, Set<Obstacle> obstacles) {
        g.save();
        g.scale(scaling(), scaling());
        worldMap.tiles().forEach(tile -> drawTileUnscaled(g, tile, worldMap.get(LayerID.TERRAIN, tile)));
        specialTile(worldMap, PROPERTY_POS_SCATTER_RED_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.RED));
        specialTile(worldMap, PROPERTY_POS_SCATTER_PINK_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.PINK));
        specialTile(worldMap, PROPERTY_POS_SCATTER_CYAN_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.CYAN));
        specialTile(worldMap, PROPERTY_POS_SCATTER_ORANGE_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.ORANGE));
        if (segmentNumbersDisplayed) {
            obstacles.stream().filter(obstacle -> !startsAtBorder(obstacle, worldMap)).forEach(obstacle -> {
                for (int i = 0; i < obstacle.numSegments(); ++i) {
                    ObstacleSegment segment = obstacle.segment(i);
                    Vector2f start = segment.startPoint().toVector2f();
                    Vector2f end = segment.endPoint().toVector2f();
                    Vector2f  middle = start.midpoint(end);
                    g.setFont(SEGMENT_NUMBER_FONT);
                    g.setFill(SEGMENT_NUMBER_FILL_COLOR);
                    g.setStroke(SEGMENT_NUMBER_STROKE_COLOR);
                    g.setLineWidth(0.1);
                    g.fillText(String.valueOf(i), middle.x() - 0.5 * SEGMENT_NUMBER_FONT_SIZE, middle.y());
                    g.strokeText(String.valueOf(i), middle.x() - 0.5 * SEGMENT_NUMBER_FONT_SIZE, middle.y());
                }
            });
        }
        if (obstacleInnerAreaDisplayed) {
            double r = 1;
            obstacles.stream()
                    .filter(Obstacle::isClosed)
                    .filter(obstacle -> !startsAtBorder(obstacle, worldMap)).forEach(obstacle -> {
                Vector2i prev = null;
                List<RectArea> rectangles = obstacle.innerAreaRectPartition().toList();
                for (int i = 0; i < rectangles.size(); ++i) {
                    RectArea rect = rectangles.get(i);
                    g.setFill(RANDOM_COLORS[i % RANDOM_COLORS.length]);
                    g.fillRect(rect.x(), rect.y(), rect.width(), rect.height());
                }
                g.setFill(Color.grayRgb(200));
                g.setStroke(Color.grayRgb(200));
                g.setLineWidth(0.5);
                for (Vector2i p : obstacle.computeInnerPolygon()) {
                    g.fillOval(p.x() - r, p.y() - r, 2*r, 2*r);
                    if (prev != null) {
                        g.strokeLine(prev.x(), prev.y(), p.x(), p.y());
                    }
                    prev = p;
                }
                for (int i = 0; i < rectangles.size(); ++i) {
                    RectArea rect = rectangles.get(i);
                    g.setFill(Color.WHITE);
                    g.setFont(Font.font("Sans", FontWeight.BOLD, 3.5));
                    g.fillText("R" + i, rect.x() + rect.width() * 0.5 - 3, rect.y() + rect.height() * 0.5 + 1);
                }
            });
        }
        g.restore();
        Vector2i houseMinTile = worldMap.getTerrainTileProperty(PROPERTY_POS_HOUSE_MIN_TILE, null);
        Vector2i houseMaxTile = worldMap.getTerrainTileProperty(PROPERTY_POS_HOUSE_MAX_TILE, null);
        if (houseMinTile != null && houseMaxTile != null) {
            drawHouse(g, houseMinTile, houseMaxTile.minus(houseMinTile).plus(1, 1));
        }
    }

    @Override
    public void drawTile(GraphicsContext g, Vector2i tile, byte content) {
        g.save();
        g.scale(scaling(), scaling());
        drawTileUnscaled(g, tile, content);
        g.restore();
    }

    private Optional<Vector2i> specialTile(WorldMap worldMap, String propertyName) {
        if (worldMap.hasProperty(LayerID.TERRAIN, propertyName)) {
            return parseTile(worldMap.getProperty(LayerID.TERRAIN, propertyName));
        }
        return Optional.empty();
    }

    private void drawTileUnscaled(GraphicsContext g, Vector2i tile, byte content) {
        switch (content) {
            case TerrainTiles.WALL_H -> drawWallH(g, tile);
            case TerrainTiles.WALL_V -> drawWallV(g, tile);
            case TerrainTiles.ARC_NW,
                 TerrainTiles.ARC_NE,
                 TerrainTiles.ARC_SW,
                 TerrainTiles.ARC_SE -> drawArc(g, tile, content);
            case TerrainTiles.DCORNER_NW,
                 TerrainTiles.DCORNER_NE,
                 TerrainTiles.DCORNER_SW,
                 TerrainTiles.DCORNER_SE -> drawDCorner(g, tile, content, xp, yp);
            case TerrainTiles.DOOR -> drawDoor(g, tile, colors.doorColor());
            case TerrainTiles.TUNNEL -> drawTunnel(g, tile);
            case TerrainTiles.ONE_WAY_UP    -> drawOneWaySign(g, tile, Direction.UP);
            case TerrainTiles.ONE_WAY_RIGHT -> drawOneWaySign(g, tile, Direction.RIGHT);
            case TerrainTiles.ONE_WAY_DOWN  -> drawOneWaySign(g, tile, Direction.DOWN);
            case TerrainTiles.ONE_WAY_LEFT  -> drawOneWaySign(g, tile, Direction.LEFT);
            default -> {}
        }
    }

    private void drawDoor(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * TS, y = tile.y() * TS;
        double height = TS * 0.25;
        g.setFill(color);
        g.fillRect(x, y + 0.5 * (TS - height), TS, height);
    }

    public void drawScatterTarget(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(color);
        g.fillRect(x, y, TS, TS);
        g.setStroke(Color.WHITE);
        g.setLineWidth(0.5);
        g.strokeOval(x + 2, y + 2, TS - 4, TS - 4);
        g.strokeLine(x + 0.5 * TS, y, x + 0.5 * TS, y + TS);
        g.strokeLine(x, y + 0.5 * TS, x + TS, y + 0.5 * TS);
    }

    private void drawOneWaySign(GraphicsContext g, Vector2i tile, Direction dir) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.save();
        g.beginPath();
        switch (dir) {
            case UP -> {
                g.moveTo(x, y+8);
                g.lineTo(x+4, y);
                g.lineTo(x+8,y+8);
            }
            case RIGHT -> {
                g.moveTo(x, y);
                g.lineTo(x+8, y+4);
                g.lineTo(x,y+8);
            }
            case DOWN -> {
                g.moveTo(x, y);
                g.lineTo(x+4, y+8);
                g.lineTo(x+8,y);
            }
            case LEFT -> {
                g.moveTo(x+8, y);
                g.lineTo(x, y+4);
                g.lineTo(x+8,y+8);

            }
        }
        g.closePath();
        g.setStroke(Color.grayRgb(222));
        g.setLineWidth(0.4);
        g.setLineDashes(0.5, 1);
        g.stroke();
        g.restore();
    }

    private void drawTunnel(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(Color.GRAY);
        g.fillRect(x, y, TS, TS);
    }

    private void drawWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(colors.wallStrokeColor());
        g.fillRect(x, y + 3.5f, TS, 1);
    }

    private void drawWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(colors.wallStrokeColor());
        // add 1 pixel to avoid gaps
        g.fillRect(x + 3.5f, y, 1, TS);
    }

    private void drawArc(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setStroke(colors.wallStrokeColor());
        g.setLineWidth(1);
        switch (cornerType) {
            case TerrainTiles.ARC_NW -> g.strokeArc(x + 4, y + 4, TS, TS, 90, 90,  ArcType.OPEN);
            case TerrainTiles.ARC_NE -> g.strokeArc(x - 4, y + 4, TS, TS, 0, 90,   ArcType.OPEN);
            case TerrainTiles.ARC_SE -> g.strokeArc(x - 4, y - 4, TS, TS, 270, 90, ArcType.OPEN);
            case TerrainTiles.ARC_SW -> g.strokeArc(x + 4, y - 4, TS, TS, 180, 90, ArcType.OPEN);
            default -> {}
        }
    }

    private void drawDCorner(GraphicsContext g, Vector2i tile, byte cornerType, double[] xp, double[] yp) {
        double x = tile.x() * TS, y = tile.y() * TS;
        double cx = x + HTS, cy = y + HTS;
        double rightEdge = x + TS, bottomEdge = y + TS;
        double d = 1;
        g.setStroke(colors.wallStrokeColor());
        g.setLineWidth(1);
        switch (cornerType) {
            case TerrainTiles.DCORNER_NW -> {
                xp[0]=xp[1]=cx-d; xp[2]=rightEdge;
                yp[0]=bottomEdge; yp[1]=yp[2]=cy-d;
                g.strokePolyline(xp,yp,xp.length);

                xp[0]=xp[1]=cx+d; xp[2]=rightEdge;
                yp[0]=bottomEdge; yp[1]=yp[2]=cy+d;
                g.strokePolyline(xp,yp,xp.length);
            }
            case TerrainTiles.DCORNER_NE -> {
                xp[0]=x; xp[1]=xp[2]=cx+d;
                yp[0]=yp[1]=cy-d; yp[2]=bottomEdge;
                g.strokePolyline(xp,yp,xp.length);

                xp[0]=x; xp[1]=xp[2]=cx-d;
                yp[0]=yp[1]=cy+d; yp[2]=bottomEdge;
                g.strokePolyline(xp,yp,xp.length);
            }
            case TerrainTiles.DCORNER_SE -> {
                xp[0]=x; xp[1]=xp[2]=cx-d;
                yp[0]=yp[1]=cy-d; yp[2]=y;
                g.strokePolyline(xp,yp,xp.length);

                xp[0]=x; xp[1]=xp[2]=cx+d;
                yp[0]=yp[1]=cy+d; yp[2]=y;
                g.strokePolyline(xp,yp,xp.length);
            }
            case TerrainTiles.DCORNER_SW -> {
                xp[0]=xp[1]=cx-d; xp[2]=rightEdge;
                yp[0]=y; yp[1]=yp[2]=cy+d;
                g.strokePolyline(xp,yp,xp.length);

                xp[0]=xp[1]=cx+d; xp[2]=rightEdge;
                yp[0]=y; yp[1]=yp[2]=cy-d;
                g.strokePolyline(xp,yp,xp.length);
            }
            default -> {}
        }
    }

    public void drawSpriteBetweenTiles(GraphicsContext g, Vector2i leftTile, RectArea sprite, double gridSize) {
        if (leftTile != null) {
            double spriteSize = 1.75 * gridSize;
            drawSpriteImage(g, sprite, gridSize,
                (leftTile.x() + 0.5) * gridSize, // sprite image is draw between left tile and right neighbor tile!
                leftTile.y() * gridSize,
                spriteSize, spriteSize);
        }
    }

    private void drawSpriteImage(GraphicsContext g, RectArea sprite, double gridSize, double x, double y, double w, double h) {
        double ox = 0.5 * (w - gridSize), oy = 0.5 * (h - gridSize);
        g.drawImage(SPRITE_SHEET, sprite.x(), sprite.y(), sprite.width(), sprite.height(), x - ox, y - oy, w, h);
    }
}