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

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Creature;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 * Controls Pac-Man using specified keys.
 * 
 * @author Armin Reichert
 */
public class KeyboardSteering implements Steering, EventHandler<KeyEvent> {

	private static final KeyboardSteering DEFAULT_STEERING = new KeyboardSteering(//
			new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN),
			new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN),
			new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN),
			new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN));

	private final EnumMap<Direction, KeyCodeCombination> keyCombinations = new EnumMap<>(Direction.class);
	private Direction dir;

	public KeyboardSteering(KeyCodeCombination kccUp, KeyCodeCombination kccDown, KeyCodeCombination kccLeft,
			KeyCodeCombination kccRight) {
		keyCombinations.put(Direction.UP, kccUp);
		keyCombinations.put(Direction.DOWN, kccDown);
		keyCombinations.put(Direction.LEFT, kccLeft);
		keyCombinations.put(Direction.RIGHT, kccRight);
	}

	public KeyboardSteering(KeyCode keyUp, KeyCode keyDown, KeyCode keyLeft, KeyCode keyRight) {
		keyCombinations.put(Direction.UP, new KeyCodeCombination(keyUp));
		keyCombinations.put(Direction.DOWN, new KeyCodeCombination(keyDown));
		keyCombinations.put(Direction.LEFT, new KeyCodeCombination(keyLeft));
		keyCombinations.put(Direction.RIGHT, new KeyCodeCombination(keyRight));
	}

	@Override
	public void steer(GameLevel level, Creature guy) {
		if (dir != null) {
			guy.setWishDir(dir);
			dir = null;
		}
	}

	@Override
	public void handle(KeyEvent event) {
		dir = computeDirection(event).or(() -> DEFAULT_STEERING.computeDirection(event)).orElse(null);
		if (dir != null) {
			event.consume();
		}
	}

	private Optional<Direction> computeDirection(KeyEvent event) {
		return keyCombinations.entrySet().stream()//
				.filter(e -> e.getValue().match(event)).findFirst()//
				.map(Map.Entry::getKey);
	}
}