/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public interface GameAction {

    Runnable NO_ACTION = () -> {};

    static boolean executeCalledAction(GameContext context, GameAction... candidates) {
        Optional<GameAction> calledAction = Stream.of(candidates).filter(GameAction::called).findFirst();
        if (calledAction.isPresent()) {
            calledAction.get().execute(context);
            return true;
        }
        return false;
    }

    default boolean executeIf(GameContext context, BooleanSupplier condition) {
        if (called() && condition.getAsBoolean()) {
            execute(context);
            return true;
        }
        return false;
    }

    void execute(GameContext context);

    /**
     * @return {@code true} if any key combination or other trigger defined for this game key is pressed
     */
    boolean called();

    KeyInput trigger();
}
