/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import javafx.scene.input.KeyCombination;

import java.util.Set;

public record ActionBinding(GameAction gameAction, Set<KeyCombination> keyCombinations) {

    public ActionBinding(GameAction action, KeyCombination... combinations) {
        this(action, Set.of(combinations));
    }
}
