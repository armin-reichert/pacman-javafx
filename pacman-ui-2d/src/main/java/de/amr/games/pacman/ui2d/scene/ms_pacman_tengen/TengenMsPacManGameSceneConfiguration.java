/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfig;
import de.amr.games.pacman.ui2d.util.AssetStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TengenMsPacManGameSceneConfiguration implements GameSceneConfig {

    public static final double SCALING = 3.0;

    // 32x28 tiles
    public static final int NES_TILES_X = 32;
    public static final int NES_TILES_Y = 28;

    public static final int NES_RESOLUTION_X = 256;
    public static final int NES_RESOLUTION_Y = 224;
    public static final float NES_ASPECT = 1f * NES_RESOLUTION_X / NES_RESOLUTION_Y;

    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final TengenMsPacManGameSpriteSheet spriteSheet;
    private final TengenMsPacManGameRenderer renderer;

    public TengenMsPacManGameSceneConfiguration(AssetStorage assets) {
        set("BootScene",   new BootScene());
        set("IntroScene",  new IntroScene());
        set("StartScene",  new OptionsScene());
        set("PlayScene2D", new TengenPlayScene2D());
        set("CutScene1",   new CutScene1());
        set("CutScene2",   new CutScene2());
        set("CutScene3",   new CutScene3());

        spriteSheet = assets.get(GameAssets2D.assetPrefix(GameVariant.MS_PACMAN_TENGEN) + ".spritesheet");
        renderer = new TengenMsPacManGameRenderer(assets);
    }

    @Override
    public void set(String id, GameScene gameScene) {
        scenesByID.put(id, gameScene);
    }

    @Override
    public GameScene get(String id) {
        return scenesByID.get(id);
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public TengenMsPacManGameRenderer renderer() {
        return renderer;
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case BOOT               -> "BootScene";
            case WAITING_FOR_START -> "StartScene";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + context.game().intermissionNumberAfterLevel();
            case TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default                 -> "PlayScene2D";
        };
        return get(sceneID);
    }

    @Override
    public void createActorAnimations(GameModel game) {
        game.pac().setAnimations(new PacAnimations(spriteSheet));
        game.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }
}