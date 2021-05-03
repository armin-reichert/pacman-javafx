package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Creates walls around inaccessible world areas.
 * 
 * @author Armin Reichert
 */
public class WallBuilder {

	private static final int N = 4;
	private static final double BLOCKSIZE = (double) TS / N;

	public static V2i northOf(int tileX, int tileY, int i) {
		int dy = i / N == 0 ? -1 : 0;
		return new V2i(tileX, tileY + dy);
	}

	public static V2i southOf(int tileX, int tileY, int i) {
		int dy = i / N == N - 1 ? 1 : 0;
		return new V2i(tileX, tileY + dy);
	}

	public static V2i westOf(int tileX, int tileY, int i) {
		int dx = i % N == 0 ? -1 : 0;
		return new V2i(tileX + dx, tileY);
	}

	public static V2i eastOf(int tileX, int tileY, int i) {
		int dx = i % N == N - 1 ? 1 : 0;
		return new V2i(tileX + dx, tileY);
	}

	private int xSize;
	private int ySize;
	private boolean[][] isWall;
	private PhongMaterial material;
	private List<Box> walls;
	private double wallSizeZ = BLOCKSIZE;

	public WallBuilder() {
		material = new PhongMaterial();
	}

	public List<Box> getWalls() {
		return Collections.unmodifiableList(walls);
	}

	public void setWallSizeZ(double wallSizeZ) {
		this.wallSizeZ = wallSizeZ;
	}

	public void setWallMaterial(PhongMaterial material) {
		this.material = material;
	}

	public List<Box> build(PacManGameWorld world) {
		long start, end;
		double millis;
		walls = new ArrayList<>();

		start = System.nanoTime();
		scan(world);
		end = System.nanoTime();
		millis = (end - start) * 1e-6;
		log("WallBuilder: scanning world took %.0f milliseconds", millis);

		start = System.nanoTime();
		createHorizontalWalls(world);
		createVerticalWalls(world);
		end = System.nanoTime();
		millis = (end - start) * 1e-6;
		log("WallBuilder: building walls took %.0f milliseconds", millis);

		return getWalls();
	}

	private void scan(PacManGameWorld world) {
		xSize = N * world.numCols();
		ySize = N * world.numRows();
		isWall = new boolean[ySize][xSize];
		// scan for blocks belonging to walls
		for (int y = 0; y < ySize; ++y) {
			for (int x = 0; x < xSize; ++x) {
				V2i tile = new V2i(x / N, y / N);
				isWall[y][x] = world.isWall(tile);
			}
		}
		// clear blocks inside wall regions
		for (int y = 0; y < ySize; ++y) {
			int tileY = y / N;
			for (int x = 0; x < xSize; ++x) {
				int tileX = x / N;
				int i = (y % N) * N + (x % N);
				V2i n = northOf(tileX, tileY, i), e = eastOf(tileX, tileY, i), s = southOf(tileX, tileY, i),
						w = westOf(tileX, tileY, i);
				if (world.isWall(n) && world.isWall(e) && world.isWall(s) && world.isWall(w)) {
					V2i se = southOf(e.x, e.y, i);
					V2i sw = southOf(w.x, w.y, i);
					V2i ne = northOf(e.x, e.y, i);
					V2i nw = northOf(w.x, w.y, i);
					if (world.isWall(se) && !world.isWall(nw) || !world.isWall(se) && world.isWall(nw)
							|| world.isWall(sw) && !world.isWall(ne) || !world.isWall(sw) && world.isWall(ne)) {
						// keep corner of wall region
					} else {
						isWall[y][x] = false;
					}
				}
			}
		}
	}

	private void addWall(int leftX, int topY, int numBlocksX, int numBlocksY) {
		if (numBlocksX == 1 && numBlocksY == 1) {
			return; // ignore 1x1 walls because they could be part of a larger wall in other orientation
		}
		Box wall = createWall(leftX, topY, material, numBlocksX, numBlocksY);
		walls.add(wall);
	}

	private Box createWall(int leftX, int topY, PhongMaterial material, int numBlocksX, int numBlocksY) {
		Box wall = new Box(numBlocksX * BLOCKSIZE, numBlocksY * BLOCKSIZE, wallSizeZ);
		wall.setMaterial(material);
		wall.setTranslateX(leftX * BLOCKSIZE + (numBlocksX - 4) * 0.5 * BLOCKSIZE);
		wall.setTranslateY(topY * BLOCKSIZE + (numBlocksY - 4) * 0.5 * BLOCKSIZE);
		wall.setTranslateZ(1.5);
		wall.drawModeProperty().bind(Env.$drawMode);
		return wall;
	}

	private void createHorizontalWalls(PacManGameWorld world) {
		int xSize = isWall[0].length, ySize = isWall.length;
		for (int y = 0; y < ySize; ++y) {
			int leftX = -1;
			int sizeX = 0;
			for (int x = 0; x < xSize; ++x) {
				if (isWall[y][x]) {
					if (leftX == -1) {
						leftX = x;
						sizeX = 1;
					} else {
						sizeX++;
					}
				} else {
					if (leftX != -1) {
						addWall(leftX, y, sizeX, 1);
						leftX = -1;
					}
				}
				if (x == xSize - 1 && leftX != -1) {
					addWall(leftX, y, sizeX, 1);
					leftX = -1;
				}
			}
			if (y == ySize - 1 && leftX != -1) {
				addWall(leftX, y, sizeX, 1);
				leftX = -1;
			}
		}
	}

	private void createVerticalWalls(PacManGameWorld world) {
		int xSize = isWall[0].length, ySize = isWall.length;
		for (int x = 0; x < xSize; ++x) {
			int topY = -1;
			int sizeY = 0;
			for (int y = 0; y < ySize; ++y) {
				if (isWall[y][x]) {
					if (topY == -1) {
						topY = y;
						sizeY = 1;
					} else {
						sizeY++;
					}
				} else {
					if (topY != -1) {
						addWall(x, topY, 1, sizeY);
						topY = -1;
					}
				}
				if (y == ySize - 1 && topY != -1) {
					addWall(x, topY, 1, sizeY);
					topY = -1;
				}
			}
			if (x == xSize - 1 && topY != -1) {
				addWall(x, topY, 1, sizeY);
				topY = -1;
			}
		}
	}
}