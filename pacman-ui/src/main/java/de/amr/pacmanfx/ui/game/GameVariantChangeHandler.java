/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.fsm.State;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.views.playview.MiniPlaySceneView;

import static java.util.Objects.requireNonNull;

public class GameVariantChangeHandler extends DefaultGameEventListener {

    private final Game game;

    public GameVariantChangeHandler(Game game) {
        this.game = requireNonNull(game);
        game.gameVariantNameProperty().addListener((_, oldGameVariantName, newGameVariantName) -> {
            if (oldGameVariantName != null) {
                exitGameVariant(oldGameVariantName);
            }
            if (newGameVariantName != null) {
                enterGameVariant(newGameVariantName);
            }
        });
    }

    private void exitGameVariant(String variantName) {
        game.ui().sounds().dispose();
        game.gameVariant(variantName).uiConfig().dispose();
        game.currentGameContext().flow().removeGameEventListener(this);
    }

    public void enterGameVariant(String variantName) {
        final UIConfig uiConfig = game.gameVariant(variantName).uiConfig();
        uiConfig.init(game);
        game.currentGameContext().flow().addGameEventListener(this);
    }

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        switch (gameEvent) {

            case LevelCreatedEvent levelCreatedEvent -> {
                final GameLevel level = levelCreatedEvent.level();
                final UIConfig currentConfig = game.currentUIConfig();
                final SpriteAnimationContainer spriteAnimationContainer = game.ui().sprites().animations();

                //TODO this should be done elsewhere
                level.entities().pac().setAnimations(currentConfig.createPacAnimations(spriteAnimationContainer));
                level.entities().ghosts().forEach(ghost ->
                    ghost.setAnimations(currentConfig.createGhostAnimations(spriteAnimationContainer, ghost.personality())));

                final MiniPlaySceneView miniPlayView = game.ui().views().gamePlayView().miniPlaySceneView();
                miniPlayView.setUIConfig(currentConfig);
                miniPlayView.setWorldSizeInPixel(level.worldMap().terrainLayer().sizeInPixel());
                miniPlayView.slideIn();

                // size of game scene might have changed, so re-embed
                game.ui().gameScenes().optCurrentGameScene().ifPresent(
                    gameScene -> game.ui().gameScenes().embedGameSceneIntoPlayView(gameScene));
            }

            case GameStateChangeEvent stateChangeEvent -> {
                final State<GameContext> gameState = stateChangeEvent.newState();
                if (GameStateID.GAME_LEVEL_COMPLETE.identifies(gameState)) {
                    final MiniPlaySceneView miniPlayView = game.ui().views().gamePlayView().miniPlaySceneView();
                    miniPlayView.slideOut();
                }
            }

            case GenericChangeEvent _ -> game.ui().gameScenes().forceGameSceneUpdate();

            default -> {}
        }

        game.ui().gameScenes().updateGameSceneAndForceReload(false);

        // Call game event handler for current game scene
        game.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.gameEventHandler().onGameEvent(gameEvent));
    }

}
