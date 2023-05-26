/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
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
import de.amr.games.pacman.ui.fx.rendering2d.GameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;

/**
 * @author Armin Reichert
 */
public class GameSceneContext {

	private final GameController gameController;
	private final PacManGamesUserInterface ui;
	private final PacManGameRenderer rendererPacMan;
	private final MsPacManGameRenderer rendererMsPacMan;
	private boolean scoreVisible;
	private boolean creditVisible;

	public GameSceneContext(GameController gameController, PacManGamesUserInterface ui,
			MsPacManGameRenderer rendererMsPacMan, PacManGameRenderer rendererPacMan) {
		checkNotNull(gameController);
		checkNotNull(ui);
		checkNotNull(rendererMsPacMan);
		checkNotNull(rendererPacMan);
		this.gameController = gameController;
		this.ui = ui;
		this.rendererMsPacMan = rendererMsPacMan;
		this.rendererPacMan = rendererPacMan;
	}

	public PacManGamesUserInterface ui() {
		return ui;
	}

	public GameRenderer renderer() {
		return gameVariant() == GameVariant.MS_PACMAN ? rendererMsPacMan : rendererPacMan;
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