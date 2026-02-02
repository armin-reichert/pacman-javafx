/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
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
        ui.optGameScene().ifPresent(gameScene -> addEntries(ui,gameScene));
    }

    private void addEntries(GameUI ui, GameScene gameScene) {
        final ActionBindingsManager actionBindings = gameScene.actionBindings();
        if (actionBindings.hasNoBindings()) {
            addRow(createLabel(NO_INFO, false));
        }
        else {
            actionBindings.actionForKeyCombination().entrySet().stream()
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