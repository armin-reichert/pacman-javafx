/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import org.tinylog.Logger;

import java.util.*;

/**
 * @author Armin Reichert
 */
public class Keyboard {

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Set<KeyCodeCombination> registeredCombinations = new HashSet<>();
    private final List<KeyCodeCombination> matchingCombinations = new ArrayList<>(3);

    public void register(KeyInput input) {
        registeredCombinations.addAll(Arrays.asList(input.getCombinations()));
    }

    public void onKeyPressed(KeyEvent e) {
        Logger.debug("Key pressed: {}", e.getCode());
        registeredCombinations.stream().filter(kcc -> kcc.match(e)).forEach(this::addMatch);
        pressedKeys.add(e.getCode());
    }

    private void addMatch(KeyCodeCombination kcc) {
        matchingCombinations.add(kcc);
        Logger.debug("Added matching combination {}", kcc);
    }

    public void onKeyReleased(KeyEvent e) {
        Logger.debug("Key released: {}", e.getCode());
        matchingCombinations.clear();
        pressedKeys.remove(e.getCode());
    }

    /**
     * @param keyInput key input
     * @return tells if any of the registered given key combinations is matched by the current keyboard state
     */
    public boolean isRegisteredKeyPressed(KeyInput keyInput) {
        return Arrays.stream(keyInput.getCombinations()).anyMatch(matchingCombinations::contains);
    }

    public boolean pressed(KeyCode keyCode) {
        //return pressed(KeyInput.of(key(keyCode)));
        return pressedKeys.contains(keyCode);
    }
}