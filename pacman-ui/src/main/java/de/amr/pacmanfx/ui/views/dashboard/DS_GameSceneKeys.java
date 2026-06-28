/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import javafx.scene.input.KeyCombination;

import java.util.Comparator;

public class DS_GameSceneKeys extends GameDashboardSection {

    public DS_GameSceneKeys() {}

    @Override
    public void update(Game game) {
        super.update(game);
        game.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> updateInfo(game, gameScene));
    }

    private void updateInfo(Game game, AbstractGameScene gameScene) {
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
                    final String localizedActionText = game.ui().translations().translate(action.resourceBundleKey());
                    addRow(keyCombination.getDisplayText(), createLabel(localizedActionText, action.isEnabled()));
                });
        }
    }
}