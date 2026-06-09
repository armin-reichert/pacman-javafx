/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCodeCombination;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class GameActionBindingsMap implements ActionBindingsRegistry {

    private final String name;
    private final Map<KeyCodeCombination, GameAction> actionBindingsMap = new HashMap<>();

    public GameActionBindingsMap(String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        final String entriesText = actionBindingsMap.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().toString()))
            .map(this::formatEntry)
            .collect(Collectors.joining("\n"));
        return name + "\n" + entriesText;
    }

    private String formatEntry(Map.Entry<KeyCodeCombination, GameAction> e) {
        return "%-20s: %s".formatted(e.getKey(), e.getValue().resourceBundleKey());
    }

    @Override
    public void dispose() {
        actionBindingsMap.clear();
        Logger.info("Game action bindings map disposed: {}", this);
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindingsMap;
    }

    @Override
    public void bindActionToKeyCombination(GameAction action, KeyCodeCombination combination) {
        requireNonNull(action);
        requireNonNull(combination);
        actionBindingsMap.put(combination, action);
    }

    @Override
    public void selectAnyMatchingBinding(GameAction action, Set<ActionKeyBinding> bindingSet) {
        requireNonNull(action);
        requireNonNull(bindingSet);

        final Set<ActionKeyBinding> matchingBindings = bindingSet.stream()
            .filter(binding -> binding.action() == action)
            .collect(Collectors.toSet());

        final int count = matchingBindings.size();
        if (count == 0) {
            return;
        }
        if (count > 1) {
            Logger.warn("Found {} bindings for action {}, selecting arbitrary one", count, action);
        }
        registerBinding(matchingBindings.iterator().next());
    }

    @Override
    public void registerAllBindings(Set<ActionKeyBinding> bindingSet) {
        requireNonNull(bindingSet);
        for (ActionKeyBinding binding : bindingSet) {
            registerBinding(binding);
        }
    }

    @Override
    public Optional<GameAction> triggeredAction(Keyboard keyboard) {
        return actionBindingsMap.keySet().stream()
            .filter(keyboard::stateMatches)
            .map(actionBindingsMap::get)
            .findFirst();
    }

    private void registerBinding(ActionKeyBinding binding) {
        requireNonNull(binding);
        for (KeyCodeCombination combination : binding.keyCombinations()) {
            actionBindingsMap.put(combination, binding.action());
        }
    }
}