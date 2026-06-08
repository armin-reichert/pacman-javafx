/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.dashboard;

import de.amr.pacmanfx.ui.app.Game;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.subviews.SubView;
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
    public void connect(Game context) {
        updateTableForCurrentSubView(context);
    }

    @Override
    public void update() {
        super.update();
        if (dashboard.appContext() != null) {
            updateTableForCurrentSubView(dashboard.appContext());
        }
    }

    private void updateTableForCurrentSubView(Game context) {
        clearSection();
        final SubView currentSubView = context.ui().subViews().currentView();
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
                    final String actionText = context.ui().translations().translate(action.resourceBundleKey());
                    final Label label = createLabel(actionText, action.isEnabled(context));
                    addRow(key.getDisplayText(), label);
                });
        }
    }
}
