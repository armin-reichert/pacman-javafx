package de.amr.games.pacman.ui3d.scene.pacman;

import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGameSceneConfiguration;
import de.amr.games.pacman.ui3d.PlayScene3D;

public class PacManGameSceneConfiguration3D extends PacManGameSceneConfiguration {

    public PacManGameSceneConfiguration3D() {
        set(GameSceneID.PLAY_SCENE_3D, new PlayScene3D());
    }
}
