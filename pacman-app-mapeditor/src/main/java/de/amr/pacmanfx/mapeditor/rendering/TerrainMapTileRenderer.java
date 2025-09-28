/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.rendering;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.Obstacle;
import de.amr.pacmanfx.lib.worldmap.ObstacleSegment;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.lib.worldmap.WorldMapParser;
import de.amr.pacmanfx.model.DefaultWorldMapPropertyName;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColorScheme;
import de.amr.pacmanfx.uilib.rendering.TerrainMapRenderer;
import de.amr.pacmanfx.uilib.rendering.TileRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.lib.worldmap.TerrainTile.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

/**
 * Tile-based renderer used in editor. At runtime and in the 2D editor preview, the path based renderer is used.
 */
public class TerrainMapTileRenderer extends BaseRenderer implements TerrainMapRenderer, TileRenderer {

    public static final Font SYMBOL_FONT = Font.font("Monospace", FontWeight.NORMAL, 8);
    public static final Color SYMBOL_COLOR = Color.gray(0.6);

    public static final Font INNER_RECT_FONT = Font.font("Sans", FontWeight.BOLD, 3.5);

    public static final String TUNNEL_SYMBOL = "\uD83D\uDE87";

    public static final Map<Direction, String> ONE_WAY_SYMBOLS = Map.of(
        Direction.LEFT,  "\u2190",
        Direction.UP,    "\u2191",
        Direction.RIGHT, "\u2192",
        Direction.DOWN,  "\u2193"
    );

    private static final Color[] RANDOM_COLORS = new Color[30];

    static {
        for (int i = 0; i < RANDOM_COLORS.length; ++i) {
            RANDOM_COLORS[i] = Color.rgb(randomInt(0, 256), randomInt(0, 256), randomInt(0, 256));
        }
    }

    private static final Color  SEGMENT_NUMBER_FILL_COLOR = Color.LIGHTGRAY;
    private static final Color  SEGMENT_NUMBER_STROKE_COLOR = Color.BLACK;
    private static final double SEGMENT_NUMBER_FONT_SIZE = 4;
    private static final Font   SEGMENT_NUMBER_FONT = Font.font("Sans", FontWeight.BOLD, SEGMENT_NUMBER_FONT_SIZE);

    private boolean obstacleInnerAreaDisplayed = false;
    private boolean scatterTargetsDisplayed = true;
    private boolean segmentNumbersDisplayed = false;
    private boolean specialTilesDisplayed = true;
    private boolean tunnelIconsDisplayed = true;

    private BiPredicate<WorldMap, Vector2i> terrainFilter = (worldMap, tile) -> true;

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

    public void setObstacleInnerAreaDisplayed(boolean obstacleInnerAreaDisplayed) {
        this.obstacleInnerAreaDisplayed = obstacleInnerAreaDisplayed;
    }

    public void setScatterTargetsDisplayed(boolean scatterTargetsDisplayed) {
        this.scatterTargetsDisplayed = scatterTargetsDisplayed;
    }

    public void setSegmentNumbersDisplayed(boolean segmentNumbersDisplayed) {
        this.segmentNumbersDisplayed = segmentNumbersDisplayed;
    }

    public void setSpecialTilesDisplayed(boolean specialTilesDisplayed) {
        this.specialTilesDisplayed = specialTilesDisplayed;
    }

    public void setTunnelIconsDisplayed(boolean tunnelIconsDisplayed) {
        this.tunnelIconsDisplayed = tunnelIconsDisplayed;
    }

    public void setTerrainFilter(BiPredicate<WorldMap, Vector2i> terrainFilter) {
        this.terrainFilter = requireNonNull(terrainFilter);
    }

    @Override
    public void draw(WorldMap worldMap) {
        ctx.save();
        ctx.scale(scaling(), scaling());
        worldMap.terrainLayer().tiles().filter(tile -> terrainFilter.test(worldMap, tile)).forEach(tile -> {
            byte code = worldMap.terrainLayer().get(tile);
            drawTileUnscaled(tile, code);
        });
        if (specialTilesDisplayed) {
            specialTile(worldMap, DefaultWorldMapPropertyName.POS_SCATTER_RED_GHOST).ifPresent(tile -> drawScatterTarget(tile, Color.RED));
            specialTile(worldMap, DefaultWorldMapPropertyName.POS_SCATTER_PINK_GHOST).ifPresent(tile -> drawScatterTarget(tile, Color.PINK));
            specialTile(worldMap, DefaultWorldMapPropertyName.POS_SCATTER_CYAN_GHOST).ifPresent(tile -> drawScatterTarget(tile, Color.CYAN));
            specialTile(worldMap, DefaultWorldMapPropertyName.POS_SCATTER_ORANGE_GHOST).ifPresent(tile -> drawScatterTarget(tile, Color.ORANGE));
        }
        if (segmentNumbersDisplayed) {
            drawObstacleSegmentNumbers(worldMap.terrainLayer().obstacles());
        }
        if (obstacleInnerAreaDisplayed) {
            drawObstacleInnerAreas(worldMap.terrainLayer().obstacles());
        }
        ctx.restore();
    }

