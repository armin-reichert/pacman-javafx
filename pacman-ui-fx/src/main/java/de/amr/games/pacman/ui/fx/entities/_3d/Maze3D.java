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
 * 3D-model for a maze. Creates cubes representing the maze walls from the map data.
 * <p>
 * TODO merge cubes into quads
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	private static class Voxel extends V2i {

		static final int N = 4;
		static final double SIZE = 8.0 / N;;

		private final short i; // 0,...,N*N-1

		public Voxel(int tileX, int tileY, int i) {
			super(tileX, tileY);
			this.i = (short) i;
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

	// TODO this is most probably total newbie code, but for now it does the job
	public void createWalls(PacManGameWorld world, Color wallColor) {
		Voxel[][][] voxels = new Voxel[world.numRows()][world.numCols()][Voxel.N * Voxel.N];
		int count = 0;
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				V2i tile = new V2i(col, row);
				if (world.isWall(tile)) {
					for (int i = 0; i < Voxel.N * Voxel.N; ++i) {
						voxels[row][col][i] = new Voxel(col, row, i);
						++count;
					}
				}
			}
		}
		log("%d voxels", count);

		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				for (int i = 0; i < Voxel.N * Voxel.N; ++i) {
					Voxel mt = voxels[row][col][i];
					if (mt == null) {
						continue;
					}
					if (world.isWall(mt.north()) && world.isWall(mt.east()) && world.isWall(mt.south())
							&& world.isWall(mt.west())) {
						V2i seOf = mt.southOf(mt.east());
						V2i swOf = mt.southOf(mt.west());
						V2i neOf = mt.northOf(mt.east());
						V2i nwOf = mt.northOf(mt.west());
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
				for (int i = 0; i < Voxel.N * Voxel.N; ++i) {
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
		Box brick = new Box(Voxel.SIZE, Voxel.SIZE, Voxel.SIZE);
		brick.setMaterial(material);
		brick.setTranslateX(voxel.x());
		brick.setTranslateY(voxel.y());
		brick.setTranslateZ(1.5);
		brick.drawModeProperty().bind(Env.$drawMode);
		return brick;
	}
}