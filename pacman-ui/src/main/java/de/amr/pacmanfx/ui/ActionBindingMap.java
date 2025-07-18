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

public class ActionBindingMap {

    private static final ActionBindingMap EMPTY_MAP = new ActionBindingMap();

    public static ActionBindingMap empty() { return EMPTY_MAP; }

    private final Keyboard keyboard;
    private final Map<KeyCombination, GameAction> bindings = new HashMap<>();

    public static Map.Entry<GameAction, Set<KeyCombination>> createActionBinding(GameAction action, KeyCombination... combinations) {
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

    private ActionBindingMap() {
        keyboard = null;
    }

    public ActionBindingMap(Keyboard keyboard) {
        this.keyboard = requireNonNull(keyboard);
    }

    public boolean isEmpty() {
        return bindings.isEmpty();
    }

    public Set<Map.Entry<KeyCombination, GameAction>> entrySet() {
        return bindings.entrySet();
    }

    public void updateKeyboard() {
        for (KeyCombination combination : bindings.keySet()) {
            keyboard.setBinding(combination, this);
        }
        bindings.entrySet().stream()
            // sort by string representation of key combination
            .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
            .forEach(entry -> Logger.debug("%-20s: %s".formatted(entry.getKey(), entry.getValue().name())));
        Logger.info("Key bindings updated");
    }

    public void removeFromKeyboard() {
        for (KeyCombination combination : bindings.keySet()) {
            keyboard.removeBinding(combination, this);
        }
        Logger.info("Key bindings removed");
    }

    public void bind(GameAction action, KeyCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        bindings.put(combination, action);
    }

    public void bind(GameAction gameAction, Map<GameAction, Set<KeyCombination>> bindings) {
        requireNonNull(gameAction);
        requireNonNull(bindings);
        if (bindings.values().stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Found null value in key bindings map");
        }
        if (bindings.containsKey(gameAction)) {
            for (KeyCombination combination : bindings.get(gameAction)) {
                this.bindings.put(combination, gameAction);
            }
        } else {
            Logger.error("No keyboard binding found for game action {}", gameAction);
        }
    }

    public Optional<GameAction> matchingAction() {
        return bindings.keySet().stream()
            .filter(keyboard::isMatching)
            .map(bindings::get)
            .findFirst();
    }

    public void runMatchingAction(GameUI ui) {
        matchingAction().ifPresent(action -> action.executeIfEnabled(ui));
    }

    public void runMatchingActionOrElse(GameUI ui, Runnable defaultAction) {
        matchingAction().ifPresentOrElse(action -> action.executeIfEnabled(ui), defaultAction);
    }
}
