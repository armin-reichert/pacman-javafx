/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.subviews.playview;

import de.amr.basics.fsm.State;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.config.UIConfig;

import static java.util.Objects.requireNonNull;

public class GameEventHandler extends DefaultGameEventListener {

    private final Game context;

    public GameEventHandler(Game context) {
        this.context = requireNonNull(context);
    }

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        switch (gameEvent) {

            case LevelCreatedEvent levelCreatedEvent -> {
                final GameLevel level = levelCreatedEvent.level();
                final UIConfig currentConfig = context.currentUIConfig();
                final SpriteAnimationSet spriteAnimationSet = context.ui().sprites().animationSet();

                //TODO this should be done elsewhere
                level.entities().pac().setAnimations(currentConfig.createPacAnimations(spriteAnimationSet));
                level.entities().ghosts().forEach(ghost ->
                    ghost.setAnimations(currentConfig.createGhostAnimations(spriteAnimationSet, ghost.personality())));

                final MiniPlaySceneView miniPlayView = context.ui().subViews().gamePlayView().miniPlaySceneView();
                miniPlayView.setUIConfig(currentConfig);
                miniPlayView.setWorldSizeInPixel(level.worldMap().terrainLayer().sizeInPixel());
                miniPlayView.slideIn();

                // size of game scene might have changed, so re-embed
                context.ui().gameScenes().optCurrentGameScene().ifPresent(
                    gameScene -> context.ui().gameScenes().embedGameSceneIntoPlayView(context, gameScene));
            }

            case GameStateChangeEvent stateChangeEvent -> {
                final State<GameContext> gameState = stateChangeEvent.newState();
                if (GameStateID.GAME_LEVEL_COMPLETE.identifies(gameState)) {
                    final MiniPlaySceneView miniPlayView = context.ui().subViews().gamePlayView().miniPlaySceneView();
                    miniPlayView.slideOut();
                }
            }

            case GenericChangeEvent _ -> context.ui().gameScenes().forceGameSceneUpdate(context);

            default -> {}
        }

        context.ui().gameScenes().updateGameSceneAndForceReload(context, false);

        // Call game event handler for current game scene
        context.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.gameEventHandler().onGameEvent(gameEvent));
    }
}
