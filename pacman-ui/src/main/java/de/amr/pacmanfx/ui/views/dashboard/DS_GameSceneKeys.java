/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.PacManGamesEngine;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

public class DS_GameSceneKeys extends GameDashboardSection {

    public DS_GameSceneKeys() {
        super(DashboardID.KEYS_LOCAL);
    }

    @Override
    public void update(PacManGamesEngine app) {
        super.update(app);
        app.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> updateInfo(app, gameScene));
    }

    private void updateInfo(PacManGamesEngine app, GameScene gameScene) {
        clearSection();

        if (!(gameScene instanceof AbstractGameScene abstractGameScene)) {
            return;
        }
        if (abstractGameScene.actionBindingsRegistry().actionBindings().isEmpty()) {
            addRow(createLabel(NO_INFO, false));
        } else {
            abstractGameScene.actionBindingsRegistry().actionBindings().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getDisplayText()))
                .forEach(entry -> {
                    final KeyCombination keyCombination = entry.getKey();
                    final GameAction action = entry.getValue();
                    final String localizedActionText = app.translationManager().translate(action.resourceBundleKey());
                    addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled(app)));
                });
        }
    }
}