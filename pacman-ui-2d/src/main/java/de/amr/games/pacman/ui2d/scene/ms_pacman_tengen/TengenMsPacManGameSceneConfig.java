/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.model.GameLevel;
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

public class TengenMsPacManGameSceneConfig implements GameSceneConfig {

    // 32x28 tiles or 32x30 tiles? Emulator seems to use 32x30?
    public static final int NES_TILES_X = 32;
    public static final int NES_TILES_Y = 30;

    public static final int NES_RESOLUTION_X = 256;
    public static final int NES_RESOLUTION_Y = 240; // see above

    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final TengenMsPacManGameSpriteSheet spriteSheet;
    private final TengenMsPacManGameRenderer renderer;

    public TengenMsPacManGameSceneConfig(AssetStorage assets) {
        set("BootScene",      new BootScene());
        set("IntroScene",     new IntroScene());
        set("StartScene",     new OptionsScene());
        set("ShowingCredits", new CreditsScene());
        set("PlayScene2D",    new TengenPlayScene2D());
        set("CutScene1",      new CutScene1());
        set("CutScene2",      new CutScene2());
        set("CutScene3",      new CutScene3());
        set("CutScene4",      new CutScene4());

        spriteSheet = assets.get(GameAssets2D.assetPrefix(GameVariant.MS_PACMAN_TENGEN) + ".spritesheet");
        renderer = new TengenMsPacManGameRenderer(assets);
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case BOOT               -> "BootScene";
            case WAITING_FOR_START  -> "StartScene";
            case SHOWING_CREDITS    -> "ShowingCredits";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + context.game().intermissionNumberAfterLevel();
            case TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default                 -> "PlayScene2D";
        };
        return get(sceneID);
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
    public void createActorAnimations(GameModel game) {
        GameLevel level = game.level().orElseThrow();
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }
}