/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.GameScene;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import java.util.stream.Stream;

public interface PacManGames_UIConfig {
    Image appIcon();
    String assetNamespace();
    ActorAnimationMap createPacAnimations(Pac pac);
    ActorAnimationMap createGhostAnimations(Ghost ghost);
    Node createLivesCounter3D();
    RectArea createLivesCounterSprite();
    PacBase3D createPac3D(Pac pac);
    Image createGhostNumberImage(int ghostIndex);
    RectArea createBonusSymbolSprite(byte symbol);
    RectArea createBonusValueSprite(byte symbol);
    GameScene2D createPiPScene(Canvas canvas);
    GameRenderer createRenderer(Canvas canvas);
    Stream<GameScene> gameScenes();
    boolean gameSceneHasID(GameScene gameScene, String sceneID);
    default boolean isGameCanvasDecorated() { return true; }
    GameScene selectGameScene(GameModel game, GameState gameState);
    SpriteSheet spriteSheet();
    WorldMapColorScheme worldMapColorScheme(WorldMap worldMap);
}