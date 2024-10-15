/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.common.BootScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfiguration;
import de.amr.games.pacman.ui2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui2d.util.AssetStorage;

public class MsPacManGameSceneConfiguration extends GameSceneConfiguration {

    private final MsPacManGameSpriteSheet spriteSheet;
    private final GameRenderer renderer;

    public MsPacManGameSceneConfiguration(AssetStorage assets) {
        set("BootScene",  new BootScene());
        set("IntroScene", new IntroScene());
        set("StartScene", new StartScene());
        set("PlayScene2D", new PlayScene2D());
        set("CutScene1", new CutScene1());
        set("CutScene2", new CutScene2());
        set("CutScene3", new CutScene3());

        spriteSheet = assets.get(GameAssets2D.assetPrefix(GameVariant.MS_PACMAN) + ".spritesheet");
        renderer = new GameRenderer(assets);
    }

    @Override
    public GameRenderer renderer() {
        return renderer;
    }

    @Override
    public MsPacManGameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case BOOT               -> "BootScene";
            case STARTING           -> "StartScene";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> cutSceneID(context.game().intermissionNumber(context.game().levelNumber()));
            case TESTING_CUT_SCENES -> cutSceneID(context.gameState().<Integer>getProperty("intermissionTestNumber"));
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