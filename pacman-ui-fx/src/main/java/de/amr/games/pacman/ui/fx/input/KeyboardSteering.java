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

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final KeyboardSteering DEFAULT_STEERING = new KeyboardSteering(//
			new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN),
			new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN),
			new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN),
			new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN));

	private Map<KeyCodeCombination, Direction> directionByKey;
	private Direction dir;
	private boolean enabled = false;

	public KeyboardSteering(KeyCodeCombination up, KeyCodeCombination down, KeyCodeCombination left,
			KeyCodeCombination right) {
		directionByKey = Map.of(up, Direction.UP, down, Direction.DOWN, left, Direction.LEFT, right, Direction.RIGHT);
	}

	public KeyboardSteering(KeyCode up, KeyCode down, KeyCode left, KeyCode right) {
		directionByKey = Map.of(//
				new KeyCodeCombination(up), Direction.UP, //
				new KeyCodeCombination(down), Direction.DOWN, //
				new KeyCodeCombination(left), Direction.LEFT, //
				new KeyCodeCombination(right), Direction.RIGHT//
		);
	}

	@Override
	public void steer(GameLevel level, Creature guy) {
		if (dir != null) {
			guy.setWishDir(dir);
			dir = null;
		}
	}

	private boolean isSteeringEvent(KeyEvent e) {
		return directionByKey.keySet().stream().anyMatch(keyCombination -> keyCombination.match(e))
				|| DEFAULT_STEERING.directionByKey.keySet().stream().anyMatch(keyCombination -> keyCombination.match(e));
	}

	@Override
	public void handle(KeyEvent e) {
		if (!isSteeringEvent(e)) {
			return;
		}
		if (!enabled) {
			LOG.info("Steering disabled, ignore key event '%s'", e.getCode());
			e.consume();
			return;
		}
		dir = computeDirection(e).or(() -> DEFAULT_STEERING.computeDirection(e)).orElse(null);
		if (dir != null) {
			e.consume();
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		LOG.info("Steering %sabled", enabled ? "en" : "dis");
	}

	private Optional<Direction> computeDirection(KeyEvent event) {
		return directionByKey.keySet().stream()//
				.filter(kcc -> kcc.match(event)).findFirst()//
				.map(directionByKey::get);
	}
}