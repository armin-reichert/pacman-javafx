/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.dashboard;

import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.GameAction;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

/**
 * Displays context-sensitive the keyboard shortcuts.
 */
public class DashboardSectionKeyShortcutsLocal extends DashboardSection {

    public DashboardSectionKeyShortcutsLocal(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void update(GameUI ui) {
        clearGrid();
        ui.access().gameScenes().optCurrentGameScene().ifPresent(gameScene -> addEntries(ui,gameScene));
    }

    private void addEntries(GameUI ui, GameScene gameScene) {
        final ActionBindingsSet actionBindings = gameScene.actionBindings();
        if (actionBindings.bindingMap().isEmpty()) {
            addRow(createLabel(NO_INFO, false));
        }
        else {
            actionBindings.bindingMap().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getDisplayText()))
                .forEach(entry -> {
                    final KeyCombination keyCombination = entry.getKey();
                    final GameAction action = entry.getValue();
                    final String localizedActionText = ui.access().translations().translate(action.resourceBundleKey());
                    addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled(ui)));
                });
        }
    }
}