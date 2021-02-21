package de.amr.games.pacman.ui.fx.common;

import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.ui.PacManGameAnimation;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

/**
 * Common interface for scene renderings.
 * 
 * @author Armin Reichert
 */
public interface Rendering extends PacManGameAnimation {

	/** Spritesheet raster size */
	static final int RASTER = 16;

	static Rectangle2D tileRegion(int tileX, int tileY, int cols, int rows) {
		return new Rectangle2D(tileX * RASTER, tileY * RASTER, cols * RASTER, rows * RASTER);
	}

	default void drawRegion(GraphicsContext g, Rectangle2D region, double x, double y) {
		g.drawImage(spritesheet(), region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
				region.getWidth(), region.getHeight());
	}

	Image spritesheet();

	Rectangle2D bonusSpriteRegion(Bonus bonus);

	Rectangle2D pacSpriteRegion(Pac pac);

	Rectangle2D ghostSpriteRegion(Ghost ghost, boolean frightened);

	Font getScoreFont();

	void drawPac(GraphicsContext g, Pac pac);

	void drawGhost(GraphicsContext g, Ghost ghost, boolean frightened);

	void drawBonus(GraphicsContext g, Bonus bonus);

	void drawTileCovered(GraphicsContext g, V2i tile);

	void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing);

	void drawFoodTiles(GraphicsContext g, Stream<V2i> tiles, Predicate<V2i> eaten);

	void drawEnergizerTiles(GraphicsContext g, Stream<V2i> energizerTiles);

	void drawScore(GraphicsContext g, GameModel game, boolean titleOnly);

	void drawLivesCounter(GraphicsContext g, GameModel game, int x, int y);

	void drawLevelCounter(GraphicsContext g, GameModel game, int x, int y);

	void signalGameState(GraphicsContext g, GameModel game);
}