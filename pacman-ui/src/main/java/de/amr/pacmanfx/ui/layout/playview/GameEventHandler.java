/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.layout.playview;

import de.amr.basics.spriteanim.SpriteAnimationSet;
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
                final UIConfig currentConfig = ui.services().getUIConfig(ui.gameContext().gameVariantName());
                final SpriteAnimationSet spriteAnimationSet = ui.services().sprites().animationSet();

                //TODO this should be done elsewhere
                level.entities().pac().setAnimations(currentConfig.createPacAnimations(spriteAnimationSet));
                level.entities().ghosts().forEach(ghost ->
                    ghost.setAnimations(currentConfig.createGhostAnimations(spriteAnimationSet, ghost.personality())));

                final MiniPlaySceneView miniPlayView = ui.services().views().playView().miniPlaySceneView();
                miniPlayView.setUIConfig(currentConfig);
                miniPlayView.setWorldSizeInPixel(level.worldMap().terrainLayer().sizeInPixel());
                miniPlayView.slideIn();

                // size of game scene might have changed, so re-embed
                ui.services().gameScenes().optCurrentGameScene().ifPresent(
                    gameScene -> ui.services().gameScenes().embedGameSceneIntoPlayView(ui, gameScene));
            }

            case GameStateChangeEvent stateChangeEvent -> {
                if (stateChangeEvent.newState().matchesByName(CanonicalGameState.LEVEL_COMPLETE.name())) {
                    final MiniPlaySceneView miniPlayView = ui.services().views().playView().miniPlaySceneView();
                    miniPlayView.slideOut();
                }
            }

            case GenericChangeEvent _ -> ui.services().gameScenes().forceGameSceneUpdate(ui);

            default -> {}
        }

        ui.services().gameScenes().updateGameSceneAndForceReload(ui, false);

        // Call game event handler for current game scene
        ui.services().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.gameEventHandler().onGameEvent(gameEvent));
    }
}
