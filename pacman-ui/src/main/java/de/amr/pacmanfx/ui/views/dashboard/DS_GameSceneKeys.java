/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.engine.runtime.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.GameActionContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

public class DS_GameSceneKeys extends GameDashboardSection {

    public DS_GameSceneKeys() {
        super(DashboardID.KEYS_LOCAL);
    }

    @Override
    public void update(GameActionContext context) {
        super.update(context);
        context.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> updateInfo(context, gameScene));
    }

    private void updateInfo(GameActionContext context, GameScene gameScene) {
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
                    final String localizedActionText = context.translationManager()
                        .translate(action.resourceBundleKey());
                    addRow(keyCombination.getDisplayText(),
                        createLabel(localizedActionText, action.isEnabled(context)));
                });
        }
    }
}