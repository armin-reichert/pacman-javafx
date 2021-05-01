package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets.getFoodColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
 * 3D-model for a maze. Creates cubes representing the maze walls from the map data.
 * <p>
 * TODO merge cubes into quads
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	static final int N = 4;
	static final double VOXEL_SIZE = 8.0 / N;;

	private static class Voxel {

		private final V2i tile;
		private final int i;

		public Voxel(V2i tile, int i) {
			this.tile = tile;
			this.i = i;
		}

		public double x() {
			return tile.x * TS - 1.5 * VOXEL_SIZE + (i % N) * VOXEL_SIZE;
		}

		public double y() {
			return tile.y * TS - 1.5 * VOXEL_SIZE + (i / N) * VOXEL_SIZE;
		}

		public V2i northOf() {
			return tile.plus(toNorth());
		}

		public V2i westOf() {
			return tile.plus(toWest());
		}

		public V2i eastOf() {
			return tile.plus(toEast());
		}

		public V2i southOf() {
			return tile.plus(toSouth());
		}

		public V2i toNorth() {
			int dy = i / N == 0 ? -1 : 0;
			return new V2i(0, dy);
		}

		public V2i toEast() {
			int dx = i % N == N - 1 ? 1 : 0;
			return new V2i(dx, 0);
		}

		public V2i toSouth() {
			int dy = i / N == N - 1 ? 1 : 0;
			return new V2i(0, dy);
		}

		public V2i toWest() {
			int dx = i % N == 0 ? -1 : 0;
			return new V2i(dx, 0);
		}

		@Override
		public int hashCode() {
			return Objects.hash(i, tile);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Voxel other = (Voxel) obj;
			return i == other.i && Objects.equals(tile, other.tile);
		}

		@Override
		public String toString() {
			return String.format("tile:%s i:%d x:%.2f y:%.2f", tile, i, x(), y());
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

	// TODO this is most probably absoulte newbie code but for now it does the job
	public void createWalls(PacManGameWorld world, Color wallColor) {
		Voxel[][][] voxels = new Voxel[world.numRows()][world.numCols()][N * N];
		int count = 0;
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				V2i tile = new V2i(col, row);
				if (world.isWall(tile)) {
					for (int i = 0; i < N * N; ++i) {
						voxels[row][col][i] = new Voxel(tile, i);
						++count;
					}
				}
			}
		}
		log("%d micro tiles created", count);

		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				for (int i = 0; i < N * N; ++i) {
					Voxel mt = voxels[row][col][i];
					if (mt == null) {
						continue;
					}
					if (world.isWall(mt.northOf()) && world.isWall(mt.eastOf()) && world.isWall(mt.southOf())
							&& world.isWall(mt.westOf())) {
						V2i seOf = mt.southOf().plus(mt.toEast());
						V2i swOf = mt.southOf().plus(mt.toWest());
						V2i neOf = mt.northOf().plus(mt.toEast());
						V2i nwOf = mt.northOf().plus(mt.toWest());
						if (world.isWall(seOf) && !world.isWall(nwOf) || !world.isWall(seOf) && world.isWall(nwOf)
								|| world.isWall(swOf) && !world.isWall(neOf) || !world.isWall(swOf) && world.isWall(neOf)) {
							// keep corner
						} else {
							voxels[row][col][i] = null;
						}
					}
				}
			}
		}
		List<Box> bricks = createBricks(world, wallColor, voxels);
		brickRoot.getChildren().setAll(bricks);
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

	public Stream<Node> foodNodes() {
		return foodRoot.getChildren().stream();
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

	private List<Box> createBricks(PacManGameWorld world, Color wallColor, Voxel[][][] voxels) {
		PhongMaterial brickMaterial = new PhongMaterial(wallColor);
		brickMaterial.setSpecularColor(wallColor.brighter());
		List<Box> bricks = new ArrayList<>();
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				for (int i = 0; i < N * N; ++i) {
					Voxel voxel = voxels[row][col][i];
					if (voxel != null) {
						bricks.add(createBrick(voxel, brickMaterial));
					}
				}
			}
		}
		log("%d bricks created", bricks.size());
		return bricks;
	}

	private Box createBrick(Voxel voxel, PhongMaterial material) {
		Box brick = new Box(VOXEL_SIZE, VOXEL_SIZE, VOXEL_SIZE);
		brick.setMaterial(material);
		brick.setTranslateX(voxel.x());
		brick.setTranslateY(voxel.y());
		brick.setTranslateZ(1.5);
		brick.drawModeProperty().bind(Env.$drawMode);
		return brick;
	}
}