/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.ActionBindingsSupport;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

public class DS_GameSceneKeys extends GameDashboardSection {

    public DS_GameSceneKeys() {
        super(DashboardID.KEYS_LOCAL);
    }

    @Override
    public void update(GameAppContext app) {
        super.update(app);
        app.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> updateInfo(app, gameScene));
    }

    private void updateInfo(GameAppContext app, GameScene gameScene) {
        clearSection();
        if (gameScene.components().hasComp(ActionBindingsSupport.class)) {
            final ActionBindingsRegistry registry = gameScene
                .components()
                .reqComp(ActionBindingsSupport.class)
                .bindingsMap();

            if (registry.actionBindings().isEmpty()) {
                addRow(createLabel(NO_INFO, false));
            } else {
                registry.actionBindings().entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getDisplayText()))
                    .forEach(entry -> {
                        final KeyCombination keyCombination = entry.getKey();
                        final GameAction action = entry.getValue();
                        final String localizedActionText = app.ui().translations().translate(action.resourceBundleKey());
                        addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled(app)));
                    });
            }
        }
    }
}