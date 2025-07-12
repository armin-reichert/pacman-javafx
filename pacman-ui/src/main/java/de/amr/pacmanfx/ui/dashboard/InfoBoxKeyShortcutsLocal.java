/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.ui.ActionBindingMap;
import de.amr.pacmanfx.ui.GameAction;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

import static de.amr.pacmanfx.ui.GameUI.theUI;

/**
 * Displays context-sensitive the keyboard shortcuts.
 */
public class InfoBoxKeyShortcutsLocal extends InfoBox {

    public InfoBoxKeyShortcutsLocal(GameContext gameContext) {
        super(gameContext);
    }

    @Override
    public void update() {
        clearGrid();
        theUI().currentGameScene().ifPresent(gameScene -> addEntries(gameScene.actionBindings()));
    }

    private void addEntries(ActionBindingMap actionBindingMap) {
        if (actionBindingMap.isEmpty()) {
            addRow(createLabel(InfoText.NO_INFO, false));
        }
        else {
            actionBindingMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getDisplayText())).forEach(entry -> {
                KeyCombination keyCombination = entry.getKey();
                GameAction action = entry.getValue();
                String localizedActionText = theUI().theAssets().text(action.name());
                addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled(theUI(), gameContext)));
            });
        }
    }
}