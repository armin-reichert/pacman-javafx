package de.amr.games.pacman.ui2d.scene.tengen;

import de.amr.games.pacman.ui2d.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.PlayScene2D;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameCutScene1;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameCutScene2;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameCutScene3;

public class TengenMsPacManGameSceneConfiguration extends GameSceneConfiguration {

    public TengenMsPacManGameSceneConfiguration() {
        set(GameSceneID.BOOT_SCENE,   new TengenMsPacManGameBootScene());
        set(GameSceneID.INTRO_SCENE,  new TengenMsPacManGameIntroScene());
        set(GameSceneID.START_SCENE,  new TengenMsPacManGameStartScene());
        set(GameSceneID.PLAY_SCENE,   new PlayScene2D());
        set(GameSceneID.CUT_SCENE_1,  new MsPacManGameCutScene1());
        set(GameSceneID.CUT_SCENE_2,  new MsPacManGameCutScene2());
        set(GameSceneID.CUT_SCENE_3,  new MsPacManGameCutScene3());
    }
}
