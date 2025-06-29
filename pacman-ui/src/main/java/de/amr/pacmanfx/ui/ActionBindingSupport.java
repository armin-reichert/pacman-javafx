/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;

/**
 * Collection of bindings between actions and key combinations. Implemented by the "views" (game view,
 * editor view, start pages view) and the game scenes.
 */
public interface ActionBindingSupport {

    static Map.Entry<GameAction, Set<KeyCombination>> binding(GameAction action, KeyCombination... combinations) {
        requireNonNull(combinations);
        if (combinations.length == 0) {
            throw new IllegalArgumentException("No key combinations specified for action " + action);
        }
        for (KeyCombination combination: combinations) {
            if (combination == null) {
                throw new IllegalArgumentException("Found null value in key combinations for action " + action);
            }
        }
        var combinationSet = Set.of(combinations);
        return entry(action, combinationSet);
    }

    /**
     * @return the keyboard providing the ke combinations
     */
    Keyboard keyboard();

    /**
     * @return map for storing the key combination to action binding information
     */
    Map<KeyCombination, GameAction> actionBindings();

    default void updateActionBindings() {
        for (KeyCombination combination : actionBindings().keySet()) {
            keyboard().setBinding(combination, this);
        }
        Logger.info("Key bindings updated for {}", getClass().getSimpleName());
        actionBindings().entrySet().stream()
                // sort by string representation of key combination
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .forEach(entry -> Logger.debug("%-20s: %s".formatted(entry.getKey(), entry.getValue().name())));
    }

    default void clearActionBindings() {
        for (KeyCombination combination : actionBindings().keySet()) {
            keyboard().removeBinding(combination, this);
        }
        Logger.info("Key bindings cleared for {}", getClass().getSimpleName());
    }

    default void bindActionToKeyCombination(GameAction action, KeyCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionBindings().put(combination, action);
    }

    default void bindAction(GameAction gameAction, Map<GameAction, Set<KeyCombination>> bindings) {
        requireNonNull(gameAction);
        requireNonNull(bindings);
        if (bindings.values().stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Found null value in key bindings map");
        }
        if (bindings.containsKey(gameAction)) {
            for (KeyCombination combination : bindings.get(gameAction)) {
                actionBindings().put(combination, gameAction);
            }
        } else {
            Logger.error("No keyboard binding found for game action {}", gameAction);
        }
    }

    default Optional<GameAction> matchingAction() {
        return actionBindings().keySet().stream()
            .filter(keyboard()::isMatching)
            .map(actionBindings()::get)
            .findFirst();
    }

    default void runMatchingAction(PacManGames_UI ui) {
        matchingAction().ifPresent(action -> GameAction.executeIfEnabled(ui, action));
    }

    default void runMatchingActionOrElse(PacManGames_UI ui, Runnable defaultAction) {
        matchingAction().ifPresentOrElse(action -> GameAction.executeIfEnabled(ui, action), defaultAction);
    }
}