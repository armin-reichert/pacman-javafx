/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public interface PacManGames_GameSceneConfig {
    void createGameScenes();
    GameScene2D createPiPScene(Canvas canvas);
    Stream<GameScene> gameScenes();
    boolean gameSceneHasID(GameScene gameScene, String sceneID);
    GameScene selectGameScene(GameModel game, GameState gameState);
}