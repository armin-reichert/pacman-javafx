package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets.getFoodColor;

import java.util.ArrayList;
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
 * <p>
 * TODO: merge also vertical sequences of cubes into quads
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	private static class Voxel extends V2i {

		static final int N = 4;
		static final double SIZE = 8.0 / N;;

		private final byte i; // 0,...,N*N-1

		public Voxel(int tileX, int tileY, int i) {
			super(tileX, tileY);
			this.i = (byte) i;
		}

		@Override
		public String toString() {
			return String.format("(x=%d, y=%d, i=%d)", x, y, i);
		}

		public double x() {
			return x * TS - 1.5 * SIZE + (i % N) * SIZE;
		}

		public double y() {
			return y * TS - 1.5 * SIZE + (i / N) * SIZE;
		}

		public V2i north() {
			return northOf(this);
		}

		public V2i west() {
			return westOf(this);
		}

		public V2i east() {
			return eastOf(this);
		}

		public V2i south() {
			return southOf(this);
		}

		public V2i northOf(V2i v) {
			int dy = i / N == 0 ? -1 : 0;
			return v.plus(0, dy);
		}

		public V2i westOf(V2i v) {
			int dx = i % N == 0 ? -1 : 0;
			return v.plus(dx, 0);
		}

		public V2i eastOf(V2i v) {
			int dx = i % N == N - 1 ? 1 : 0;
			return v.plus(dx, 0);
		}

		public V2i southOf(V2i v) {
			int dy = i / N == N - 1 ? 1 : 0;
			return v.plus(0, dy);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + i;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Voxel other = (Voxel) obj;
			if (i != other.i)
				return false;
			return true;
		}
	}

	private final Box floor;
	private final Group brickRoot = new Group();
	private final Group foodRoot = new Group();

	public Maze3D(double unscaledWidth, double unscaledHeight) {
		var floorMaterial = new PhongMaterial();
		var floorTexture = new Image(getClass().getResourceAsStream("/common/escher-texture.jpg"));
		floorMaterial.setDiffuseMap(floorTexture);
		floorMaterial.setDiffuseColor(Color.rgb(30, 30, 120));
		floorMaterial.setSpecularColor(Color.rgb(60, 60, 240));
		floor = new Box(unscaledWidth, unscaledHeight, 0.1);
		floor.setMaterial(floorMaterial);
		floor.setTranslateX(unscaledWidth / 2 - 4);
		floor.setTranslateY(unscaledHeight / 2 - 4);
		floor.setTranslateZ(3);
		getChildren().addAll(floor, brickRoot, foodRoot);
	}

	public void createWalls(PacManGameWorld world, Color wallColor) {
		Voxel[][][] voxels = new Voxel[world.numRows()][world.numCols()][Voxel.N * Voxel.N];
		
		// create N*N voxels for each wall tile
		int voxelCount = 0;
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				V2i tile = new V2i(col, row);
				if (world.isWall(tile)) {
					for (int i = 0; i < Voxel.N * Voxel.N; ++i) {
						voxels[row][col][i] = new Voxel(col, row, i);
						++voxelCount;
					}
				}
			}
		}
		log("%d voxels", voxelCount);
		
		// remove voxels inside the wall boundaries
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				for (int i = 0; i < Voxel.N * Voxel.N; ++i) {
					Voxel voxel = voxels[row][col][i];
					if (voxel == null) {
						continue;
					}
					if (world.isWall(voxel.north()) && world.isWall(voxel.east()) && world.isWall(voxel.south())
							&& world.isWall(voxel.west())) {
						V2i se = voxel.southOf(voxel.east());
						V2i sw = voxel.southOf(voxel.west());
						V2i ne = voxel.northOf(voxel.east());
						V2i nw = voxel.northOf(voxel.west());
						if (world.isWall(se) && !world.isWall(nw) || !world.isWall(se) && world.isWall(nw)
								|| world.isWall(sw) && !world.isWall(ne) || !world.isWall(sw) && world.isWall(ne)) {
							// keep corner
						} else {
							voxels[row][col][i] = null;
						}
					}
				}
			}
		}
		
		// create walls from voxels
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseColor(wallColor);
		material.setSpecularColor(wallColor.brighter());
		List<Box> bricks = createWalls(world, material, voxels);
		brickRoot.getChildren().setAll(bricks);
	}

	private List<Box> createWalls(PacManGameWorld world, PhongMaterial material, Voxel[][][] voxels) {
		List<Box> walls = new ArrayList<>();
		for (int y = 0; y < Voxel.N * world.numRows(); ++y) {
			Voxel wallStart = null;
			int wallWidth = 1;
			for (int x = 0; x < Voxel.N * world.numCols(); ++x) {
				int row = y / Voxel.N, col = x / Voxel.N;
				int i = (y % Voxel.N) * Voxel.N + (x % Voxel.N);
				Voxel voxel = voxels[row][col][i];
				if (voxel != null) {
					if (wallStart == null) {
						wallStart = voxel;
						wallWidth = 1;
					} else {
						wallWidth++;
					}
				} else {
					if (wallStart != null) {
						walls.add(createHorizontalWall(wallStart, material, wallWidth));
						wallStart = null;
						wallWidth = 1;
					}
				}
				if (x == Voxel.N * world.numCols() - 1 && wallStart != null) {
					walls.add(createHorizontalWall(wallStart, material, wallWidth));
					wallStart = null;
					wallWidth = 1;
				}
			}
			if (y == Voxel.N * world.numRows() - 1 && wallStart != null) {
				walls.add(createHorizontalWall(wallStart, material, wallWidth));
				wallStart = null;
				wallWidth = 1;
			}
		}
		log("%d walls created", walls.size());
		return walls;
	}

	private Box createHorizontalWall(Voxel voxel, PhongMaterial material, int n) {
		Box brick = new Box(n * Voxel.SIZE, Voxel.SIZE, Voxel.SIZE);
		brick.setMaterial(material);
		brick.setTranslateX(voxel.x() + (n - 1) * Voxel.SIZE * 0.5);
		brick.setTranslateY(voxel.y());
		brick.setTranslateZ(1.5);
		brick.drawModeProperty().bind(Env.$drawMode);
		return brick;
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