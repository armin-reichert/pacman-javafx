package de.amr.games.pacman.ui2d.scene.pacman_xxl;

import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.BootScene;
import de.amr.games.pacman.ui2d.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameStartScene;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGameCutScene1;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGameCutScene2;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGameCutScene3;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGameIntroScene;
import de.amr.games.pacman.ui2d.util.AssetStorage;

public class PacManGameXXLSceneConfiguration extends GameSceneConfiguration {

    public PacManGameXXLSceneConfiguration() {
        set(GameSceneID.BOOT_SCENE,   new BootScene());
        set(GameSceneID.INTRO_SCENE,  new PacManGameIntroScene());
        set(GameSceneID.START_SCENE,  new MsPacManGameStartScene());
        set(GameSceneID.PLAY_SCENE,   new PlayScene2D());
        set(GameSceneID.CUT_SCENE_1,  new PacManGameCutScene1());
        set(GameSceneID.CUT_SCENE_2,  new PacManGameCutScene2());
        set(GameSceneID.CUT_SCENE_3,  new PacManGameCutScene3());
    }

    @Override
    public GameWorldRenderer createRenderer(AssetStorage assets) {
        return new PacManXXLGameRenderer(assets);
    }
}