    private void drawObstacleInnerAreas(Set<Obstacle> obstacles) {
        double r = 1;
        obstacles.stream().filter(Obstacle::isClosed).filter(not(Obstacle::borderObstacle)).forEach(obstacle -> {
            Vector2i prev = null;
            List<RectShort> rectangles = obstacle.innerAreaRectangles();
            for (int i = 0; i < rectangles.size(); ++i) {
                RectShort rect = rectangles.get(i);
                ctx.setFill(RANDOM_COLORS[i % RANDOM_COLORS.length]);
                ctx.fillRect(rect.x(), rect.y(), rect.width(), rect.height());
            }
            List<Vector2i> innerPolygon = obstacle.computeInnerPolygon();
            ctx.setFill(Color.grayRgb(200));
            ctx.setStroke(Color.grayRgb(200));
            ctx.setLineWidth(0.5);
            for (Vector2i p : innerPolygon) {
                ctx.fillOval(p.x() - r, p.y() - r, 2*r, 2*r);
                if (prev != null) {
                    ctx.strokeLine(prev.x(), prev.y(), p.x(), p.y());
                }
                prev = p;
            }
            ctx.save();
            ctx.setFill(Color.WHITE);
            ctx.setFont(INNER_RECT_FONT);
            ctx.setTextAlign(TextAlignment.CENTER);
            ctx.setTextBaseline(VPos.CENTER);
            for (int i = 0; i < rectangles.size(); ++i) {
                RectShort rect = rectangles.get(i);
                String text = "R" + (i+1);
                ctx.fillText(text, rect.x() + 0.5 * rect.width(), rect.y() + 0.5 * rect.height());
            }
            ctx.restore();
        });
    }

    private void drawObstacleSegmentNumbers(Set<Obstacle> obstacles) {
        ctx.setFont(SEGMENT_NUMBER_FONT);
        ctx.setFill(SEGMENT_NUMBER_FILL_COLOR);
        ctx.setStroke(SEGMENT_NUMBER_STROKE_COLOR);
        ctx.setLineWidth(0.1);
        obstacles.stream().filter(not(Obstacle::borderObstacle)).forEach(obstacle -> {
            for (int i = 0; i < obstacle.numSegments(); ++i) {
                ObstacleSegment segment = obstacle.segment(i);
                Vector2f startPoint = segment.startPoint().toVector2f();
                Vector2f endPoint = segment.endPoint().toVector2f();
                Vector2f midpoint = startPoint.midpoint(endPoint);
                String text = String.valueOf(i);
                ctx.save();
                ctx.setTextAlign(TextAlignment.CENTER);
                ctx.fillText  (text, midpoint.x(), midpoint.y());
                ctx.strokeText(text, midpoint.x(), midpoint.y());
                ctx.restore();
            }
        });
    }

    @Override
    public void drawTile(Vector2i tile, byte content) {
        ctx.save();
        ctx.scale(scaling(), scaling());
        drawTileUnscaled(tile, content);
        ctx.restore();
    }

    private Optional<Vector2i> specialTile(WorldMap worldMap, String propertyName) {
        if (worldMap.terrainLayer().propertyMap().containsKey(propertyName)) {
            return WorldMapParser.parseTile(worldMap.terrainLayer().propertyMap().get(propertyName));
        }
        return Optional.empty();
    }

    private void drawTileUnscaled(Vector2i tile, byte code) {
        switch (code) {
            case 0x01 -> drawWallH(tile);
            case 0x02 -> drawWallV(tile);
            case 0x03, 0x04, 0x05, 0x06 -> drawArc(tile, code);
            case 0x07 -> drawTunnelIcon(tile);
            case 0x0e -> drawDoor(tile, colorScheme().doorColor());
            case 0x10, 0x11, 0x12, 0x13 -> drawDCorner(tile, code, xp, yp);
            case 0x14,0x15,0x16,0x17 -> drawOneWaySign(tile, code);
        }
    }

    private void drawDoor(Vector2i tile, Color color) {
        double x = tile.x() * TS, y = tile.y() * TS;
        double height = TS * 0.25;
        ctx.setFill(color);
        ctx.fillRect(x, y + 0.5 * (TS - height), TS, height);
    }

    public void drawScatterTarget(Vector2i tile, Color color) {
        if (scatterTargetsDisplayed) {
            double x = tile.x() * TS, y = tile.y() * TS;
            ctx.setFill(color);
            ctx.fillRect(x, y, TS, TS);
            ctx.setStroke(Color.WHITE);
            ctx.setLineWidth(0.5);
            ctx.strokeOval(x + 2, y + 2, TS - 4, TS - 4);
            ctx.strokeLine(x + 0.5 * TS, y, x + 0.5 * TS, y + TS);
            ctx.strokeLine(x, y + 0.5 * TS, x + TS, y + 0.5 * TS);
        }
    }

