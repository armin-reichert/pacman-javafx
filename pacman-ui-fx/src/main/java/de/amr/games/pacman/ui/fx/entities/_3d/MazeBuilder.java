package de.amr.games.pacman.ui.fx.entities._3d;

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
 * Creates 3D model from a world description.
 * 
 * @author Armin Reichert
 */
public class MazeBuilder {

	private static final int N = 4;
	private static final double BLOCKSIZE = (double) TS / N;

	public static V2i northOf(int tileX, int tileY, int i) {
		int dy = i / N == 0 ? -1 : 0;
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

	public static V2i southOf(int tileX, int tileY, int i) {
		int dy = i / N == N - 1 ? 1 : 0;
		return new V2i(tileX, tileY + dy);
	}

	private int xSize;
	private int ySize;
	private byte[][] cells;
	private PhongMaterial material;
	private List<Box> walls;

	public MazeBuilder() {
		material = new PhongMaterial();
	}

	public List<Box> getWalls() {
		return Collections.unmodifiableList(walls);
	}

	public void setWallMaterial(PhongMaterial material) {
		this.material = material;
	}

	public void build(PacManGameWorld world) {
		walls = new ArrayList<>();
		scan(world);
		createWalls(world);
	}

	private void scan(PacManGameWorld world) {
		xSize = N * world.numCols();
		ySize = N * world.numRows();
		cells = new byte[ySize][xSize];
		// mark cells inside walls
		for (int y = 0; y < ySize; ++y) {
			for (int x = 0; x < xSize; ++x) {
				V2i tile = new V2i(x / N, y / N);
				if (world.isWall(tile)) {
					int i = (y % N) * N + (x % N);
					cells[y][x] = (byte) i;
				} else {
					cells[y][x] = (byte) -1;
				}
			}
		}
		// clear cells inside wall areas
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
						// keep corner
					} else {
						cells[y][x] = -1;
					}
				}
			}
		}
	}

	private void addWall(int leftX, int topY, int wallWidth, int wallHeight) {
		Box wall = createWall(leftX, topY, material, wallWidth, wallHeight);
		if (wall != null) {
			walls.add(wall);
		}
	}

	private void createWalls(PacManGameWorld world) {
		MazeBuilder scanner = new MazeBuilder();
		scanner.scan(world);
		walls.clear();
		createHorizontalWalls(world);
		createVerticalWalls(world);
	}

	private Box createWall(int leftX, int topY, PhongMaterial material, int width, int height) {
		if (width <= 1 && height <= 1) {
			return null; // ignore 1x1 walls for now
		}
		Box wall = new Box(width * BLOCKSIZE, height * BLOCKSIZE, BLOCKSIZE);
		wall.setMaterial(material);
		wall.setTranslateX(leftX * BLOCKSIZE + (width - 4) * 0.5 * BLOCKSIZE);
		wall.setTranslateY(topY * BLOCKSIZE + (height - 4) * 0.5 * BLOCKSIZE);
		wall.setTranslateZ(1.5);
		wall.drawModeProperty().bind(Env.$drawMode);
		return wall;
	}

	private void createHorizontalWalls(PacManGameWorld world) {
		int xSize = cells[0].length, ySize = cells.length;
		for (int y = 0; y < ySize; ++y) {
			int wallStartX = -1;
			int wallWidth = 0;
			for (int x = 0; x < xSize; ++x) {
				byte cell = cells[y][x];
				if (cell != -1) {
					if (wallStartX == -1) {
						wallStartX = x;
						wallWidth = 1;
					} else {
						wallWidth++;
					}
				} else {
					if (wallStartX != -1) {
						addWall(wallStartX, y, wallWidth, 1);
						wallStartX = -1;
					}
				}
				if (x == xSize - 1 && wallStartX != -1) {
					addWall(wallStartX, y, wallWidth, 1);
					wallStartX = -1;
				}
			}
			if (y == ySize - 1 && wallStartX != -1) {
				addWall(wallStartX, y, wallWidth, 1);
				wallStartX = -1;
			}
		}
	}

	private void createVerticalWalls(PacManGameWorld world) {
		int xSize = cells[0].length, ySize = cells.length;
		for (int x = 0; x < xSize; ++x) {
			int wallStartY = -1;
			int wallHeight = 0;
			for (int y = 0; y < ySize; ++y) {
				byte cell = cells[y][x];
				if (cell != -1) {
					if (wallStartY == -1) {
						wallStartY = y;
						wallHeight = 1;
					} else {
						wallHeight++;
					}
				} else {
					if (wallStartY != -1) {
						addWall(x, wallStartY, 1, wallHeight);
						wallStartY = -1;
					}
				}
				if (y == ySize - 1 && wallStartY != -1) {
					addWall(x, wallStartY, 1, wallHeight);
					wallStartY = -1;
				}
			}
			if (x == xSize - 1 && wallStartY != -1) {
				addWall(x, wallStartY, 1, wallHeight);
				wallStartY = -1;
			}
		}
	}
}