/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;

import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public interface GameAction {

    Runnable NO_ACTION = () -> {};

    static void executeCalledAction(GameContext context, Runnable defaultAction, GameAction... actions) {
        Stream.of(actions).filter(GameAction::called).findFirst().ifPresentOrElse(action -> action.execute(context), defaultAction);
    }

    void execute(GameContext context);

    /**
     * @return {@code true} if any key combination or other trigger defined for this game key is pressed
     */
    boolean called();

    KeyInput trigger();
}
