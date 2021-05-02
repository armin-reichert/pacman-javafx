package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets.getFoodColor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;

/**
 * 3D-model for a maze. Creates boxes representing walls from the world map.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	private static final int N = 4;
	private static final double VSIZE = (double) TS / N;

	private static class WallScanner {

		private int xSize;
		private int ySize;
		private byte[][] cells;

		public static V2i northOf(int tileX, int tileY, int i) {
			int dy = i / N == 0 ? -1 : 0;
			return new V2i(tileX, tileY + dy);
		}

		public V2i westOf(int tileX, int tileY, int i) {
			int dx = i % N == 0 ? -1 : 0;
			return new V2i(tileX + dx, tileY);
		}

		public V2i eastOf(int tileX, int tileY, int i) {
			int dx = i % N == N - 1 ? 1 : 0;
			return new V2i(tileX + dx, tileY);
		}

		public V2i southOf(int tileX, int tileY, int i) {
			int dy = i / N == N - 1 ? 1 : 0;
			return new V2i(tileX, tileY + dy);
		}

		public void scan(PacManGameWorld world) {
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
	}

	private Box floor;
	private Group wallRoot = new Group();
	private Group foodRoot = new Group();

	public Maze3D(double unscaledWidth, double unscaledHeight) {
		createFloor(unscaledWidth, unscaledHeight);
		getChildren().addAll(floor, wallRoot, foodRoot);
	}

	private void createFloor(double unscaledWidth, double unscaledHeight) {
		floor = new Box(unscaledWidth, unscaledHeight, 0.1);
		var material = new PhongMaterial();
		var texture = new Image(getClass().getResourceAsStream("/common/escher-texture.jpg"));
		material.setDiffuseMap(texture);
		material.setDiffuseColor(Color.rgb(30, 30, 120));
		material.setSpecularColor(Color.rgb(60, 60, 240));
		floor.setMaterial(material);
		floor.setTranslateX(unscaledWidth / 2 - TS / 2);
		floor.setTranslateY(unscaledHeight / 2 - TS / 2);
		floor.setTranslateZ(3);
	}

	private void addWall(Box wall) {
		if (wall != null) {
			wallRoot.getChildren().add(wall);
			log("Wall added");
		}
	}

	public void createWalls(PacManGameWorld world, Color wallColor) {
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseColor(wallColor);
		material.setSpecularColor(wallColor.brighter());
		WallScanner scanner = new WallScanner();
		scanner.scan(world);
		wallRoot.getChildren().clear();
		createHorizontalWalls(world, material, scanner.cells);
		createVerticalWalls(world, material, scanner.cells);
	}

	private Box createWall(int leftX, int topY, PhongMaterial material, int width, int height) {
		if (width <= 1 && height <= 1) {
			return null; // TODO
		}
		Box wall = new Box(width * VSIZE, height * VSIZE, VSIZE);
		wall.setMaterial(material);
		wall.setTranslateX(leftX * VSIZE + (width - 4) * 0.5 * VSIZE);
		wall.setTranslateY(topY * VSIZE + (height - 4) * 0.5 * VSIZE);
		wall.setTranslateZ(1.5);
		wall.drawModeProperty().bind(Env.$drawMode);
		return wall;
	}

	private void createHorizontalWalls(PacManGameWorld world, PhongMaterial material, byte[][] cells) {
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
						addWall(createWall(wallStartX, y, material, wallWidth, 1));
						wallStartX = -1;
					}
				}
				if (x == xSize - 1 && wallStartX != -1) {
					addWall(createWall(wallStartX, y, material, wallWidth, 1));
					wallStartX = -1;
				}
			}
			if (y == ySize - 1 && wallStartX != -1) {
				addWall(createWall(wallStartX, y, material, wallWidth, 1));
				wallStartX = -1;
			}
		}
	}

	private void createVerticalWalls(PacManGameWorld world, PhongMaterial material, byte[][] cells) {
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
						addWall(createWall(x, wallStartY, material, 1, wallHeight));
						wallStartY = -1;
					}
				}
				if (y == ySize - 1 && wallStartY != -1) {
					addWall(createWall(x, wallStartY, material, 1, wallHeight));
					wallStartY = -1;
				}
			}
			if (x == xSize - 1 && wallStartY != -1) {
				addWall(createWall(x, wallStartY, material, 1, wallHeight));
				wallStartY = -1;
			}
		}
	}

	public Stream<Node> foodNodes() {
		return foodRoot.getChildren().stream();
	}

	public void resetFood(GameVariant variant, GameModel game) {
		GameLevel level = game.currentLevel();
		PacManGameWorld world = level.world;
		var foodMaterial = new PhongMaterial(getFoodColor(variant, level.mazeNumber));
		List<Node> foodNodes = world.tiles().filter(world::isFoodTile)//
				.map(tile -> createPellet(world.isEnergizerTile(tile) ? 2.5 : 1, tile, foodMaterial))
				.collect(Collectors.toList());
		foodRoot.getChildren().setAll(foodNodes);
	}

	private Sphere createPellet(double r, V2i tile, PhongMaterial material) {
		Sphere s = new Sphere(r);
		s.setMaterial(material);
		s.setTranslateX(tile.x * TS);
		s.setTranslateY(tile.y * TS);
		s.setTranslateZ(1);
		s.setUserData(tile);
		return s;
	}
}