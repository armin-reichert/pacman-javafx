/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import javafx.scene.input.KeyCode;

import java.util.Collections;
import java.util.Map;

/**
 * @author Armin Reichert
 */
public class Settings {

    private static final Map<Direction, KeyCode> KEYS_NUMPAD = Map.of(
        Direction.UP, KeyCode.NUMPAD8,
        Direction.DOWN, KeyCode.NUMPAD5,
        Direction.LEFT, KeyCode.NUMPAD4,
        Direction.RIGHT, KeyCode.NUMPAD6);

    private static final Map<Direction, KeyCode> KEYS_CURSOR = Map.of(
        Direction.UP, KeyCode.UP,
        Direction.DOWN, KeyCode.DOWN,
        Direction.LEFT, KeyCode.LEFT,
        Direction.RIGHT, KeyCode.RIGHT);

    private static Map<Direction, KeyCode> keyMap(String name) {
        return switch (name) {
            case "numpad" -> KEYS_NUMPAD;
            case "cursor" -> KEYS_CURSOR;
            default -> throw new IllegalArgumentException("Unknown keymap name: " + name);
        };
    }

    public boolean fullScreen;
    public GameVariant variant;
    public float zoom;
    public Map<Direction, KeyCode> keyMap;

    public Settings() {
        this(Collections.emptyMap());
    }

    public Settings(Map<String, String> map) {
        fullScreen = false;
        variant = GameVariant.PACMAN;
        zoom = 2;
        keyMap = keyMap("cursor");
        merge(map);
    }

    public void merge(Map<String, String> map) {
        if (map.containsKey("fullScreen")) {
            fullScreen = Boolean.parseBoolean(map.get("fullScreen"));
        }
        if (map.containsKey("variant")) {
            variant = GameVariant.valueOf(map.get("variant"));
        }
        if (map.containsKey("zoom")) {
            zoom = Float.parseFloat(map.get("zoom"));
        }
        if (map.containsKey("keys")) {
            keyMap = keyMap(map.get("keys"));
        }
    }

    @Override
    public String toString() {
        return "Settings [fullScreen=" + fullScreen + ", variant=" + variant + ", zoom=" + zoom + "]";
    }
}