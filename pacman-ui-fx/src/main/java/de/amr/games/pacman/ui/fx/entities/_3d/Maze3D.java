package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * 3D-model for a maze.
 * 
 * TODO merge bricks into blocks
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	static final int N = 4;
	static final double SIZE = 8.0 / N;;

	static class MicroTile {

		private final V2i tile;
		private final int i;

		public MicroTile(V2i tile, int i) {
			this.tile = tile;
			this.i = i;
		}

		public double x() {
			return tile.x * TS - 1.5 * SIZE + (i % N) * SIZE;
		}

		public double y() {
			return tile.y * TS - 1.5 * SIZE + (i / N) * SIZE;
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
			MicroTile other = (MicroTile) obj;
			return i == other.i && Objects.equals(tile, other.tile);
		}

		@Override
		public String toString() {
			return String.format("tile:%s i:%d x:%.2f y:%.2f", tile, i, x(), y());
		}

	}

	private PacManGameWorld world;
	private MicroTile[][][] microTiles;

	public Maze3D(PacManGameWorld world, Color wallColor) {
		this.world = world;
		microTiles = new MicroTile[world.numRows()][world.numCols()][N * N];
		int count = 0;
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				V2i tile = new V2i(col, row);
				if (world.isWall(tile)) {
					for (int i = 0; i < N * N; ++i) {
						microTiles[row][col][i] = new MicroTile(tile, i);
						++count;
					}
				}
			}
		}
		log("%d micro tiles created", count);

		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				for (int i = 0; i < N * N; ++i) {
					MicroTile mt = microTiles[row][col][i];
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
							microTiles[row][col][i] = null;
						}
					}
				}
			}
		}
		List<Box> bricks = createBricks(wallColor);
		getChildren().addAll(bricks);
	}

	private List<Box> createBricks(Color wallColor) {
		PhongMaterial brickMaterial = new PhongMaterial(wallColor);
		brickMaterial.setSpecularColor(wallColor.brighter());
		List<Box> bricks = new ArrayList<>();
		for (int row = 0; row < world.numRows(); ++row) {
			for (int col = 0; col < world.numCols(); ++col) {
				for (int i = 0; i < N * N; ++i) {
					MicroTile mt = microTiles[row][col][i];
					if (mt != null) {
						bricks.add(createBrick(mt, brickMaterial));
					}
				}
			}
		}
		log("%d bricks created", bricks.size());
		return bricks;
	}

	private Box createBrick(MicroTile microTile, PhongMaterial material) {
		Box brick = new Box(SIZE, SIZE, SIZE);
		brick.setMaterial(material);
		brick.setTranslateX(microTile.x());
		brick.setTranslateY(microTile.y());
		brick.setTranslateZ(1.5);
		brick.drawModeProperty().bind(Env.$drawMode);
		return brick;
	}
}