    private void drawOneWaySign(Vector2i tile, byte code) {
        double x = tile.x() * TS, y = tile.y() * TS;
        Direction dir = switch (code) {
            case 0x14 -> Direction.UP;
            case 0x15 -> Direction.RIGHT;
            case 0x16 -> Direction.DOWN;
            case 0x17 -> Direction.LEFT;
            default -> throw new IllegalArgumentException("Illegal one-way tile code: " + code);
        };
        Point2D offset = switch (code) {
            case 0x14 -> new Point2D(1.5, 5);
            case 0x15 -> new Point2D(1, 6);
            case 0x16 -> new Point2D(1.5, 5);
            case 0x17 -> new Point2D(1, 6);
            default -> throw new IllegalArgumentException("Illegal one-way tile code: " + code);
        };
        ctx.setFont(SYMBOL_FONT);
        ctx.setFill(SYMBOL_COLOR);
        ctx.fillText(ONE_WAY_SYMBOLS.get(dir), x + offset.getX(), y + offset.getY());
    }

    private void drawTunnelIcon(Vector2i tile) {
        if (tunnelIconsDisplayed) {
            double x = tile.x() * TS, y = tile.y() * TS;
            ctx.setFont(SYMBOL_FONT);
            ctx.setFill(SYMBOL_COLOR);
            ctx.fillText(TUNNEL_SYMBOL, x, y + 7);
        }
    }

    private void drawWallH(Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx.setFill(colorScheme().wallStrokeColor());
        ctx.fillRect(x, y + 3.5f, TS, 1);
    }

    private void drawWallV(Vector2i tile) {
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx.setFill(colorScheme().wallStrokeColor());
        // add 1 pixel to avoid gaps
        ctx.fillRect(x + 3.5f, y, 1, TS);
    }

    private void drawArc(Vector2i tile, byte cornerType) {
        double x = tile.x() * TS, y = tile.y() * TS;
        ctx.setStroke(colorScheme().wallStrokeColor());
        ctx.setLineWidth(1);
        if (cornerType == ARC_NW.$) ctx.strokeArc(x + 4, y + 4, TS, TS, 90, 90,  ArcType.OPEN);
        if (cornerType == ARC_NE.$) ctx.strokeArc(x - 4, y + 4, TS, TS, 0, 90,   ArcType.OPEN);
        if (cornerType == ARC_SE.$) ctx.strokeArc(x - 4, y - 4, TS, TS, 270, 90, ArcType.OPEN);
        if (cornerType == ARC_SW.$) ctx.strokeArc(x + 4, y - 4, TS, TS, 180, 90, ArcType.OPEN);
    }

    private void drawDCorner(Vector2i tile, byte code, double[] xp, double[] yp) {
        double x = tile.x() * TS, y = tile.y() * TS;
        double cx = x + HTS, cy = y + HTS;
        double rightEdge = x + TS, bottomEdge = y + TS;
        double d = 1;
        ctx.setStroke(colorScheme().wallStrokeColor());
        ctx.setLineWidth(1);
        switch (code) {
            case 0x10 -> {
                xp[0] = xp[1] = cx - d;
                xp[2] = rightEdge;
                yp[0] = bottomEdge;
                yp[1] = yp[2] = cy - d;
                ctx.strokePolyline(xp, yp, xp.length);

                xp[0] = xp[1] = cx + d;
                xp[2] = rightEdge;
                yp[0] = bottomEdge;
                yp[1] = yp[2] = cy + d;
                ctx.strokePolyline(xp, yp, xp.length);
            }
            case 0x11 -> {
                xp[0] = x;
                xp[1] = xp[2] = cx + d;
                yp[0] = yp[1] = cy - d;
                yp[2] = bottomEdge;
                ctx.strokePolyline(xp, yp, xp.length);

                xp[0] = x;
                xp[1] = xp[2] = cx - d;
                yp[0] = yp[1] = cy + d;
                yp[2] = bottomEdge;
                ctx.strokePolyline(xp, yp, xp.length);
            }
            case 0x12 -> {
                xp[0] = x;
                xp[1] = xp[2] = cx - d;
                yp[0] = yp[1] = cy - d;
                yp[2] = y;
                ctx.strokePolyline(xp, yp, xp.length);

                xp[0] = x;
                xp[1] = xp[2] = cx + d;
                yp[0] = yp[1] = cy + d;
                yp[2] = y;
                ctx.strokePolyline(xp, yp, xp.length);
            }
            case 0x13 -> {
                xp[0] = xp[1] = cx - d;
                xp[2] = rightEdge;
                yp[0] = y;
                yp[1] = yp[2] = cy + d;
                ctx.strokePolyline(xp, yp, xp.length);

                xp[0] = xp[1] = cx + d;
                xp[2] = rightEdge;
                yp[0] = y;
                yp[1] = yp[2] = cy - d;
                ctx.strokePolyline(xp, yp, xp.length);
            }
        }
    }
}