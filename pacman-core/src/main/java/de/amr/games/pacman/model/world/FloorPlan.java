/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2i;

import java.io.PrintWriter;
import java.io.Writer;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * Provides information about rooms, walls, doors etc. Used to construct 3D representation of a world from simple 2D
 * tile map data.
 * 
 * @author Armin Reichert
 */
public class FloorPlan {

	public static final byte EMPTY = 0;
	public static final byte CORNER = 1;
	public static final byte HWALL = 2;
	public static final byte VWALL = 3;
	public static final byte DOOR = 4;

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

	private final byte[][] cells;
	private final World world;
	private final int resolution;

	public FloorPlan(World world, int resolution) {
		checkNotNull(world);
		this.world = world;
		this.resolution = resolution;
		int numCellsX = resolution * world.numCols();
		int numCellsY = resolution * world.numRows();
		cells = new byte[numCellsY][numCellsX];
		separateWallsFromEmptyCells(numCellsX, numCellsY);
		emptyCellsSurroundedByWalls(numCellsX, numCellsY);
		separateHorizontalWallsAndCorners(numCellsX, numCellsY);
		separateVerticalWallsAndCorners(numCellsX, numCellsY);
	}

	public byte cell(int x, int y) {
		return cells[y][x];
	}

	public int sizeX() {
		return cells[0].length;
	}

	public int sizeY() {
		return cells.length;
	}

	public int getResolution() {
		return resolution;
	}

	public Vector2i tile(int x, int y) {
		return v2i(x / resolution, y / resolution);
	}

	public void print(Writer w, boolean useSymbols) {
		PrintWriter p = new PrintWriter(w);
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

	private void separateVerticalWallsAndCorners(int numCellsX, int numCellsY) {
		for (int x = 0; x < numCellsX; ++x) {
			int startY = -1;
			int size = 0;
			for (int y = 0; y < numCellsY; ++y) {
				if (cells[y][x] == CORNER) {
					if (startY == -1) {
						startY = y;
						size = 1;
					} else {
						cells[y][x] = (y == numCellsY - 1) ? CORNER : VWALL;
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

	private void separateHorizontalWallsAndCorners(int numCellsX, int numCellsY) {
		for (int y = 0; y < numCellsY; ++y) {
			int startX = -1;
			int size = 0;
			for (int x = 0; x < numCellsX; ++x) {
				if (cells[y][x] == CORNER) {
					if (startX == -1) {
						startX = x;
						size = 1;
					} else {
						cells[y][x] = x == numCellsX - 1 ? CORNER : HWALL;
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

	private void separateWallsFromEmptyCells(int numCellsX, int numCellsY) {
		for (int y = 0; y < numCellsY; ++y) {
			for (int x = 0; x < numCellsX; ++x) {
				Vector2i tile = v2i(x / resolution, y / resolution);
				cells[y][x] = world.isWall(tile) ? CORNER : EMPTY; // use CORNER as synonym for any kind of wall
			}
		}
	}

	private void emptyCellsSurroundedByWalls(int numCellsX, int numCellsY) {
		for (int y = 0; y < numCellsY; ++y) {
			for (int x = 0; x < numCellsX; ++x) {
				int i = (y % resolution) * resolution + (x % resolution);
				Vector2i tile = v2i(x / resolution, y / resolution);
				Vector2i n = northOf(tile, i);
				Vector2i e = eastOf(tile, i);
				Vector2i s = southOf(tile, i);
				Vector2i w = westOf(tile, i);
				if (world.isWall(n) && world.isWall(e) && world.isWall(s) && world.isWall(w)) {
					Vector2i se = southOf(e, i);
					Vector2i sw = southOf(w, i);
					Vector2i ne = northOf(e, i);
					Vector2i nw = northOf(w, i);
					if (   world.isWall(se) && !world.isWall(nw) || !world.isWall(se) && world.isWall(nw)
						|| world.isWall(sw) && !world.isWall(ne) || !world.isWall(sw) && world.isWall(ne)) {
						// keep corner of wall region
					} else {
						cells[y][x] = EMPTY;
					}
				}
			}
		}
	}
}