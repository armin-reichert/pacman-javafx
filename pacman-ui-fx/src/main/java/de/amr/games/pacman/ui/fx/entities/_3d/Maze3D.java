package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;

/**
 * 3D-model for a maze that is created from the simple textual map description.
 * 
 * @author Armin Reichert
 */
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
		List<MicroTile> microTiles = new ArrayList<>();
		double w = TS / 3.0, h = TS / 3.0;
		world.tiles().filter(world::isWall).forEach(tile -> {
			double bx = tile.x * TS - w, by = tile.y * TS - h;
			//@formatter:off
			microTiles.add(new MicroTile(bx,     by,     tile, 0));
			microTiles.add(new MicroTile(bx+w,   by,     tile, 1));
			microTiles.add(new MicroTile(bx+2*w, by,     tile, 2));
			microTiles.add(new MicroTile(bx,     by+h,   tile, 3));
			microTiles.add(new MicroTile(bx+w,   by+h,   tile, 4));
			microTiles.add(new MicroTile(bx+2*w, by+h,   tile, 5));
			microTiles.add(new MicroTile(bx,     by+2*h, tile, 6));
			microTiles.add(new MicroTile(bx+w,   by+2*h, tile, 7));
			microTiles.add(new MicroTile(bx+2*w, by+2*h, tile, 8));
			//@formatter:on
		});

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
		bricks = microTiles.stream().map(mt -> createBrick(mt.x, mt.y, brickMaterial)).collect(Collectors.toList());
	}

	public Node createPellet(V2i tile, PhongMaterial material) {
		Sphere s = new Sphere(1);
		s.setMaterial(material);
		s.setTranslateX(tile.x * TS);
		s.setTranslateY(tile.y * TS);
		s.setUserData(tile);
		return s;
	}

	private Box createBrick(double x, double y, PhongMaterial material) {
		Box brick = new Box(2, 2, 3);
		brick.setMaterial(material);
		brick.setTranslateX(x);
		brick.setTranslateY(y);
		brick.setTranslateZ(1.5);
		brick.drawModeProperty().bind(Env.$drawMode);
		return brick;
	}

	public List<Node> getBricks() {
		return Collections.unmodifiableList(bricks);
	}
}