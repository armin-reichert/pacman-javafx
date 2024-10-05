/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public interface GameAction {

    static Optional<GameAction> calledAction(Keyboard keyboard, GameAction... candidates) {
        return Stream.of(candidates).filter(action -> action.called(keyboard)).findFirst();
    }

    static Optional<GameAction> calledAction(Keyboard keyboard, List<GameAction> candidates) {
        return candidates.stream().filter(action -> action.called(keyboard)).findFirst();
    }

    void execute(GameContext context);

    /**
     * @return {@code true} if any key combination or other trigger defined for this game key is pressed
     */
    boolean called(Keyboard keyboard);

    /**
     * @return the key input (set of key combinations) that triggers this action
     */
    KeyInput trigger();
}
