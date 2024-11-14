/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.input;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Armin Reichert
 */
public class Keyboard {

    public static KeyCodeCombination naked(KeyCode code) {
        return new KeyCodeCombination(code);
    }

    public static KeyCodeCombination alt(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.ALT_DOWN);
    }

    public static KeyCodeCombination shift(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
    }

    public static KeyCodeCombination shift_alt(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN);
    }

    public static KeyCodeCombination control(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.CONTROL_DOWN);
    }

    public String format(KeyCodeCombination... combinations) {
        return Arrays.stream(combinations)
            .map(KeyCodeCombination::toString)
            .map(s -> "[" + s + "]")
            .collect(Collectors.joining(", "));
    }

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Map<KeyCodeCombination, Object> registeredCombinations = new HashMap<>();
    private final List<KeyCodeCombination> matches = new ArrayList<>(3);

    public void register(KeyCodeCombination kcc, Object owner) {
        if (registeredCombinations.get(kcc) == owner) {
            Logger.info("Key code combination '{}' already registered: {}", kcc, owner);
        }
        registeredCombinations.put(kcc, owner);
        Logger.info("Key code combination '{}' registered: {}", kcc, owner);
    }

    public void unregister(KeyCodeCombination kcc, Object owner) {
        boolean removed = registeredCombinations.remove(kcc, owner);
        if (removed) {
            Logger.info("Key code combination '{}' removed: {}", kcc, owner);
        }
    }

    public void onKeyPressed(KeyEvent keyEvent) {
        Logger.debug("Key pressed: {}", keyEvent.getCode());
        pressedKeys.add(keyEvent.getCode());
        registeredCombinations.keySet().stream().filter(kcc -> kcc.match(keyEvent)).forEach(matches::add);
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        Logger.debug("Key released: {}", keyEvent.getCode());
        pressedKeys.remove(keyEvent.getCode());
        matches.clear();
    }

    public boolean isMatching(KeyCodeCombination kcc) {
        return matches.contains(kcc);
    }

    public boolean pressed(KeyCode keyCode) {
        return pressedKeys.contains(keyCode);
    }
}