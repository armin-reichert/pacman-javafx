package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Maze2D {

	private V2i tile;
	private Image image;

	public void render(GraphicsContext g) {
		g.drawImage(image, t(tile.x), t(tile.y));
	}

	public void setTile(V2i tile) {
		this.tile = tile;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public void hideEatenFoodTiles(GraphicsContext g, Stream<V2i> foodTiles, Predicate<V2i> eaten) {
		g.setFill(Color.BLACK);
		foodTiles.filter(eaten).forEach(foodTile -> {
			g.fillRect(foodTile.x * TS, foodTile.y * TS, TS, TS);
		});
	}
}