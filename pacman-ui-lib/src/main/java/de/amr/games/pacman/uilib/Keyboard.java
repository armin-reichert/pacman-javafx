/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCombination.*;

/**
 * @author Armin Reichert
 */
public class Keyboard {

    public static KeyCodeCombination naked(KeyCode code) {
        return new KeyCodeCombination(code);
    }

    public static KeyCodeCombination alt(KeyCode code) {
        return new KeyCodeCombination(code, ALT_DOWN);
    }

    public static KeyCodeCombination control(KeyCode code) {
        return new KeyCodeCombination(code, CONTROL_DOWN);
    }

    public static KeyCodeCombination shift(KeyCode code) {
        return new KeyCodeCombination(code, SHIFT_DOWN);
    }

    public static KeyCodeCombination shift_alt(KeyCode code) {
        return new KeyCodeCombination(code, SHIFT_DOWN, ALT_DOWN);
    }

    public static String format(KeyCodeCombination... combinations) {
        return Arrays.stream(combinations)
            .map(KeyCodeCombination::toString)
            .map(s -> "[" + s + "]")
            .collect(Collectors.joining(", "));
    }

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Map<KeyCodeCombination, ActionProvider> registeredBindings = new HashMap<>();
    private final List<KeyCodeCombination> matches = new ArrayList<>(3);

    public void bind(KeyCodeCombination combination, ActionProvider actionProvider) {
        if (registeredBindings.get(combination) == actionProvider) {
            Logger.debug("Key code combination '{}' already bound to {}", combination, actionProvider);
        } else {
            registeredBindings.put(combination, actionProvider);
            Logger.debug("Key code combination '{}' bound to {}", combination, actionProvider);
        }
    }

    public void unbind(KeyCodeCombination combination, ActionProvider client) {
        boolean removed = registeredBindings.remove(combination, client);
        if (removed) {
            Logger.debug("Key code combination '{}' bound to {}", combination, client);
        }
    }

    public void onKeyPressed(KeyEvent key) {
        Logger.trace("Key pressed: {}", key);
        pressedKeys.add(key.getCode());
        registeredBindings.keySet().stream().filter(combination -> combination.match(key)).forEach(matches::add);
    }

    public void onKeyReleased(KeyEvent key) {
        Logger.trace("Key released: {}", key);
        pressedKeys.remove(key.getCode());
        matches.clear();
    }

    public boolean isMatching(KeyCodeCombination combination) {
        return matches.contains(combination);
    }

    public boolean isPressed(KeyCode keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public void logCurrentBindings() {
        Logger.info("--------------------------");
        registeredBindings.keySet().stream()
            .sorted(Comparator.comparing(KeyCodeCombination::getDisplayText))
            .forEach(combination -> {
                ActionProvider actionProvider = registeredBindings.get(combination);
                Logger.info("{}: {}", combination, actionProvider.getClass().getSimpleName());
        });
        Logger.info("--------------------------");
    }
}