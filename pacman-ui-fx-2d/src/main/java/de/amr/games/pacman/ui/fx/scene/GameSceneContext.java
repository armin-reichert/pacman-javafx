/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.Optional;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.PacManGamesUserInterface;
import de.amr.games.pacman.ui.fx.rendering2d.GameSpritesheet;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpritesheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpritesheet;

/**
 * @author Armin Reichert
 */
public class GameSceneContext {

	private final GameController gameController;
	private final PacManGamesUserInterface ui;
	private final PacManGameSpritesheet sheetPacMan;
	private final MsPacManGameSpritesheet sheetMsPacMan;
	private boolean scoreVisible;
	private boolean creditVisible;

	public GameSceneContext(GameController gameController, PacManGamesUserInterface ui,
			MsPacManGameSpritesheet sheetMsPacMan, PacManGameSpritesheet sheetPacMan) {
		checkNotNull(gameController);
		checkNotNull(ui);
		checkNotNull(sheetMsPacMan);
		checkNotNull(sheetPacMan);
		this.gameController = gameController;
		this.ui = ui;
		this.sheetMsPacMan = sheetMsPacMan;
		this.sheetPacMan = sheetPacMan;
	}

	public PacManGamesUserInterface ui() {
		return ui;
	}

	public GameSpritesheet spritesheet() {
		return gameVariant() == GameVariant.MS_PACMAN ? sheetMsPacMan : sheetPacMan;
	}

	public boolean isScoreVisible() {
		return scoreVisible;
	}

	public void setScoreVisible(boolean visible) {
		this.scoreVisible = visible;
	}

	public boolean isCreditVisible() {
		return creditVisible;
	}

	public void setCreditVisible(boolean visible) {
		this.creditVisible = visible;
	}

	public GameController gameController() {
		return gameController;
	}

	public GameModel game() {
		return gameController.game();
	}

	public GameVariant gameVariant() {
		return game().variant();
	}

	public GameState state() {
		return gameController.state();
	}

	public boolean hasCredit() {
		return game().hasCredit();
	}

	public Optional<GameLevel> level() {
		return game().level();
	}

	public Optional<World> world() {
		return level().map(GameLevel::world);
	}
}