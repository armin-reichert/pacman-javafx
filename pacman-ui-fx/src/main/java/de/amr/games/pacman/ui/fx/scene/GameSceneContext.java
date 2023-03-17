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

import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;

/**
 * @author Armin Reichert
 */
public class GameSceneContext {

	private final GameController gameController;
	private boolean scoreVisible;
	private boolean creditVisible;
	private Rendering2D rendering2D;

	public GameSceneContext(GameController gameController) {
		this.gameController = Objects.requireNonNull(gameController, "Game controller must not be NULL");
	}

	public Rendering2D rendering2D() {
		return rendering2D;
	}

	public void setRendering2D(Rendering2D renderer) {
		this.rendering2D = Objects.requireNonNull(renderer, "2D renderer must not be NULL");
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