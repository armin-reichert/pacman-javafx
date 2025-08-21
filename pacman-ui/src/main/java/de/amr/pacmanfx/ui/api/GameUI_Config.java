/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.assets.WorldMapColorScheme;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.rendering.ActorSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import java.util.stream.Stream;

public interface GameUI_Config extends Disposable {

    String SCENE_ID_BOOT_SCENE_2D    = "BootScene2D";
    String SCENE_ID_INTRO_SCENE_2D   = "IntroScene2D";
    String SCENE_ID_START_SCENE_2D   = "StartScene2D";
    String SCENE_ID_CREDITS_SCENE_2D = "CreditsScene2D";
    String SCENE_ID_PLAY_SCENE_2D    = "PlayScene2D";
    String SCENE_ID_CUT_SCENE_N_2D   = "CutScene_%d_2D";
    String SCENE_ID_PLAY_SCENE_3D    = "PlayScene3D";

    // scene config
    void createGameScenes();
    Stream<GameScene> gameScenes();
    boolean gameSceneHasID(GameScene gameScene, String sceneID);
    GameScene selectGameScene(GameContext gameContext);

    GameUI theUI();

    default GlobalAssets globalAssets() {
        return theUI().assets();
    }

    AssetStorage assets();

    void loadAssets();

    Image bonusSymbolImage(byte symbol);

    Image bonusValueImage(byte symbol);

    WorldMapColorScheme colorScheme(WorldMap worldMap);

    GameLevelRenderer createGameLevelRenderer(Canvas canvas);

    HUDRenderer createHUDRenderer(Canvas canvas);

    ActorSpriteRenderer createActorSpriteRenderer(Canvas canvas);

    AnimationManager createGhostAnimations(Ghost ghost);

    Node createLivesCounterShape3D();

    AnimationManager createPacAnimations(Pac pac);

    PacBase3D createPac3D(AnimationRegistry animationMgr, GameLevel gameLevel, Pac pac);

    default boolean hasGameCanvasRoundedBorder() { return true; }

    Image killedGhostPointsImage(Ghost ghost, int killedIndex);

    SpriteSheet<?> spriteSheet();

    SoundManager soundManager();
}