package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.*;
import de.amr.games.pacman.ui2d.scene.ms_pacman.StartScene;
import de.amr.games.pacman.ui2d.util.AssetStorage;

public class PacManGameSceneConfiguration extends GameSceneConfiguration {

    private final GameSpriteSheet spriteSheet;
    private final GameRenderer renderer;

    public PacManGameSceneConfiguration(AssetStorage assets) {
        set(GameSceneID.BOOT_SCENE,  new BootScene());
        set(GameSceneID.INTRO_SCENE, new IntroScene());
        set(GameSceneID.START_SCENE, new StartScene());
        set(GameSceneID.PLAY_SCENE,  new PlayScene2D());
        set(GameSceneID.CUT_SCENE_1, new CutScene1());
        set(GameSceneID.CUT_SCENE_2, new CutScene2());
        set(GameSceneID.CUT_SCENE_3, new CutScene3());

        spriteSheet = assets.get(GameAssets2D.assetPrefix(GameVariant.PACMAN) + ".spritesheet");
        renderer = new GameRenderer(assets);
    }

    @Override
    public GameRenderer renderer() {
        return renderer;
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        GameSceneID sceneID = switch (context.gameState()) {
            case BOOT               -> GameSceneID.BOOT_SCENE;
            case STARTING           -> GameSceneID.START_SCENE;
            case INTRO              -> GameSceneID.INTRO_SCENE;
            case INTERMISSION       -> cutSceneID(context.game().intermissionNumber(context.game().levelNumber()));
            case TESTING_CUT_SCENES -> cutSceneID(context.gameState().<Integer>getProperty("intermissionTestNumber"));
            default                 -> GameSceneID.PLAY_SCENE;
        };
        return get(sceneID);
    }

    @Override
    public void createActorAnimations(GameModel game) {
        game.pac().setAnimations(new PacAnimations(spriteSheet));
        game.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }
}