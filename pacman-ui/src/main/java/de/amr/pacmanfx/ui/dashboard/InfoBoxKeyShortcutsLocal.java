/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.ActionBindingManager;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameUI;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

/**
 * Displays context-sensitive the keyboard shortcuts.
 */
public class InfoBoxKeyShortcutsLocal extends InfoBox {

    public InfoBoxKeyShortcutsLocal(GameUI ui) {
        super(ui);
    }

    @Override
    public void update() {
        clearGrid();
        ui.currentGameScene().ifPresent(gameScene -> addEntries(gameScene.actionBindings()));
    }

    private void addEntries(ActionBindingManager actionBindingManager) {
        if (actionBindingManager.isEmpty()) {
            addRow(createLabel(NO_INFO, false));
        }
        else {
            actionBindingManager.actionByCombination().entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getDisplayText()))
                    .forEach(entry -> {
                KeyCombination keyCombination = entry.getKey();
                GameAction action = entry.getValue();
                String localizedActionText = ui.theAssets().text(action.name());
                addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled(ui)));
            });
        }
    }
}