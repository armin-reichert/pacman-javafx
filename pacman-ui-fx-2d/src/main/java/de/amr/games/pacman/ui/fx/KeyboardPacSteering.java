/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Steering;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.HashMap;
import java.util.Map;

/**
 * Controls Pac-Man using specified keys.
 *
 * @author Armin Reichert
 */
public class KeyboardPacSteering implements Steering {

    public final Map<KeyCodeCombination, Direction> mappings = new HashMap<>();

    /**
     * Default steering: unmodified cursor keys.
     */
    public KeyboardPacSteering() {
        mappings.put(Keyboard.just(KeyCode.UP),       Direction.UP);
        mappings.put(Keyboard.just(KeyCode.DOWN),     Direction.DOWN);
        mappings.put(Keyboard.just(KeyCode.LEFT),     Direction.LEFT);
        mappings.put(Keyboard.just(KeyCode.RIGHT),    Direction.RIGHT);
        mappings.put(Keyboard.control(KeyCode.UP),    Direction.UP);
        mappings.put(Keyboard.control(KeyCode.DOWN),  Direction.DOWN);
        mappings.put(Keyboard.control(KeyCode.LEFT),  Direction.LEFT);
        mappings.put(Keyboard.control(KeyCode.RIGHT), Direction.RIGHT);
    }

    @Override
    public void steer(Creature creature, World world) {
        for (var combination : mappings.keySet()) {
            if (Keyboard.pressed(combination)) {
                creature.setWishDir(mappings.get(combination));
            }
        }
    }
}