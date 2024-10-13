package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.BootScene;
import de.amr.games.pacman.ui2d.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.util.AssetStorage;

public class MsPacManGameSceneConfiguration extends GameSceneConfiguration {

    public MsPacManGameSceneConfiguration() {
        set(GameSceneID.BOOT_SCENE,   new BootScene());
        set(GameSceneID.INTRO_SCENE,  new MsPacManGameIntroScene());
        set(GameSceneID.START_SCENE,  new MsPacManGameStartScene());
        set(GameSceneID.PLAY_SCENE,   new PlayScene2D());
        set(GameSceneID.CUT_SCENE_1,  new MsPacManGameCutScene1());
        set(GameSceneID.CUT_SCENE_2,  new MsPacManGameCutScene2());
        set(GameSceneID.CUT_SCENE_3,  new MsPacManGameCutScene3());
    }

    @Override
    public GameWorldRenderer createRenderer(AssetStorage assets) {
        return new MsPacManGameRenderer(assets);
    }

}
