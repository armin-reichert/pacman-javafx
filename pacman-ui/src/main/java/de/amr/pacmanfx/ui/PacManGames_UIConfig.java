/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.uilib.GameScene;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.XMan3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import java.util.stream.Stream;

public interface PacManGames_UIConfig {
    Image appIcon();
    String assetNamespace();
    void createActorAnimations(GameLevel level);
    Node createLivesCounter3D();
    XMan3D createPac3D(Pac pac);
    GameScene2D createPiPScene(Canvas canvas);
    GameRenderer createRenderer(Canvas canvas);
    Stream<GameScene> gameScenes();
    boolean gameSceneHasID(GameScene gameScene, String sceneID);
    default boolean isGameCanvasDecorated() { return true; }
    GameScene selectGameScene(GameModel game, GameState gameState);
    <T extends GameSpriteSheet> T spriteSheet();
    WorldMapColorScheme worldMapColorScheme(WorldMap worldMap);
}