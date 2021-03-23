package de.amr.games.pacman.ui.fx.scenes.common.scene3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

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

		public V2i up() {
			return new V2i(0, i < 3 ? -1 : 0);
		}

		public V2i right() {
			return new V2i(i == 2 || i == 5 || i == 8 ? 1 : 0, 0);
		}

		public V2i down() {
			return new V2i(0, i > 5 ? 1 : 0);
		}

		public V2i left() {
			return new V2i(i == 0 || i == 3 || i == 6 ? -1 : 0, 0);
		}

		public V2i up_right() {
			return up().plus(right());
		}

		public V2i up_left() {
			return up().plus(left());
		}

		public V2i down_right() {
			return down().plus(right());
		}

		public V2i down_left() {
			return down().plus(left());
		}

	}

	private List<Node> bricks;

	public Maze3D(GameModel game, Color wallColor) {
		List<MicroTile> brickPositions = new ArrayList<>();
		game.level.world.tiles().filter(game.level.world::isWall).forEach(tile -> {
			double w = 8.0 / 3, h = 8.0 / 3;
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
			V2i northOf = t.tile.plus(t.up()), eastOf = t.tile.plus(t.right()), southOf = t.tile.plus(t.down()),
					westOf = t.tile.plus(t.left());
			if (game.level.world.isWall(northOf) && game.level.world.isWall(eastOf) && game.level.world.isWall(southOf)
					&& game.level.world.isWall(westOf)) {
				V2i seOf = t.tile.plus(t.down_right()), swOf = t.tile.plus(t.down_left()), neOf = t.tile.plus(t.up_right()),
						nwOf = t.tile.plus(t.up_left());
				if (game.level.world.isWall(seOf) && !game.level.world.isWall(nwOf)) {
					// keep corner
				} else if (!game.level.world.isWall(seOf) && game.level.world.isWall(nwOf)) {
					// keep corner
				} else if (game.level.world.isWall(swOf) && !game.level.world.isWall(neOf)) {
					// keep corner
				} else if (!game.level.world.isWall(swOf) && game.level.world.isWall(neOf)) {
					// keep corner
				} else {
					positionsToRemove.add(t);
				}
			}
		}
		brickPositions.removeAll(positionsToRemove);

		PhongMaterial wallMaterial = new PhongMaterial(wallColor);
		Image texture = Assets3D.randomArea(Assets3D.WALL_TEXTURE, 64, 64);
		wallMaterial.setBumpMap(texture);
		wallMaterial.setDiffuseMap(texture);

		bricks = new ArrayList<>();
		IntStream.range(0, 36 * 3).forEach(yy -> {
			List<MicroTile> row = brickPositions.stream().filter(t -> yy == 3 * t.tile.y + (t.i / 3))
					.sorted(Comparator.comparing(t -> t.tile.x)).collect(Collectors.toList());
			log("Process row of length %d at yy=%d", row.size(), yy);
			buildRow(row, yy, wallMaterial);
		});
	}

	private void buildRow(List<MicroTile> row, int yy, PhongMaterial wallMaterial) {
		if (row.size() == 0) {
			return;
		}
		Deque<MicroTile> started = new ArrayDeque<>();
		Deque<MicroTile> completed = new ArrayDeque<>();
		for (int i = 0; i < row.size(); i++) {
			MicroTile next = row.get(i);
			if (started.isEmpty()) {
				started.add(next);
			} else {
				MicroTile last = started.getLast();
				if (next.tile.x == last.tile.x || next.tile.x == last.tile.x + 1) {
					started.add(next);
				} else {
					completed = new ArrayDeque<>(started);
					started.clear();
					started.add(next);
				}
			}
			if (completed.size() > 0) {
				log("Horizontal wall of size %d at yy=%d", completed.size(), yy);
				for (MicroTile w : completed) {
					bricks.add(new Brick3D(w.x, w.y, 2, 2, 8, wallMaterial, w.tile));
				}
				completed.clear();
			}
			if (i == row.size() - 1 && started.size() > 0) {
				for (MicroTile w : started) {
					bricks.add(new Brick3D(w.x, w.y, 2, 2, 8, wallMaterial, w.tile));
				}
			}
		}
	}

	public List<Node> getWalls() {
		return Collections.unmodifiableList(bricks);
	}
}
