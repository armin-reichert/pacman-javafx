/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;

import static java.util.Objects.requireNonNull;

public class GameEventHandler extends DefaultGameEventListener {

    private final GameUI ui;

    public GameEventHandler(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        switch (gameEvent) {

            case LevelCreatedEvent levelCreatedEvent -> {
                final GameLevel level = levelCreatedEvent.level();
                final UIConfig uiConfig = ui.currentConfig();

                //TODO this should be done elsewhere
                level.entities().pac().setAnimationManager(uiConfig.createPacAnimations(ui.spriteAnimationSet()));
                level.entities().ghosts().forEach(ghost ->
                    ghost.setAnimationManager(uiConfig.createGhostAnimations(ui.spriteAnimationSet(), ghost.personality())));

                final MiniGameView miniGameView = ui.viewManager().playView().miniView();
                miniGameView.setGameLevel(level);
                miniGameView.slideIn();

                // size of game scene might have changed, so re-embed
                ui.gameSceneManager().optCurrentGameScene().ifPresent(ui.gameSceneEmbeddingManager()::embedGameSceneIntoPlayView);
            }

            case GameStateChangeEvent stateChangeEvent -> {
                if (stateChangeEvent.newState().matchesByName(CanonicalGameState.LEVEL_COMPLETE.name())) {
                    final MiniGameView miniGameView = ui.viewManager().playView().miniView();
                    miniGameView.slideOut();
                }
            }

            case GenericChangeEvent _ -> ui.gameSceneManager().forceGameSceneUpdate();

            default -> {}
        }

        ui.gameSceneManager().updateGameSceneAndForceReload(false);

        // Call game event handler for current game scene
        ui.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.gameEventHandler().onGameEvent(gameEvent));
    }
}
