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

    /**
     * Executes the first action from the given list that has been called by user input.
     *
     * @param context game context
     * @param candidates actions that will be checked
     * @return {@code true} if an action from the list has been executed
     */
    static boolean executeActionIfCalled(GameContext context, GameAction... candidates) {
        Optional<GameAction> calledAction = Stream.of(candidates).filter(GameAction::called).findFirst();
        if (calledAction.isPresent()) {
            calledAction.get().execute(context);
            return true;
        }
        return false;
    }

    /**
     * Executes this action if it has been called by user input and if condition holds.
     *
     * @param context game context
     * @param condition condition that must hold if action is executed
     * @return {@code true} if action has been executed
     */
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

    /**
     * @return the key input (set of key combinations) that triggers this action
     */
    KeyInput trigger();
}
