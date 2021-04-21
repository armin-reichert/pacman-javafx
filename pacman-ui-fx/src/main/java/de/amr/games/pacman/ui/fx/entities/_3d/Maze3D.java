package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * 3D-model for a maze.
 * 
 * @author Armin Reichert
 */
public class Maze3D {

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

	private List<Node> bricks;

	public Maze3D(PacManGameWorld world, Color wallColor) {
		List<MicroTile> microTiles = new ArrayList<>();
		world.tiles().filter(world::isWall).forEach(tile -> {
			for (int i = 0; i < N * N; ++i)
				microTiles.add(new MicroTile(tile, i));
		});
		log("%d micro tiles created", microTiles.size());

		List<MicroTile> microTilesToRemove = new ArrayList<>();
		for (MicroTile mt : microTiles) {
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
					microTilesToRemove.add(mt);
				}
			}
		}
		microTiles.removeAll(microTilesToRemove);

		PhongMaterial brickMaterial = new PhongMaterial(wallColor);
		bricks = microTiles.stream().map(mt -> createBrick(mt.x(), mt.y(), brickMaterial)).collect(Collectors.toList());
		log("%d bricks created", bricks.size());
	}

	public List<Node> getBricks() {
		return Collections.unmodifiableList(bricks);
	}

	private Box createBrick(double x, double y, PhongMaterial material) {
		Box brick = new Box(SIZE, SIZE, SIZE);
		brick.setMaterial(material);
		brick.setTranslateX(x);
		brick.setTranslateY(y);
		brick.setTranslateZ(1.5);
		brick.drawModeProperty().bind(Env.$drawMode);
		return brick;
	}
}