/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.input;

import de.amr.games.pacman.controller.Steering;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Creature;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.Modifier;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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