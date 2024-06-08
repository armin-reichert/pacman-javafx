/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.steering.Steering;
import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.ui2d.util.KeyInput.control;
import static de.amr.games.pacman.ui2d.util.KeyInput.key;

/**
 * Controls Pac-Man using specified keys.
 *
 * @author Armin Reichert
 */
public class KeyboardPacSteering implements Steering {

    public final Map<KeyInput, Direction> mappings = new HashMap<>();

    /**
     * Default steering: unmodified cursor keys.
     */
    public KeyboardPacSteering() {
        mappings.put(KeyInput.of(key(KeyCode.UP)),        Direction.UP);
        mappings.put(KeyInput.of(key(KeyCode.DOWN)),      Direction.DOWN);
        mappings.put(KeyInput.of(key(KeyCode.LEFT)),      Direction.LEFT);
        mappings.put(KeyInput.of(key(KeyCode.RIGHT)),     Direction.RIGHT);
        mappings.put(KeyInput.of(control(KeyCode.UP)),    Direction.UP);
        mappings.put(KeyInput.of(control(KeyCode.DOWN)),  Direction.DOWN);
        mappings.put(KeyInput.of(control(KeyCode.LEFT)),  Direction.LEFT);
        mappings.put(KeyInput.of(control(KeyCode.RIGHT)), Direction.RIGHT);
    }

    @Override
    public void steer(Creature creature, World world) {
        for (var input : mappings.keySet()) {
            if (Keyboard.pressed(input)) {
                creature.setWishDir(mappings.get(input));
            }
        }
    }
}