/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class GameActionBindingsSet implements ActionBindingsSet {

    private final Map<KeyCodeCombination, GameAction> bindingMap = new HashMap<>();

    public GameActionBindingsSet() {}

    @Override
    public void dispose() {
        bindingMap.clear();
        Logger.info("Action bindings disposed: {}", this);
    }

    @Override
    public Map<KeyCodeCombination, GameAction> bindingMap() {
        return bindingMap;
    }

    @Override
    public void setKeyCombination(GameAction action, KeyCodeCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        bindingMap.put(combination, action);
    }

    @Override
    public void registerFirstBinding(GameAction gameAction, Set<ActionBinding> bindings) {
        requireNonNull(gameAction);
        requireNonNull(bindings);

        final List<ActionBinding> matchingBindings = bindings.stream()
            .filter(actionBinding -> actionBinding.gameAction() == gameAction)
            .toList();

        final int count = matchingBindings.size();
        if (count == 0) {
            return;
        }
        if (count > 1) {
            Logger.warn("Found {} bindings for action {}, selecting first one", count, gameAction);
        }
        registerBinding(matchingBindings.getFirst());
    }

    @Override
    public void registerAllBindings(Set<ActionBinding> bindings) {
        requireNonNull(bindings);
        for (ActionBinding binding : bindings) {
            registerBinding(binding);
        }
    }

    @Override
    public Optional<GameAction> actionMatchingKeyboardState(Keyboard keyboard) {
        return bindingMap.keySet().stream()
            .filter(keyboard::stateMatches)
            .map(bindingMap::get)
            .findFirst();
    }

    @Override
    public void logBindings() {
        // Sort output by key combination display text
        bindingMap.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().toString()))
            .forEach(e -> Logger.info("%-20s: %s".formatted(e.getKey(), e.getValue().resourceBundleKey())));
    }

    private void registerBinding(ActionBinding binding) {
        requireNonNull(binding);
        for (KeyCodeCombination combination : binding.keyCombinations()) {
            bindingMap.put(combination, binding.gameAction());
        }
    }
}