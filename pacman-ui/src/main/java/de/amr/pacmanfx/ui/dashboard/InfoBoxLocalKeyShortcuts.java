/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameAction;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;
import java.util.Map;

import static de.amr.pacmanfx.ui.PacManGames.theUI;

/**
 * Displays context-sensitive the keyboard shortcuts.
 */
public class InfoBoxLocalKeyShortcuts extends InfoBox {

    @Override
    public void update() {
        clearGrid();
        theUI().currentGameScene().ifPresent(gameScene -> addEntries(gameScene.actionBindings()));
    }

    private void addEntries(Map<KeyCombination, GameAction> bindings) {
        bindings.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getDisplayText())).forEach(entry -> {
            KeyCombination keyCombination = entry.getKey();
            GameAction action = entry.getValue();
            addRow(keyCombination.getDisplayText(), createLabel(action.name()));
        });
    }
}