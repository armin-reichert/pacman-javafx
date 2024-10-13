package de.amr.games.pacman.ui3d.scene.pacman_xxl;

import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.pacman_xxl.PacManGameXXLSceneConfiguration;
import de.amr.games.pacman.ui3d.PlayScene3D;

public class PacManGameXXLSceneConfiguration3D extends PacManGameXXLSceneConfiguration {

    public PacManGameXXLSceneConfiguration3D() {
        set(GameSceneID.PLAY_SCENE_3D, new PlayScene3D());
    }
}
