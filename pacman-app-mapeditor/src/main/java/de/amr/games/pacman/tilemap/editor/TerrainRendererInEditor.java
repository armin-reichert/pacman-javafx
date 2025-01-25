/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.*;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.tilemap.TileMap.parseVector2i;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;

/**
 * @author Armin Reichert
 */
public class TerrainRendererInEditor extends TerrainRenderer {

    private static final Color SEGMENT_NUMBER_COLOR = Color.GRAY;
    private static final double SEGMENT_NUMBER_FONT_SIZE = 4;
    private static final Font SEGMENT_NUMBER_FONT = Font.font("Sans", FontWeight.BOLD, SEGMENT_NUMBER_FONT_SIZE);

    private boolean segmentNumbersDisplayed;

    private final double[] xp = new double[3];
    private final double[] yp = new double[3];

    public TerrainRendererInEditor() {}

    public void setSegmentNumbersDisplayed(boolean segmentNumbersDisplayed) {
        this.segmentNumbersDisplayed = segmentNumbersDisplayed;
    }

    @Override
    public void drawTerrain(GraphicsContext g, TileMap terrainMap, List<Obstacle> obstacles) {
        g.save();
        g.scale(scaling(), scaling());
        terrainMap.tiles().forEach(tile -> drawTileUnscaled(g, tile, terrainMap.get(tile)));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_RED_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.RED));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_PINK_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.PINK));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_CYAN_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.CYAN));
        specialTile(terrainMap, PROPERTY_POS_SCATTER_ORANGE_GHOST).ifPresent(tile -> drawScatterTarget(g, tile, Color.ORANGE));
        if (segmentNumbersDisplayed) {
            obstacles.forEach(obstacle -> {
                for (int i = 0; i < obstacle.numSegments(); ++i) {
                    ObstacleSegment segment = obstacle.segment(i);
                    Vector2i start = segment.startPoint(), end = segment.endPoint(), middle = start.midpoint(end);
                    g.setFont(SEGMENT_NUMBER_FONT);
                    g.setFill(SEGMENT_NUMBER_COLOR);
                    g.fillText(String.valueOf(i), middle.x() - 0.5 * SEGMENT_NUMBER_FONT_SIZE, middle.y());
                    g.setStroke(Color.WHITE);
                    g.setLineWidth(0.1);
                    g.strokeText(String.valueOf(i), middle.x() - 0.5 * SEGMENT_NUMBER_FONT_SIZE, middle.y());
                }

                Vector2i prev = null;
                double r = 1;
                for (Vector2i p : PolygonToRectSet.computeInnerPoints(obstacle)) {
                    g.setFill(Color.RED);
                    g.setStroke(Color.RED);
                    g.setLineWidth(1);
                    g.fillOval(p.x() - r, p.y() - r, 2*r, 2*r);
                    if (prev != null) {
                        g.strokeLine(prev.x(), prev.y(), p.x(), p.y());
                    }
                    prev = p;
                }

            });
        }
        g.restore();
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
            Vector2i tile = parseVector2i(terrainMap.getStringProperty(propertyName));
            return Optional.ofNullable(tile);
        }
        return Optional.empty();
    }

    private void drawTileUnscaled(GraphicsContext g, Vector2i tile, byte content) {
        switch (content) {
            case TileEncoding.WALL -> drawWall(g, tile);
            case TileEncoding.WALL_H -> drawWallH(g, tile);
            case TileEncoding.WALL_V -> drawWallV(g, tile);
            case TileEncoding.DWALL_H -> drawDWallH(g, tile);
            case TileEncoding.DWALL_V -> drawDWallV(g, tile);
            case TileEncoding.CORNER_NW, TileEncoding.CORNER_NE, TileEncoding.CORNER_SW, TileEncoding.CORNER_SE -> drawCorner(g, tile, content);
            case TileEncoding.DCORNER_NW, TileEncoding.DCORNER_NE, TileEncoding.DCORNER_SW, TileEncoding.DCORNER_SE -> drawDCorner(g, tile, content);
            case TileEncoding.DCORNER_ANGULAR_NW, TileEncoding.DCORNER_ANGULAR_NE, TileEncoding.DCORNER_ANGULAR_SW, TileEncoding.DCORNER_ANGULAR_SE
                    -> drawDCornerAngular(g, tile, content, xp, yp);
            case TileEncoding.DOOR -> drawDoor(g, tile, colors.doorColor());
            case TileEncoding.TUNNEL -> drawTunnel(g, tile);
            case TileEncoding.ONE_WAY_UP    -> drawOneWaySign(g, tile, Direction.UP);
            case TileEncoding.ONE_WAY_RIGHT -> drawOneWaySign(g, tile, Direction.RIGHT);
            case TileEncoding.ONE_WAY_DOWN  -> drawOneWaySign(g, tile, Direction.DOWN);
            case TileEncoding.ONE_WAY_LEFT  -> drawOneWaySign(g, tile, Direction.LEFT);
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

    private void drawWall(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(colors.wallFillColor());
        g.fillRect(x, y, TS, TS);
    }

    private void drawWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(colors.wallStrokeColor());
        g.fillRect(x, y + 3.5f, TS, 1);
    }

    private void drawDWallH(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(colors.wallStrokeColor());
        // add 1 pixel to avoid gaps
        g.fillRect(x, y + 2.5f, TS, 1);
        g.fillRect(x, y + 4.5f, TS, 1);
    }

    private void drawWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(colors.wallStrokeColor());
        // add 1 pixel to avoid gaps
        g.fillRect(x + 3.5f, y, 1, TS);
    }

    private void drawDWallV(GraphicsContext g, Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setFill(colors.wallStrokeColor());
        g.fillRect(x + 2.5f, y, 1, TS);
        g.fillRect(x + 4.5f, y, 1, TS);
    }

    private void drawCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setStroke(colors.wallStrokeColor());
        g.setLineWidth(1);
        switch (cornerType) {
            case TileEncoding.CORNER_NW -> g.strokeArc(x + 4, y + 4, TS, TS, 90, 90,  ArcType.OPEN);
            case TileEncoding.CORNER_NE -> g.strokeArc(x - 4, y + 4, TS, TS, 0, 90,   ArcType.OPEN);
            case TileEncoding.CORNER_SE -> g.strokeArc(x - 4, y - 4, TS, TS, 270, 90, ArcType.OPEN);
            case TileEncoding.CORNER_SW -> g.strokeArc(x + 4, y - 4, TS, TS, 180, 90, ArcType.OPEN);
            default -> {}
        }
    }

    private void drawDCorner(GraphicsContext g, Vector2i tile, byte cornerType) {
        double x = tile.x() * TS, y = tile.y() * TS;
        g.setStroke(colors.wallStrokeColor());
        g.setLineWidth(1);
        switch (cornerType) {
            case TileEncoding.DCORNER_NW -> {
                g.strokeArc(x + 5, y + 5, 6, 6, 90, 90, ArcType.OPEN);
                g.strokeArc(x + 3, y + 3, 10, 10, 90, 90, ArcType.OPEN);
            }
            case TileEncoding.DCORNER_NE -> {
                g.strokeArc(x - 3, y + 5, 6, 6, 0, 90, ArcType.OPEN);
                g.strokeArc(x - 5, y + 3, 10, 10, 0, 90, ArcType.OPEN);
            }
            case TileEncoding.DCORNER_SE -> {
                g.strokeArc(x - 3, y - 3, 6, 6, 270, 90, ArcType.OPEN);
                g.strokeArc(x - 5, y - 5, 10, 10, 270, 90, ArcType.OPEN);
            }
            case TileEncoding.DCORNER_SW -> {
                g.strokeArc(x + 5, y - 3, 6, 6, 180, 90, ArcType.OPEN);
                g.strokeArc(x + 3, y - 5, 10, 10, 180, 90, ArcType.OPEN);
            }
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
            case TileEncoding.DCORNER_ANGULAR_NW -> {
                xp[0]=xp[1]=cx-d; xp[2]=rightEdge;
                yp[0]=bottomEdge; yp[1]=yp[2]=cy-d;
                g.strokePolyline(xp,yp,xp.length);

                xp[0]=xp[1]=cx+d; xp[2]=rightEdge;
                yp[0]=bottomEdge; yp[1]=yp[2]=cy+d;
                g.strokePolyline(xp,yp,xp.length);
            }
            case TileEncoding.DCORNER_ANGULAR_NE -> {
                xp[0]=x; xp[1]=xp[2]=cx+d;
                yp[0]=yp[1]=cy-d; yp[2]=bottomEdge;
                g.strokePolyline(xp,yp,xp.length);

                xp[0]=x; xp[1]=xp[2]=cx-d;
                yp[0]=yp[1]=cy+d; yp[2]=bottomEdge;
                g.strokePolyline(xp,yp,xp.length);
            }
            case TileEncoding.DCORNER_ANGULAR_SE -> {
                xp[0]=x; xp[1]=xp[2]=cx-d;
                yp[0]=yp[1]=cy-d; yp[2]=y;
                g.strokePolyline(xp,yp,xp.length);

                xp[0]=x; xp[1]=xp[2]=cx+d;
                yp[0]=yp[1]=cy+d; yp[2]=y;
                g.strokePolyline(xp,yp,xp.length);
            }
            case TileEncoding.DCORNER_ANGULAR_SW -> {
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