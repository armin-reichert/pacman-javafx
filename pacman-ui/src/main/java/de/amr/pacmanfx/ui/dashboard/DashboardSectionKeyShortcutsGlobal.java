/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

public class DashboardSectionKeyShortcutsGlobal extends DashboardSection {

    public DashboardSectionKeyShortcutsGlobal(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void update(GameUI ui) {
        clearGrid();
        addEntries(ui);
    }

    private void addEntries(GameUI ui) {
        final ActionBindingsManager actionBindingsManager = ui.views().currentView().actionBindingsManager();
        if (actionBindingsManager.hasNoBindings()) {
            addRow(createLabel(NO_INFO, false));
        }
        else {
            actionBindingsManager.actionForKeyCombination().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getDisplayText()))
                .forEach(entry -> {
                    final KeyCombination keyCombination = entry.getKey();
                    final GameAction action = entry.getValue();
                    final String localizedActionText = ui.translate(action.name());
                    addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled(ui)));
                });
        }
    }
}
