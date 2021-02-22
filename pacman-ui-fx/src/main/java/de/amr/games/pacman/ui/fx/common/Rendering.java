package de.amr.games.pacman.ui.fx.common;

import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface Rendering {

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	Color getMazeWallBorderColor(int mazeIndex);

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	Color getMazeWallColor(int mazeIndex);

	void drawLifeCounterSymbol(GraphicsContext g, int x, int y);

	void drawLivesCounter(GraphicsContext g, GameModel game, int x, int y);

	void drawPlayer(GraphicsContext g, Pac pac);

	void drawGhost(GraphicsContext g, Ghost ghost, boolean frightened);

	void drawBonus(GraphicsContext g, Bonus bonus);

	void drawTileCovered(GraphicsContext g, V2i tile);

	void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing);

	void drawFoodTiles(GraphicsContext g, Stream<V2i> tiles, Predicate<V2i> eaten);

	void drawEnergizerTiles(GraphicsContext g, Stream<V2i> energizerTiles);

	void drawGameState(GraphicsContext g, GameModel game);

	void drawScore(GraphicsContext g, GameModel game, boolean titleOnly);

	void drawLevelCounter(GraphicsContext g, GameModel game, int rightX, int y);

}
