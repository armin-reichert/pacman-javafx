/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import de.amr.pacmanfx.ui.api.GameUI;
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

    private void addEntries(ActionBindingsManager actionBindingsManager) {
        if (actionBindingsManager.hasNoEntries()) {
            addRow(createLabel(NO_INFO, false));
        }
        else {
            actionBindingsManager.actionByKeyCombination().entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getDisplayText()))
                    .forEach(entry -> {
                KeyCombination keyCombination = entry.getKey();
                GameAction action = entry.getValue();
                String localizedActionText = ui.globalAssets().translated(action.name());
                addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled(ui)));
            });
        }
    }
}