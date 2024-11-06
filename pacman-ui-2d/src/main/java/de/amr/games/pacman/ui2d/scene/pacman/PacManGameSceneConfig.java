/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.ArcadeBootScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfig;
import de.amr.games.pacman.ui2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui2d.util.AssetStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class PacManGameSceneConfig implements GameSceneConfig {

    private final Map<String, GameScene> scenesByID = new HashMap<>();
    private final PacManGameSpriteSheet spriteSheet;
    private final PacManGameRenderer renderer;

    public PacManGameSceneConfig(AssetStorage assets) {
        set("BootScene",  new ArcadeBootScene());
        set("IntroScene", new IntroScene());
        set("StartScene", new StartScene());
        set("PlayScene2D", new PlayScene2D());
        set("CutScene1", new CutScene1());
        set("CutScene2", new CutScene2());
        set("CutScene3", new CutScene3());

        spriteSheet = assets.get(GameAssets2D.assetPrefix(GameVariant.PACMAN) + ".spritesheet");
        renderer = new PacManGameRenderer(assets);
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
    public PacManGameRenderer renderer() {
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