/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.subviews.dashboard;

import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.action.GameAction;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

public class DashboardSectionKeyboardShortcutsCurrentGameScene extends DashboardSection {

    public DashboardSectionKeyboardShortcutsCurrentGameScene(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void connect(AppContext ui) {
        updateTableForCurrentGameScene(ui);
    }

    @Override
    public void update() {
        super.update();
        if (dashboard.context() != null) {
            updateTableForCurrentGameScene(dashboard.context());
        }
    }

    private void updateTableForCurrentGameScene(AppContext ui) {
        clearSection();
        final GameScene currentGameScene = ui.ui().gameScenes().optCurrentGameScene().orElse(null);
        if (currentGameScene != null) {
            final var currentBindingsMap = currentGameScene.actionBindings().bindingMap();
            if (currentBindingsMap.isEmpty()) {
                addRow(createLabel(NO_INFO, false));
            } else {
                currentBindingsMap.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getDisplayText()))
                    .forEach(entry -> {
                        final KeyCombination keyCombination = entry.getKey();
                        final GameAction action = entry.getValue();
                        final String localizedActionText = ui.ui().translations().translate(action.resourceBundleKey());
                        addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled(ui)));
                    });
            }
        }
    }
}