/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.dashboard;

import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.action.core.GameAction;
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
    public void connect(Game game) {
        updateTableForCurrentSubView(game);
    }

    @Override
    public void update() {
        super.update();
        if (dashboard.game() != null) {
            updateTableForCurrentSubView(dashboard.game());
        }
    }

    private void updateTableForCurrentSubView(Game game) {
        clearSection();
        final SubView currentSubView = game.ui().subViews().currentView();
        if (currentSubView == null) {
            return;
        }

        final Map<KeyCodeCombination, GameAction> currentBindingMap = currentSubView.actionBindings().actionBindings();
        if (currentBindingMap.isEmpty()) {
            addRow(createLabel(NO_INFO, false));
        }
        else {
            currentBindingMap.keySet().stream()
                .sorted(Comparator.comparing(KeyCombination::getDisplayText))
                .forEach(key -> {
                    final GameAction action = currentBindingMap.get(key);
                    final String actionText = game.ui().translations().translate(action.resourceBundleKey());
                    final Label label = createLabel(actionText, action.isEnabled());
                    addRow(key.getDisplayText(), label);
                });
        }
    }
}
