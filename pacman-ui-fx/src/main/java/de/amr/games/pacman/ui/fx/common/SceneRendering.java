package de.amr.games.pacman.ui.fx.common;

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

	/** Spritesheet raster size */
	static final int RASTER = 16;

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

	static Rectangle2D tileRegion(int tileX, int tileY, int cols, int rows) {
		return new Rectangle2D(tileX * RASTER, tileY * RASTER, cols * RASTER, rows * RASTER);
	}

	Image spritesheet();

	Rectangle2D bonusSprite(Bonus bonus);

	Rectangle2D pacSprite(Pac pac);

	Rectangle2D ghostSprite(Ghost ghost, boolean frightened);

	Font getScoreFont();

	default void drawRegion(GraphicsContext g, Rectangle2D region, double x, double y) {
		g.drawImage(spritesheet(), region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
				region.getWidth(), region.getHeight());
	}

	default Rectangle2D toRegion(Rectangle2D tile) {
		return new Rectangle2D(RASTER * tile.getMinX(), RASTER * tile.getMinY(), RASTER * tile.getWidth(),
				RASTER * tile.getHeight());
	}

	void drawPac(GraphicsContext g, Pac pac);

	void drawGhost(GraphicsContext g, Ghost ghost, boolean frightened);

	void drawBonus(GraphicsContext g, Bonus bonus);

	void hideTile(GraphicsContext g, V2i tile);

	void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing);

	void drawFoodTiles(GraphicsContext g, Stream<V2i> tiles, Predicate<V2i> eaten);

	void drawEnergizerTiles(GraphicsContext g, Stream<V2i> energizerTiles);

	void drawScore(GraphicsContext g, PacManGameModel game, boolean titleOnly);

	void drawLivesCounter(GraphicsContext g, PacManGameModel game, int x, int y);

	void drawLevelCounter(GraphicsContext g, PacManGameModel game, int x, int y);

	void signalGameState(GraphicsContext g, PacManGameModel game);
}