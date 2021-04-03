package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.function.IntSupplier;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LivesCounter2D extends Renderable2D {

	private final IntSupplier lifeCountSupplier;
	private V2i tile;
	private Image spritesheet;
	private Rectangle2D lifeImage;

	public LivesCounter2D(IntSupplier lifeCountSupplier) {
		this.lifeCountSupplier = lifeCountSupplier;
	}

	@Override
	public void setRendering(GameRendering2D rendering) {
		setSpritesheet(rendering.spritesheet);
		setLifeImage(rendering.getLifeImage());
	}

	@Override
	public void render(GraphicsContext g) {
		int numLives = lifeCountSupplier.getAsInt();
		int maxLivesDisplayed = 5;
		double x = tile.x * TS, y = tile.y * TS;
		double w = lifeImage.getWidth(), h = lifeImage.getHeight();
		for (int i = 0; i < Math.min(numLives, maxLivesDisplayed); ++i) {
			g.drawImage(spritesheet, lifeImage.getMinX(), lifeImage.getMinY(), w, h, x + t(2 * i), y, w, h);
		}
		if (numLives > maxLivesDisplayed) {
			g.setFill(Color.YELLOW);
			g.setFont(Font.font("Sans Serif", FontWeight.BOLD, 6));
			g.fillText("+" + (numLives - maxLivesDisplayed), x + t(10), y + t(1) - 2);
		}
	}

	public V2i getTile() {
		return tile;
	}

	public void setLeftUpperCorner(V2i tile) {
		this.tile = tile;
	}

	public Image getSpritesheet() {
		return spritesheet;
	}

	public void setSpritesheet(Image spritesheet) {
		this.spritesheet = spritesheet;
	}

	public Rectangle2D getLifeImage() {
		return lifeImage;
	}

	public void setLifeImage(Rectangle2D lifeImage) {
		this.lifeImage = lifeImage;
	}
}