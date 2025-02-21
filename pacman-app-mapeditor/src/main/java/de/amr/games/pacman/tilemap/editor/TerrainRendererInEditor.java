/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.Obstacle;
import de.amr.games.pacman.lib.tilemap.ObstacleSegment;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.tilemap.TileMap.parseTile;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;

/**
 * @author Armin Reichert
 */
public class TerrainRendererInEditor extends TerrainRenderer {

    private static final Color[] RANDOM_COLORS = new Color[50];
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

    public TerrainRendererInEditor() {}

    public void setSegmentNumbersDisplayed(boolean segmentNumbersDisplayed) {
        this.segmentNumbersDisplayed = segmentNumbersDisplayed;
    }

    public void setObstacleInnerAreaDisplayed(boolean obstacleInnerAreaDisplayed) {
        this.obstacleInnerAreaDisplayed = obstacleInnerAreaDisplayed;
    }

    @Override
    public void drawTerrain(GraphicsContext g, TileMap terrainMap, Set<Obstacle> obstacles) {
        g.save();
        g.scale(scaling(), scaling());
        terrainMap.tiles().forEach(tile -> drawTileUnscaled(g, tile, terrainMap.get(tile)));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_RED_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.RED));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_PINK_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.PINK));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_CYAN_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.CYAN));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_ORANGE_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.ORANGE));
        if (segmentNumbersDisplayed) {
            obstacles.stream().filter(obstacle -> !startsAtBorder(obstacle, terrainMap)).forEach(obstacle -> {
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
                    .filter(obstacle -> !startsAtBorder(obstacle, terrainMap)).forEach(obstacle -> {
                Vector2i prev = null;
                List<RectArea> rectangles = obstacle.innerAreaRectPartition().toList();
                for (int i = 0; i < rectangles.size(); ++i) {
                    RectArea rect = rectangles.get(i);
                    g.setFill(RANDOM_COLORS[i]);
                    g.fillRect(rect.x(), rect.y(), rect.width(), rect.height());
                }
                g.setFill(Color.grayRgb(200));
                g.setStroke(Color.grayRgb(200));
                g.setLineWidth(0.5);
                for (Vector2i p : obstacle.computeInnerPolygonPoints()) {
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
        Vector2i houseMinTile = terrainMap.getTileProperty(PROPERTY_POS_HOUSE_MIN_TILE, null);
        Vector2i houseMaxTile = terrainMap.getTileProperty(PROPERTY_POS_HOUSE_MAX_TILE, null);
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

    public void drawActorRelatedTile(GraphicsContext g, String propertyName, Vector2i tile) {
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
            return parseTile(terrainMap.getStringProperty(propertyName));
        }
        return Optional.empty();
    }

    private void drawTileUnscaled(GraphicsContext g, Vector2i tile, byte content) {
        switch (content) {
            case TerrainTiles.WALL_H -> drawWallH(g, tile);
            case TerrainTiles.WALL_V -> drawWallV(g, tile);
            case TerrainTiles.CORNER_NW, TerrainTiles.CORNER_NE, TerrainTiles.CORNER_SW, TerrainTiles.CORNER_SE -> drawCorner(g, tile, content);
            case TerrainTiles.DCORNER_ANGULAR_NW, TerrainTiles.DCORNER_ANGULAR_NE, TerrainTiles.DCORNER_ANGULAR_SW, TerrainTiles.DCORNER_ANGULAR_SE
                    -> drawDCornerAngular(g, tile, content, xp, yp);
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

    private void drawScatterTarget(GraphicsContext g, Vector2i tile, Color color) {
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

    private void drawPacHome(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(Color.YELLOW);
        g.fillOval(x, y, TS, TS);
    }

    private void drawGhostHome(GraphicsContext g, Vector2i tile, Color color) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(color);
        g.fillOval(x, y, TS, TS);
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

    private void drawCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setStroke(colors.wallStrokeColor());
        g.setLineWidth(1);
        switch (cornerType) {
            case TerrainTiles.CORNER_NW -> g.strokeArc(x + 4, y + 4, TS, TS, 90, 90,  ArcType.OPEN);
            case TerrainTiles.CORNER_NE -> g.strokeArc(x - 4, y + 4, TS, TS, 0, 90,   ArcType.OPEN);
            case TerrainTiles.CORNER_SE -> g.strokeArc(x - 4, y - 4, TS, TS, 270, 90, ArcType.OPEN);
            case TerrainTiles.CORNER_SW -> g.strokeArc(x + 4, y - 4, TS, TS, 180, 90, ArcType.OPEN);
            default -> {}
        }
    }

    private void drawDCornerAngular(GraphicsContext g, Vector2i tile, byte cornerType, double[] xp, double[] yp) {
        double x = tile.x() * TS, y = tile.y() * TS;
        double cx = x + HTS, cy = y + HTS;
        double rightEdge = x + TS, bottomEdge = y + TS;
        double d = 1;
        g.setStroke(colors.wallStrokeColor());
        g.setLineWidth(1);
        switch (cornerType) {
            case TerrainTiles.DCORNER_ANGULAR_NW -> {
                xp[0]=xp[1]=cx-d; xp[2]=rightEdge;
                yp[0]=bottomEdge; yp[1]=yp[2]=cy-d;
                g.strokePolyline(xp,yp,xp.length);

                xp[0]=xp[1]=cx+d; xp[2]=rightEdge;
                yp[0]=bottomEdge; yp[1]=yp[2]=cy+d;
                g.strokePolyline(xp,yp,xp.length);
            }
            case TerrainTiles.DCORNER_ANGULAR_NE -> {
                xp[0]=x; xp[1]=xp[2]=cx+d;
                yp[0]=yp[1]=cy-d; yp[2]=bottomEdge;
                g.strokePolyline(xp,yp,xp.length);

                xp[0]=x; xp[1]=xp[2]=cx-d;
                yp[0]=yp[1]=cy+d; yp[2]=bottomEdge;
                g.strokePolyline(xp,yp,xp.length);
            }
            case TerrainTiles.DCORNER_ANGULAR_SE -> {
                xp[0]=x; xp[1]=xp[2]=cx-d;
                yp[0]=yp[1]=cy-d; yp[2]=y;
                g.strokePolyline(xp,yp,xp.length);

                xp[0]=x; xp[1]=xp[2]=cx+d;
                yp[0]=yp[1]=cy+d; yp[2]=y;
                g.strokePolyline(xp,yp,xp.length);
            }
            case TerrainTiles.DCORNER_ANGULAR_SW -> {
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
}