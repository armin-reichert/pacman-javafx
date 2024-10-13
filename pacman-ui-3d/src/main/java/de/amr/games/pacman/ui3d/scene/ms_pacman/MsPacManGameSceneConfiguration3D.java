package de.amr.games.pacman.ui3d.scene.ms_pacman;

import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameSceneConfiguration;
import de.amr.games.pacman.ui3d.PlayScene3D;

public class MsPacManGameSceneConfiguration3D extends MsPacManGameSceneConfiguration {

    public MsPacManGameSceneConfiguration3D() {
        set(GameSceneID.PLAY_SCENE_3D, new PlayScene3D());
    }
}
