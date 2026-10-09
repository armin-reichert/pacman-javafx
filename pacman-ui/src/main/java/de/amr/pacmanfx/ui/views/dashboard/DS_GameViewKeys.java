/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.engine.runtime.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.GameActionContext;
import de.amr.pacmanfx.ui.views.GameView;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;
import java.util.Map;

public class DS_GameViewKeys extends GameDashboardSection {

    public DS_GameViewKeys() {
        super(DashboardID.KEYS_GLOBAL);
    }

    @Override
    public void update(GameActionContext context) {
        super.update(context);
        context.ui().viewManager().optCurrentView().ifPresent(view -> updateInfo(context, view));
    }

    private void updateInfo(GameActionContext context, GameView view) {
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
                    final String actionText = context.translationManager().translate(action.resourceBundleKey());
                    final Label label = createLabel(actionText, action.isEnabled(context));
                    addRow(key.getDisplayText(), label);
                });
        }
    }
}
