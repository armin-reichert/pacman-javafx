package de.amr.games.pacman.ui.fx.rendering;

import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;

public interface Rendering<GC, COLOR> {

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	COLOR getMazeWallBorderColor(int mazeIndex);

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	COLOR getMazeWallColor(int mazeIndex);

	void drawLifeCounterSymbol(GC g, int x, int y);

	void drawLivesCounter(GC g, GameModel game, int x, int y);

	void drawPlayer(GC g, Pac pac);

	void drawGhost(GC g, Ghost ghost, boolean frightened);

	void drawBonus(GC g, Bonus bonus);

	void drawTileCovered(GC g, V2i tile);

	void drawMaze(GC g, int mazeNumber, int x, int y, boolean flashing);

	void drawFoodTiles(GC g, Stream<V2i> tiles, Predicate<V2i> eaten);

	void drawEnergizerTiles(GC g, Stream<V2i> energizerTiles);

	void drawGameState(GC g, GameModel game);

	void drawScore(GC g, GameModel game, boolean titleOnly);

	void drawLevelCounter(GC g, GameModel game, int rightX, int y);

	// Ms. Pac-Man only

	void drawSpouse(GC g, Pac pac);

	void drawStork(GC g, GameEntity stork);

	void drawBag(GC g, GameEntity bag);

	void drawJunior(GC g, GameEntity junior);

	void drawHeart(GC g, GameEntity heart);

}