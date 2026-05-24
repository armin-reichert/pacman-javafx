/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.pacmanfx.event.DefaultGameEventListener;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.event.LevelCreatedEvent;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;

import static java.util.Objects.requireNonNull;

class PlayViewGameEventHandler extends DefaultGameEventListener {

    private final PlayView playView;

    public PlayViewGameEventHandler(PlayView playView) {
        this.playView = requireNonNull(playView);
    }

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        final GameUI ui = playView.ui();
        switch (gameEvent) {

            case LevelCreatedEvent levelCreatedEvent -> {
                final GameLevel level = levelCreatedEvent.level();
                final UIConfig uiConfig = ui.currentConfig();

                //TODO this should be done elsewhere
                level.pac().setAnimationManager(uiConfig.createPacAnimations(ui.spriteAnimationSet()));
                level.ghosts().forEach(ghost ->
                    ghost.setAnimationManager(uiConfig.createGhostAnimations(ui.spriteAnimationSet(), ghost.personality())));

                playView.miniView().setGameLevel(level);
                playView.miniView().slideIn();
                // size of game scene might have changed, so re-embed
                playView.optCurrentGameScene().ifPresent(gameScene -> playView.embedGameScene(playView.parentScene(), gameScene));
            }

            case GameStateChangeEvent stateChangeEvent -> {
                if (stateChangeEvent.newState().matchesByName(CanonicalGameState.LEVEL_COMPLETE.name())) {
                    playView.miniView().slideOut();
                }
            }

            default -> {
            }
        }

        playView.updateGameScene();

        // Call game event handler for current game scene
        playView.optCurrentGameScene().ifPresent(gameScene -> gameScene.gameEventHandler().onGameEvent(gameEvent));
    }
}
