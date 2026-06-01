/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.dashboard;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.subviews.GameUI_SubView;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;
import java.util.Map;

public class DashboardSectionKeyShortcutsGlobal extends DashboardSection {

    public DashboardSectionKeyShortcutsGlobal(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void connect(GameUI ui) {
        updateTableForCurrentSubView(ui);
    }

    @Override
    public void update() {
        super.update();
        if (dashboard.ui() != null) {
            updateTableForCurrentSubView(dashboard.ui());
        }
    }

    private void updateTableForCurrentSubView(GameUI ui) {
        clearSection();
        final GameUI_SubView currentSubView = ui.access().subViews().currentView();
        if (currentSubView == null) {
            return;
        }

        final Map<KeyCodeCombination, GameAction> currentBindingMap = currentSubView.actionBindings().bindingMap();
        if (currentBindingMap.isEmpty()) {
            addRow(createLabel(NO_INFO, false));
        }
        else {
            currentBindingMap.keySet().stream()
                .sorted(Comparator.comparing(KeyCombination::getDisplayText))
                .forEach(key -> {
                    final GameAction action = currentBindingMap.get(key);
                    final String actionText = ui.access().translations().translate(action.resourceBundleKey());
                    final Label label = createLabel(actionText, action.isEnabled(ui));
                    addRow(key.getDisplayText(), label);
                });
        }
    }
}
