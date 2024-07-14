/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.GameWorld;
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

    public final Map<KeyInput, Direction> dirByKey = new HashMap<>();

    /**
     * Default steering: unmodified cursor keys.
     */
    public KeyboardPacSteering() {
        dirByKey.put(KeyInput.of(key(KeyCode.UP)),        Direction.UP);
        dirByKey.put(KeyInput.of(key(KeyCode.DOWN)),      Direction.DOWN);
        dirByKey.put(KeyInput.of(key(KeyCode.LEFT)),      Direction.LEFT);
        dirByKey.put(KeyInput.of(key(KeyCode.RIGHT)),     Direction.RIGHT);
        dirByKey.put(KeyInput.of(control(KeyCode.UP)),    Direction.UP);
        dirByKey.put(KeyInput.of(control(KeyCode.DOWN)),  Direction.DOWN);
        dirByKey.put(KeyInput.of(control(KeyCode.LEFT)),  Direction.LEFT);
        dirByKey.put(KeyInput.of(control(KeyCode.RIGHT)), Direction.RIGHT);
    }

    @Override
    public void steer(Creature creature, GameWorld world) {
        dirByKey.keySet().stream()
            .filter(Keyboard::pressed)
            .map(dirByKey::get)
            .findFirst()
            .ifPresent(creature::setWishDir);
    }
}