package de.amr.games.pacman.ui.fx.rendering;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Bonus;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.PacManGameAnimation;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Interface implemented by each scene rendering. Currently only spritesheet based rendering is
 * supported.
 * 
 * @author Armin Reichert
 */
public interface SceneRendering extends PacManGameAnimation {

	static Image exchangeColors(Image source, Map<Color, Color> exchanges) {
		WritableImage newImage = new WritableImage((int) source.getWidth(), (int) source.getHeight());
		for (int x = 0; x < source.getWidth(); ++x) {
			for (int y = 0; y < source.getHeight(); ++y) {
				Color oldColor = source.getPixelReader().getColor(x, y);
				for (Map.Entry<Color, Color> entry : exchanges.entrySet()) {
					if (oldColor.equals(entry.getKey())) {
						newImage.getPixelWriter().setColor(x, y, entry.getValue());
					}
				}
			}
		}
		return newImage;
	}

	Image spritesheet();

	GraphicsContext gc();

	Font getScoreFont();

	default void drawRegion(Rectangle2D region, double x, double y) {
		gc().drawImage(spritesheet(), region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
				region.getWidth(), region.getHeight());
	}

	Rectangle2D bonusSprite(Bonus bonus, PacManGameModel game);

	Rectangle2D pacSprite(Pac pac, PacManGameModel game);

	Rectangle2D ghostSprite(Ghost ghost, PacManGameModel game);

	void drawPac(Pac pac, PacManGameModel game);

	void drawGhost(Ghost ghost, PacManGameModel game);

	void drawBonus(Bonus bonus, PacManGameModel game);

	void hideTile(V2i tile);

	void drawMaze(int mazeNumber, int x, int y, boolean flashing);

	void drawFoodTiles(Stream<V2i> tiles, Predicate<V2i> eaten);

	void drawEnergizerTiles(Stream<V2i> energizerTiles);

	void drawScore(PacManGameModel game);

	void drawLivesCounter(PacManGameModel game, int x, int y);

	void drawLevelCounter(PacManGameModel game, int x, int y);

	void showGameState(PacManGameModel game);
}