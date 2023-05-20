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
package de.amr.games.pacman.ui.fx.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.Steering;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Creature;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;

/**
 * Controls Pac-Man using specified keys.
 * 
 * @author Armin Reichert
 */
public class KeyboardSteering extends Steering implements EventHandler<KeyEvent> {

	protected Map<KeyCodeCombination, Direction> dirByCombination = new HashMap<>();
	protected Direction dir;

	/**
	 * Default steering: unmodified cursor keys.
	 */
	public KeyboardSteering() {
		put(new KeyCodeCombination(KeyCode.UP), Direction.UP);
		put(new KeyCodeCombination(KeyCode.DOWN), Direction.DOWN);
		put(new KeyCodeCombination(KeyCode.LEFT), Direction.LEFT);
		put(new KeyCodeCombination(KeyCode.RIGHT), Direction.RIGHT);
	}

	@Override
	public void handle(KeyEvent event) {
		if (combinations().noneMatch(c -> c.match(event))) {
			return;
		}
		if (!isEnabled()) {
			Logger.trace("Steering disabled, ignore key event '{}'", event.getCode());
			event.consume();
			return;
		}
		dir = computeDirection(event).orElse(null);
		if (dir != null) {
			event.consume();
		}
	}

	@Override
	public void steer(GameLevel level, Creature guy) {
		if (dir != null) {
			guy.setWishDir(dir);
			dir = null;
		}
	}

	public void define(Direction dir, KeyCode code, Modifier... modifiers) {
		dirByCombination.put(new KeyCodeCombination(code, modifiers), dir);
	}

	public void put(KeyCodeCombination combination, Direction dir) {
		dirByCombination.put(combination, dir);
	}

	public Stream<KeyCodeCombination> combinations() {
		return dirByCombination.keySet().stream();
	}

	public boolean isSteeringEvent(KeyEvent event) {
		return combinations().anyMatch(c -> c.match(event));
	}

	public Optional<Direction> computeDirection(KeyEvent event) {
		return combinations().filter(c -> c.match(event)).findFirst().map(dirByCombination::get);
	}
}