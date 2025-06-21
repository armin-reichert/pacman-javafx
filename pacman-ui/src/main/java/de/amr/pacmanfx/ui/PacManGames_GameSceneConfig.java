/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.model.GameModel;

import java.util.stream.Stream;

public interface PacManGames_GameSceneConfig {
    void createGameScenes();
    Stream<GameScene> gameScenes();
    boolean gameSceneHasID(GameScene gameScene, String sceneID);
    GameScene selectGameScene(GameModel game, GameState gameState);
}