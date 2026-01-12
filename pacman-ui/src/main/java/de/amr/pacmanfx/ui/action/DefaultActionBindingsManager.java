/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCombination;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class DefaultActionBindingsManager implements ActionBindingsManager {

    private final Map<KeyCombination, GameAction> actionForKeyCombination = new HashMap<>();

    public DefaultActionBindingsManager() {}

    @Override
    public void dispose() {
        actionForKeyCombination.clear();
    }

    @Override
    public Map<KeyCombination, GameAction> actionForKeyCombination() {
        return actionForKeyCombination;
    }

    @Override
    public boolean hasNoBindings() {
        return actionForKeyCombination.isEmpty();
    }

    @Override
    public void activateBindings(Keyboard keyboard) {
        for (KeyCombination combination : actionForKeyCombination.keySet()) {
            keyboard.setBinding(combination, this);
        }
        actionForKeyCombination.entrySet().stream()
            // sort by string representation of key combination
            .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
            .forEach(entry -> Logger.debug("%-20s: %s".formatted(entry.getKey(), entry.getValue().name())));
        Logger.info("Key bindings updated");
    }

    @Override
    public void releaseBindings(Keyboard keyboard) {
        for (KeyCombination combination : actionForKeyCombination.keySet()) {
            keyboard.removeBinding(combination, this);
        }
        Logger.info("Key bindings removed");
    }

    @Override
    public void setKeyCombination(GameAction action, KeyCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionForKeyCombination.put(combination, action);
    }

    @Override
    public void useAnyBinding(GameAction gameAction, Set<ActionBinding> actionBindings) {
        requireNonNull(gameAction);
        requireNonNull(actionBindings);
        actionBindings.stream()
            .filter(actionBinding -> actionBinding.gameAction() == gameAction)
            .findAny()
            .ifPresent(this::useBinding);
    }

    @Override
    public void useAllBindings(Set<ActionBinding> actionBindings) {
        for (ActionBinding binding : actionBindings) {
            useBinding(binding);
        }
    }

    @Override
    public Optional<GameAction> matchingAction(Keyboard keyboard) {
        return actionForKeyCombination.keySet().stream()
            .filter(keyboard::isMatching)
            .map(actionForKeyCombination::get)
            .findFirst();
    }

    private void useBinding(ActionBinding binding) {
        for (KeyCombination combination : binding.keyCombinations()) {
            actionForKeyCombination.put(combination, binding.gameAction());
        }
    }
}