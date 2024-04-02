/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2i;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * Provides information about rooms, walls, doors etc. Used to construct 3D representation of a world from simple 2D
 * tile map data.
 *
 * @author Armin Reichert
 */
public class FloorPlan {

    public static final byte EMPTY  = 0;
    public static final byte CORNER = 1;
    public static final byte HWALL  = 2;
    public static final byte VWALL  = 3;
    public static final byte DOOR   = 4;

    private static char symbol(byte b) {
        return switch (b) {
            case CORNER -> '+';
            case EMPTY  -> ' ';
            case HWALL  -> '—';
            case VWALL  -> '|';
            case DOOR   -> 'd';
            default     -> '?';
        };
    }

    public static FloorPlan read(URL url) {
        try {
            return read(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FloorPlan read(InputStream in) {
        try (var rdr = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            var floorPlan = new FloorPlan();
            int row = 0;
            String line = rdr.readLine();
            while (line != null) {
                if (line.startsWith("#")) {
                    var keyValue = line.substring(1).split("=");
                    var key = keyValue[0].trim();
                    var value = keyValue[1].trim();
                    switch (key) {
                        case "resolution" -> floorPlan.resolution = Integer.parseInt(value);
                        case "cols"       -> floorPlan.sizeX = Integer.parseInt(value);
                        case "rows"       -> floorPlan.sizeY = Integer.parseInt(value);
                    }
                } else {
                    if (floorPlan.cells == null) {
                        floorPlan.cells = new byte[floorPlan.sizeY][floorPlan.sizeX];
                    }
                    for (int col = 0; col < line.length(); ++col) {
                        floorPlan.cells[row][col] = Byte.parseByte(String.valueOf(line.charAt(col)));
                    }
                    ++row;
                }
                line = rdr.readLine();
            }
            return floorPlan;
        } catch (Exception x) {
            Logger.error("Could not read floor plan");
            Logger.error(x);
            return null;
        }
    }

    private byte[][] cells;
    private int sizeX;
    private int sizeY;
    private int resolution;

    public FloorPlan(World world, int sizeX, int sizeY, int resolution) {
        checkNotNull(world);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.resolution = resolution;
        cells = new byte[sizeY][sizeX];
        separateWallsFromEmptyCells(world);
        clearCellsSurroundedByWalls(world);
        separateHorizontalWallsAndCorners();
        separateVerticalWallsAndCorners();
    }

    private FloorPlan() {
    }

    public byte cell(int x, int y) {
        return cells[y][x];
    }

    public int sizeX() {
        return sizeX;
    }

    public int sizeY() {
        return sizeY;
    }

    public int resolution() {
        return resolution;
    }

    public Vector2i tileOfCell(int cellX, int cellY) {
        return v2i(cellX / resolution, cellY / resolution);
    }

    public void write(File file) {
        checkNotNull(file);
        try (var out = new FileWriter(file, StandardCharsets.UTF_8)) {
            print(new PrintWriter(out), false);
        } catch (Exception x) {
            Logger.error("Could not write floor plan to file " + file);
            Logger.error(x);
        }
    }

    public void print(Writer w, boolean useSymbols) {
        checkNotNull(w);
        PrintWriter p = new PrintWriter(w);
        p.println("# resolution=" + resolution);
        p.println("# cols=" + sizeX());
        p.println("# rows=" + sizeY());
        for (int y = 0; y < sizeY(); ++y) {
            for (int x = 0; x < sizeX(); ++x) {
                p.print(useSymbols ? String.valueOf(symbol(cell(x, y))) : cell(x, y));
            }
            p.println();
        }
    }

    private Vector2i northOf(int tileX, int tileY, int i) {
        int dy = i / resolution == 0 ? -1 : 0;
        return v2i(tileX, tileY + dy);
    }

    private Vector2i northOf(Vector2i tile, int i) {
        return northOf(tile.x(), tile.y(), i);
    }

    private Vector2i southOf(int tileX, int tileY, int i) {
        int dy = i / resolution == resolution - 1 ? 1 : 0;
        return v2i(tileX, tileY + dy);
    }

    private Vector2i southOf(Vector2i tile, int i) {
        return southOf(tile.x(), tile.y(), i);
    }

    private Vector2i westOf(int tileX, int tileY, int i) {
        int dx = i % resolution == 0 ? -1 : 0;
        return v2i(tileX + dx, tileY);
    }

    private Vector2i westOf(Vector2i tile, int i) {
        return westOf(tile.x(), tile.y(), i);
    }

    private Vector2i eastOf(int tileX, int tileY, int i) {
        int dx = i % resolution == resolution - 1 ? 1 : 0;
        return v2i(tileX + dx, tileY);
    }

    private Vector2i eastOf(Vector2i tile, int i) {
        return eastOf(tile.x(), tile.y(), i);
    }

    private void separateVerticalWallsAndCorners() {
        for (int x = 0; x < sizeX; ++x) {
            int startY = -1;
            int size = 0;
            for (int y = 0; y < sizeY; ++y) {
                if (cells[y][x] == CORNER) {
                    if (startY == -1) {
                        startY = y;
                        size = 1;
                    } else {
                        cells[y][x] = (y == sizeY - 1) ? CORNER : VWALL;
                        ++size;
                    }
                } else {
                    if (size == 1) {
                        cells[startY][x] = CORNER;
                    } else if (size > 1) {
                        cells[startY + size - 1][x] = CORNER;
                    }
                    startY = -1;
                    size = 0;
                }
            }
        }
    }

    private void separateHorizontalWallsAndCorners() {
        for (int y = 0; y < sizeY; ++y) {
            int startX = -1;
            int size = 0;
            for (int x = 0; x < sizeX; ++x) {
                if (cells[y][x] == CORNER) {
                    if (startX == -1) {
                        startX = x;
                        size = 1;
                    } else {
                        cells[y][x] = x == sizeX - 1 ? CORNER : HWALL;
                        ++size;
                    }
                } else {
                    if (size == 1) {
                        cells[y][startX] = CORNER;
                    } else if (size > 1) {
                        cells[y][startX + size - 1] = CORNER;
                    }
                    startX = -1;
                    size = 0;
                }
            }
        }
    }

    private void separateWallsFromEmptyCells(World world) {
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                Vector2i tile = v2i(x / resolution, y / resolution);
                cells[y][x] = world.isWall(tile) ? CORNER : EMPTY; // use CORNER as synonym for any kind of wall
            }
        }
    }




    private boolean checkForCardinalDirections(World world,Vector2i[] arr){
        return world.isWall(arr[0]) && world.isWall(arr[1]) && world.isWall(arr[2]) && world.isWall(arr[3]);
    }


    private boolean checkForOrdinalDirections(World world,Vector2i[] arr){
        boolean c1 = world.isWall(arr[0]) && !world.isWall(arr[3]);
        boolean c2 =  !world.isWall(arr[0]) &&  world.isWall(arr[3]);
        boolean c3 = world.isWall(arr[1]) && !world.isWall(arr[2]);
        boolean c4 = !world.isWall(arr[1]) &&  world.isWall(arr[2]);
        return !(  c1 || c2 ||  c3 || c4);
    }





    private void clearCellsSurroundedByWalls(World world) {
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                int i = (y % resolution) * resolution + (x % resolution);
                Vector2i tile = v2i(x / resolution, y / resolution);
                Vector2i n = northOf(tile, i), e = eastOf(tile, i), s = southOf(tile, i), w = westOf(tile, i);
                Vector2i[] arr={n,e,s,w};
                if (checkForCardinalDirections(world,arr)) {
                    Vector2i se = southOf(e, i), sw = southOf(w, i), ne = northOf(e, i), nw = northOf(w, i);
                    arr= new Vector2i[]{se, sw, ne, nw};
                    if ( checkForOrdinalDirections(world,arr)) {
                        cells[y][x] = EMPTY;
                    }
                }
            }
        }
    }
}