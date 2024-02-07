/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2i;

import java.io.PrintWriter;
import java.io.Writer;

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
		switch (b) {
		case CORNER:
			return '+';
		case EMPTY:
			return ' ';
		case HWALL:
			return '\u2014';
		case VWALL:
			return '|';
		case DOOR:
			return 'd';
		default:
			return '?';
		}
	}

	private byte[][] info;
	private final World world;
	private final int resolution;

	public FloorPlan(World world, int resolution) {
		this.world = world;
		this.resolution = resolution;
		int numBlocksX = resolution * world.numCols();
		int numBlocksY = resolution * world.numRows();
		info = new byte[numBlocksY][numBlocksX];
		scanForWalls(numBlocksX, numBlocksY);
		clearPlacesSurroundedByWalls(numBlocksX, numBlocksY);
		separateWallsAndCorners(numBlocksX, numBlocksY);
	}

	public byte get(int x, int y) {
		return info[y][x];
	}

	public int sizeX() {
		return info[0].length;
	}

	public int sizeY() {
		return info.length;
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
				p.print(useSymbols ? String.valueOf(symbol(get(x, y))) : get(x, y));
			}
			p.println();
		}
	}

	private Vector2i northOf(int tileX, int tileY, int i) {
		int dy = i / resolution == 0 ? -1 : 0;
		return v2i(tileX, tileY + dy);
	}

	private Vector2i southOf(int tileX, int tileY, int i) {
		int dy = i / resolution == resolution - 1 ? 1 : 0;
		return v2i(tileX, tileY + dy);
	}

	private Vector2i westOf(int tileX, int tileY, int i) {
		int dx = i % resolution == 0 ? -1 : 0;
		return v2i(tileX + dx, tileY);
	}

	private Vector2i eastOf(int tileX, int tileY, int i) {
		int dx = i % resolution == resolution - 1 ? 1 : 0;
		return v2i(tileX + dx, tileY);
	}

	private void separateWallsAndCorners(int numBlocksX, int numBlocksY) {
		separateHorizontalWallsAndCorners(numBlocksX, numBlocksY);
		separateVerticalWallsAndCorners(numBlocksX, numBlocksY);
	}

	private void separateVerticalWallsAndCorners(int numBlocksX, int numBlocksY) {
		for (int x = 0; x < numBlocksX; ++x) {
			int startY = -1;
			int size = 0;
			for (int y = 0; y < numBlocksY; ++y) {
				if (info[y][x] == CORNER) {
					if (startY == -1) {
						startY = y;
						size = 1;
					} else {
						info[y][x] = (y == numBlocksY - 1) ? CORNER : VWALL;
						++size;
					}
				} else {
					if (size == 1) {
						info[startY][x] = CORNER;
					} else if (size > 1) {
						info[startY + size - 1][x] = CORNER;
					}
					startY = -1;
					size = 0;
				}
			}
		}
	}

	private void separateHorizontalWallsAndCorners(int numBlocksX, int numBlocksY) {
		for (int y = 0; y < numBlocksY; ++y) {
			int startX = -1;
			int size = 0;
			for (int x = 0; x < numBlocksX; ++x) {
				if (info[y][x] == CORNER) {
					if (startX == -1) {
						startX = x;
						size = 1;
					} else {
						info[y][x] = x == numBlocksX - 1 ? CORNER : HWALL;
						++size;
					}
				} else {
					if (size == 1) {
						info[y][startX] = CORNER;
					} else if (size > 1) {
						info[y][startX + size - 1] = CORNER;
					}
					startX = -1;
					size = 0;
				}
			}
		}
	}

	private void scanForWalls(int numBlocksX, int numBlocksY) {
		for (int y = 0; y < numBlocksY; ++y) {
			for (int x = 0; x < numBlocksX; ++x) {
				Vector2i tile = v2i(x / resolution, y / resolution);
				info[y][x] = world.isWall(tile) ? CORNER : EMPTY;
			}
		}
	}

	private void clearPlacesSurroundedByWalls(int numBlocksX, int numBlocksY) {
		for (int y = 0; y < numBlocksY; ++y) {
			int tileY = y / resolution;
			for (int x = 0; x < numBlocksX; ++x) {
				int tileX = x / resolution;
				int i = (y % resolution) * resolution + (x % resolution);
				var n = northOf(tileX, tileY, i);
				var e = eastOf(tileX, tileY, i);
				var s = southOf(tileX, tileY, i);
				var w = westOf(tileX, tileY, i);
				if (world.isWall(n) && world.isWall(e) && world.isWall(s) && world.isWall(w)) {
					var se = southOf(e.x(), e.y(), i);
					var sw = southOf(w.x(), w.y(), i);
					var ne = northOf(e.x(), e.y(), i);
					var nw = northOf(w.x(), w.y(), i);
					if (world.isWall(se) && !world.isWall(nw) || !world.isWall(se) && world.isWall(nw)
							|| world.isWall(sw) && !world.isWall(ne) || !world.isWall(sw) && world.isWall(ne)) {
						// keep corner of wall region
					} else {
						info[y][x] = EMPTY;
					}
				}
			}
		}
	}
}