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

public class DS_GameViewKeys extends DashboardSection {

    public DS_GameViewKeys() {}

    @Override
    public void update(Game game) {
        super.update(game);
        game.ui().views().optCurrentView().ifPresent(view -> updateInfo(game, view));
    }

    private void updateInfo(Game game, GameView view) {
        clearSection();

        final Map<KeyCodeCombination, GameAction> currentBindingMap = view.actionBindings().actionBindings();
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
