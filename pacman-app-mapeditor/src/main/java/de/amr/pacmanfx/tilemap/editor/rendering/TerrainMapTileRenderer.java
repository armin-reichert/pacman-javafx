/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.rendering;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.*;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.rendering.BaseCanvasRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TileRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;

/**
 * Tile-based renderer used in editor. At runtime and in the 2D editor preview, the path based renderer is used.
 */
public class TerrainMapTileRenderer extends BaseCanvasRenderer implements TerrainMapRenderer, TileRenderer {

    private static final Color[] RANDOM_COLORS = new Color[30];
    static {
        for (int i = 0; i < RANDOM_COLORS.length; ++i) {
            RANDOM_COLORS[i] = Color.rgb(randomInt(0, 256), randomInt(0, 256), randomInt(0, 256));
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

    private final ObjectProperty<TerrainMapColorScheme> colorScheme = new SimpleObjectProperty<>(DEFAULT_COLOR_SCHEME);

    public TerrainMapTileRenderer(Canvas canvas) {
        super(canvas);
    }

    public void setColorScheme(TerrainMapColorScheme colorScheme) {
        this.colorScheme.set(colorScheme);
    }

    public TerrainMapColorScheme colorScheme() {
        return colorScheme.get();
    }

    @Override
    public ObjectProperty<TerrainMapColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    public void setSegmentNumbersDisplayed(boolean segmentNumbersDisplayed) {
        this.segmentNumbersDisplayed = segmentNumbersDisplayed;
    }

    public void setObstacleInnerAreaDisplayed(boolean obstacleInnerAreaDisplayed) {
        this.obstacleInnerAreaDisplayed = obstacleInnerAreaDisplayed;
    }

    @Override
    public void draw(WorldMap worldMap, Set<Obstacle> obstacles) {
        ctx().save();
        ctx().scale(scaling(), scaling());
        worldMap.tiles().forEach(tile -> {
            byte code = worldMap.content(LayerID.TERRAIN, tile);
            drawTileUnscaled(tile, code);
        });
        specialTile(worldMap, WorldMapProperty.POS_SCATTER_RED_GHOST).ifPresent(tile -> drawScatterTarget(tile, Color.RED));
        specialTile(worldMap, WorldMapProperty.POS_SCATTER_PINK_GHOST).ifPresent(tile -> drawScatterTarget(tile, Color.PINK));
        specialTile(worldMap, WorldMapProperty.POS_SCATTER_CYAN_GHOST).ifPresent(tile -> drawScatterTarget(tile, Color.CYAN));
        specialTile(worldMap, WorldMapProperty.POS_SCATTER_ORANGE_GHOST).ifPresent(tile -> drawScatterTarget(tile, Color.ORANGE));
        if (segmentNumbersDisplayed) {
            drawObstacleSegmentNumbers(worldMap, obstacles);
        }
        if (obstacleInnerAreaDisplayed) {
            drawObstacleInnerAreas(worldMap, obstacles);
        }
        ctx().restore();
    }

    private void drawObstacleInnerAreas(WorldMap worldMap, Set<Obstacle> obstacles) {
        double r = 1;
        obstacles.stream()
                .filter(Obstacle::isClosed)
                .filter(obstacle -> !Ufx.isBorderObstacle(worldMap, obstacle)).forEach(obstacle -> {
            Vector2i prev = null;
            List<RectShort> rectangles = obstacle.innerAreaRectangles();
            for (int i = 0; i < rectangles.size(); ++i) {
                RectShort rect = rectangles.get(i);
                ctx().setFill(RANDOM_COLORS[i % RANDOM_COLORS.length]);
                ctx().fillRect(rect.x(), rect.y(), rect.width(), rect.height());
            }
            ctx().setFill(Color.grayRgb(200));
            ctx().setStroke(Color.grayRgb(200));
            ctx().setLineWidth(0.5);
            for (Vector2i p : obstacle.computeInnerPolygon()) {
                ctx().fillOval(p.x() - r, p.y() - r, 2*r, 2*r);
                if (prev != null) {
                    ctx().strokeLine(prev.x(), prev.y(), p.x(), p.y());
                }
                prev = p;
            }
            for (int i = 0; i < rectangles.size(); ++i) {
                RectShort rect = rectangles.get(i);
                ctx().setFill(Color.WHITE);
                ctx().setFont(Font.font("Sans", FontWeight.BOLD, 3.5));
                ctx().fillText("R" + (i+1), rect.x() + rect.width() * 0.5 - 3, rect.y() + rect.height() * 0.5 + 1);
            }
        });
    }

    private void drawObstacleSegmentNumbers(WorldMap worldMap, Set<Obstacle> obstacles) {
        obstacles.stream().filter(obstacle -> !Ufx.isBorderObstacle(worldMap, obstacle)).forEach(obstacle -> {
            for (int i = 0; i < obstacle.numSegments(); ++i) {
                ObstacleSegment segment = obstacle.segment(i);
                Vector2f start = segment.startPoint().toVector2f();
                Vector2f end = segment.endPoint().toVector2f();
                Vector2f  middle = start.midpoint(end);
                ctx().setFont(SEGMENT_NUMBER_FONT);
                ctx().setFill(SEGMENT_NUMBER_FILL_COLOR);
                ctx().setStroke(SEGMENT_NUMBER_STROKE_COLOR);
                ctx().setLineWidth(0.1);
                ctx().fillText(String.valueOf(i), middle.x() - 0.5 * SEGMENT_NUMBER_FONT_SIZE, middle.y());
                ctx().strokeText(String.valueOf(i), middle.x() - 0.5 * SEGMENT_NUMBER_FONT_SIZE, middle.y());
            }
        });
    }

    @Override
    public void drawTile(Vector2i tile, byte content) {
        ctx().save();
        ctx().scale(scaling(), scaling());
        drawTileUnscaled(tile, content);
        ctx().restore();
    }

    private Optional<Vector2i> specialTile(WorldMap worldMap, String propertyName) {
        if (worldMap.properties(LayerID.TERRAIN).containsKey(propertyName)) {
            return WorldMapParser.parseTile(worldMap.properties(LayerID.TERRAIN).get(propertyName));
        }
        return Optional.empty();
    }

    private void drawTileUnscaled(Vector2i tile, byte code) {
        if       (code == WALL_H.$) drawWallH(tile);
        else  if (code == WALL_V.$) drawWallV(tile);

        else if (code == ARC_NW.$) drawArc(tile, code);
        else if (code == ARC_NE.$) drawArc(tile, code);
        else if (code == ARC_SW.$) drawArc(tile, code);
        else if (code == ARC_SE.$) drawArc(tile, code);

        else if (code == DARC_NW.$) drawDCorner(tile, code, xp, yp);
        else if (code == DARC_NE.$) drawDCorner(tile, code, xp, yp);
        else if (code == DARC_SW.$) drawDCorner(tile, code, xp, yp);
        else if (code == DARC_SE.$) drawDCorner(tile, code, xp, yp);

        else if (code == DOOR.$)   drawDoor(tile, colorScheme().doorColor());
        else if (code == TUNNEL.$) drawTunnel(tile);

        else if (code == ONE_WAY_UP.$)    drawOneWaySign(tile, Direction.UP);
        else if (code == ONE_WAY_RIGHT.$) drawOneWaySign(tile, Direction.RIGHT);
        else if (code == ONE_WAY_DOWN.$)  drawOneWaySign(tile, Direction.DOWN);
        else if (code == ONE_WAY_LEFT.$)  drawOneWaySign(tile, Direction.LEFT);
    }

    private void drawDoor(Vector2i tile, Color color) {
        double x = tile.x() * TS, y = tile.y() * TS;
        double height = TS * 0.25;
        ctx().setFill(color);
        ctx().fillRect(x, y + 0.5 * (TS - height), TS, height);
    }

    public void drawScatterTarget(Vector2i tile, Color color) {
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx().setFill(color);
        ctx().fillRect(x, y, TS, TS);
        ctx().setStroke(Color.WHITE);
        ctx().setLineWidth(0.5);
        ctx().strokeOval(x + 2, y + 2, TS - 4, TS - 4);
        ctx().strokeLine(x + 0.5 * TS, y, x + 0.5 * TS, y + TS);
        ctx().strokeLine(x, y + 0.5 * TS, x + TS, y + 0.5 * TS);
    }

    private void drawOneWaySign(Vector2i tile, Direction dir) {
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx().save();
        ctx().beginPath();
        switch (dir) {
            case UP -> {
                ctx().moveTo(x, y+8);
                ctx().lineTo(x+4, y);
                ctx().lineTo(x+8,y+8);
            }
            case RIGHT -> {
                ctx().moveTo(x, y);
                ctx().lineTo(x+8, y+4);
                ctx().lineTo(x,y+8);
            }
            case DOWN -> {
                ctx().moveTo(x, y);
                ctx().lineTo(x+4, y+8);
                ctx().lineTo(x+8,y);
            }
            case LEFT -> {
                ctx().moveTo(x+8, y);
                ctx().lineTo(x, y+4);
                ctx().lineTo(x+8,y+8);

            }
        }
        ctx().closePath();
        ctx().setStroke(Color.grayRgb(222));
        ctx().setLineWidth(0.4);
        ctx().setLineDashes(0.5, 1);
        ctx().stroke();
        ctx().restore();
    }

    private void drawTunnel(Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx().setFill(Color.GRAY);
        ctx().fillRect(x, y, TS, TS);
    }

    private void drawWallH(Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx().setFill(colorScheme().wallStrokeColor());
        ctx().fillRect(x, y + 3.5f, TS, 1);
    }

    private void drawWallV(Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx().setFill(colorScheme().wallStrokeColor());
        // add 1 pixel to avoid gaps
        ctx().fillRect(x + 3.5f, y, 1, TS);
    }

    private void drawArc(Vector2i tile, byte cornerType) {
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx().setStroke(colorScheme().wallStrokeColor());
        ctx().setLineWidth(1);
        if (cornerType == ARC_NW.$) ctx().strokeArc(x + 4, y + 4, TS, TS, 90, 90,  ArcType.OPEN);
        if (cornerType == ARC_NE.$) ctx().strokeArc(x - 4, y + 4, TS, TS, 0, 90,   ArcType.OPEN);
        if (cornerType == ARC_SE.$) ctx().strokeArc(x - 4, y - 4, TS, TS, 270, 90, ArcType.OPEN);
        if (cornerType == ARC_SW.$) ctx().strokeArc(x + 4, y - 4, TS, TS, 180, 90, ArcType.OPEN);
    }

    private void drawDCorner(Vector2i tile, byte cornerType, double[] xp, double[] yp) {
        double x = tile.x() * TS, y = tile.y() * TS;
        double cx = x + HTS, cy = y + HTS;
        double rightEdge = x + TS, bottomEdge = y + TS;
        double d = 1;
        ctx().setStroke(colorScheme().wallStrokeColor());
        ctx().setLineWidth(1);
        if (cornerType == DARC_NW.$) {
            xp[0]=xp[1]=cx-d; xp[2]=rightEdge;
            yp[0]=bottomEdge; yp[1]=yp[2]=cy-d;
            ctx().strokePolyline(xp,yp,xp.length);

            xp[0]=xp[1]=cx+d; xp[2]=rightEdge;
            yp[0]=bottomEdge; yp[1]=yp[2]=cy+d;
            ctx().strokePolyline(xp,yp,xp.length);
        }
        else if (cornerType == DARC_NE.$) {
            xp[0]=x; xp[1]=xp[2]=cx+d;
            yp[0]=yp[1]=cy-d; yp[2]=bottomEdge;
            ctx().strokePolyline(xp,yp,xp.length);

            xp[0]=x; xp[1]=xp[2]=cx-d;
            yp[0]=yp[1]=cy+d; yp[2]=bottomEdge;
            ctx().strokePolyline(xp,yp,xp.length);
        }
        else if (cornerType == DARC_SE.$) {
            xp[0]=x; xp[1]=xp[2]=cx-d;
            yp[0]=yp[1]=cy-d; yp[2]=y;
            ctx().strokePolyline(xp,yp,xp.length);

            xp[0]=x; xp[1]=xp[2]=cx+d;
            yp[0]=yp[1]=cy+d; yp[2]=y;
            ctx().strokePolyline(xp,yp,xp.length);
        }
        else if (cornerType == DARC_SW.$) {
            xp[0]=xp[1]=cx-d; xp[2]=rightEdge;
            yp[0]=y; yp[1]=yp[2]=cy+d;
            ctx().strokePolyline(xp,yp,xp.length);

            xp[0]=xp[1]=cx+d; xp[2]=rightEdge;
            yp[0]=y; yp[1]=yp[2]=cy-d;
            ctx().strokePolyline(xp,yp,xp.length);
        }
    }
}