/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.common.*;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.scene.canvas.Canvas;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

public class MsPacManGameSceneConfig implements GameSceneConfig {

    private final AssetStorage assets;
    private final MsPacManGameSpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public MsPacManGameSceneConfig(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        spriteSheet = assets.get(GameAssets2D.PFX_MS_PACMAN + ".spritesheet");
        set("BootScene",   new BootScene());
        set("IntroScene",  new IntroScene());
        set("StartScene",  new StartScene());
        set("PlayScene2D", new PlayScene2D());
        set("CutScene1",   new CutScene1());
        set("CutScene2",   new CutScene2());
        set("CutScene3",   new CutScene3());
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
    public WorldMapColoring worldMapColoring(WorldMap worldMap) {
        return MsPacManGameRenderer.WORLD_MAP_COLORINGS.get(worldMap.getConfigValue("colorMapIndex"));
    }

    @Override
    public MsPacManGameRenderer createRenderer(Canvas canvas) {
        return new MsPacManGameRenderer(assets, canvas);
    }

    @Override
    public MsPacManGameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + context.level().intermissionNumber();
            case TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default                 -> "PlayScene2D";
        };
        return get(sceneID);
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }
}