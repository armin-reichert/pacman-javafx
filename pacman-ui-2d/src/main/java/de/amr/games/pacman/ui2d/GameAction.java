/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public interface GameAction {

    static Optional<GameAction> calledAction(GameAction... candidates) {
        return Stream.of(candidates).filter(GameAction::called).findFirst();
    }

    void execute(GameContext context);

    /**
     * @return {@code true} if any key combination or other trigger defined for this game key is pressed
     */
    boolean called();

    /**
     * @return the key input (set of key combinations) that triggers this action
     */
    KeyInput trigger();
}
