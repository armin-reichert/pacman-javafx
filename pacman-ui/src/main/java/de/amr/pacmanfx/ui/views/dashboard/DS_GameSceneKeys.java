/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

public class DS_GameSceneKeys extends GameDashboardSection {

    public DS_GameSceneKeys() {
        super(DashboardID.KEYS_LOCAL);
    }

    @Override
    public void update(GameActionContext actionContext) {
        super.update(actionContext);
        actionContext.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> updateInfo(actionContext, gameScene));
    }

    private void updateInfo(GameActionContext actionContext, GameScene gameScene) {
        clearSection();
        final var currentBindingsMap = gameScene.actionBindings().actionBindings();
        if (currentBindingsMap.isEmpty()) {
            addRow(createLabel(NO_INFO, false));
        } else {
            currentBindingsMap.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getDisplayText()))
                .forEach(entry -> {
                    final KeyCombination keyCombination = entry.getKey();
                    final GameAction action = entry.getValue();
                    final String localizedActionText = actionContext.ui().translations().translate(action.resourceBundleKey());
                    addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled()));
                });
        }
    }
}