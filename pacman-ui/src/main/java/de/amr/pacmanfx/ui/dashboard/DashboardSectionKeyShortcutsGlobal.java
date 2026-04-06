/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.layout.View;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;
import java.util.Map;

public class DashboardSectionKeyShortcutsGlobal extends DashboardSection {

    public DashboardSectionKeyShortcutsGlobal(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void update(GameUI ui) {
        clearGrid();
        addRows(ui, ui.views().currentView());
    }

    private void addRows(GameUI ui, View view) {
        final Map<KeyCombination, GameAction> bindingMap = view.actionBindings().keyCombinationToActionMap();
        if (bindingMap.isEmpty()) {
            addRow(createLabel(NO_INFO, false));
        }
        else {
            bindingMap.keySet().stream()
                .sorted(Comparator.comparing(KeyCombination::getDisplayText))
                .forEach(key -> {
                    final GameAction action = bindingMap.get(key);
                    final String actionText = ui.translate(action.name());
                    final Label label = createLabel(actionText, action.isEnabled(ui));
                    addRow(key.getDisplayText(), label);
                });
        }
    }
}
