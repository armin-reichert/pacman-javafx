package de.amr.games.pacman.ui2d.scene.tengen;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameCutScene1;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameCutScene2;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameCutScene3;
import de.amr.games.pacman.ui2d.util.AssetStorage;

public class TengenMsPacManGameSceneConfiguration extends GameSceneConfiguration {

    public TengenMsPacManGameSceneConfiguration() {
        set(GameSceneID.BOOT_SCENE,  new TengenMsPacManGameBootScene());
        set(GameSceneID.INTRO_SCENE, new TengenMsPacManGameIntroScene());
        set(GameSceneID.START_SCENE, new TengenMsPacManGameStartScene());
        set(GameSceneID.PLAY_SCENE,  new PlayScene2D());
        set(GameSceneID.CUT_SCENE_1, new MsPacManGameCutScene1());
        set(GameSceneID.CUT_SCENE_2, new MsPacManGameCutScene2());
        set(GameSceneID.CUT_SCENE_3, new MsPacManGameCutScene3());
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
    public GameWorldRenderer createRenderer(AssetStorage assets) {
        return new TengenMsPacManGameRenderer(assets);
    }

    @Override
    public void createActorAnimations(GameModel game, GameSpriteSheet spriteSheet) {
        game.pac().setAnimations(new TengenMsPacManGamePacAnimations(spriteSheet));
        game.ghosts().forEach(ghost -> ghost.setAnimations(new TengenMsPacManGameGhostAnimations(spriteSheet, ghost.id())));
    }
}