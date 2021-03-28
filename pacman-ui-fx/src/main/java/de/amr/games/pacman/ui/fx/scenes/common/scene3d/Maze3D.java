package de.amr.games.pacman.ui.fx.scenes.common.scene3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.world.PacManGameWorld;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public class Maze3D {

	private static class MicroTile {

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
			return String.format("tile:%s i:%d x:%.2f y:%.2f", tile, i, x, y);
		}

		private final double x;
		private final double y;
		private final V2i tile;
		private final int i;

		public MicroTile(double x, double y, V2i tile, int i) {
			this.x = x;
			this.y = y;
			this.tile = tile;
			this.i = i;
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
			return new V2i(0, i < 3 ? -1 : 0);
		}

		public V2i toEast() {
			return new V2i(i == 2 || i == 5 || i == 8 ? 1 : 0, 0);
		}

		public V2i toSouth() {
			return new V2i(0, i > 5 ? 1 : 0);
		}

		public V2i toWest() {
			return new V2i(i == 0 || i == 3 || i == 6 ? -1 : 0, 0);
		}
	}

	private List<Node> bricks;

	public Maze3D(AbstractGameModel game, Color wallColor) {
		PacManGameWorld world = game.currentLevel.world;
		List<MicroTile> brickPositions = new ArrayList<>();
		world.tiles().filter(game.currentLevel.world::isWall).forEach(tile -> {
			double w = TS / 3.0, h = TS / 3.0;
			double bx = tile.x * TS - w, by = tile.y * TS - h;
			List<MicroTile> small = new ArrayList<>();
			//@formatter:off
			small.add(new MicroTile(bx,     by,     tile, 0));
			small.add(new MicroTile(bx+w,   by,     tile, 1));
			small.add(new MicroTile(bx+2*w, by,     tile, 2));
			small.add(new MicroTile(bx,     by+h,   tile, 3));
			small.add(new MicroTile(bx+w,   by+h,   tile, 4));
			small.add(new MicroTile(bx+2*w, by+h,   tile, 5));
			small.add(new MicroTile(bx,     by+2*h, tile, 6));
			small.add(new MicroTile(bx+w,   by+2*h, tile, 7));
			small.add(new MicroTile(bx+2*w, by+2*h, tile, 8));
			//@formatter:on
			brickPositions.addAll(small);
		});

		List<MicroTile> positionsToRemove = new ArrayList<>();
		for (MicroTile t : brickPositions) {
			if (world.isWall(t.northOf()) && world.isWall(t.eastOf()) && world.isWall(t.southOf())
					&& world.isWall(t.westOf())) {
				V2i seOf = t.southOf().plus(t.toEast());
				V2i swOf = t.southOf().plus(t.toWest());
				V2i neOf = t.northOf().plus(t.toEast());
				V2i nwOf = t.northOf().plus(t.toWest());
				if (world.isWall(seOf) && !world.isWall(nwOf) || !world.isWall(seOf) && world.isWall(nwOf)
						|| world.isWall(swOf) && !world.isWall(neOf) || !world.isWall(swOf) && world.isWall(neOf)) {
					// keep corner
				} else {
					positionsToRemove.add(t);
				}
			}
		}
		brickPositions.removeAll(positionsToRemove);
		bricks = brickPositions.stream().map(mt -> new Brick3D(mt.x, mt.y, 2, 2, 3, Assets3D.randomWallMaterial(), mt.tile))
				.collect(Collectors.toList());
	}

	public List<Node> getWalls() {
		return Collections.unmodifiableList(bricks);
	}
}
