/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.views.GameView;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;
import java.util.Map;

public class DS_GlobaKeysl extends DashboardSection {

    public DS_GlobaKeysl(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void connect(Game game) {
        updateTableForCurrentSubView(game);
    }

    @Override
    public void update(Game game) {
        super.update(game);
        updateTableForCurrentSubView(game);
    }

    private void updateTableForCurrentSubView(Game game) {
        clearSection();
        final GameView currentGameView = game.ui().views().assertCurrentView();

        final Map<KeyCodeCombination, GameAction> currentBindingMap = currentGameView.actionBindings().actionBindings();
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
