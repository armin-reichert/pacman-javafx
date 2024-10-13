package de.amr.games.pacman.ui3d.scene.tengen;

import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.tengen.TengenMsPacManGameSceneConfiguration;
import de.amr.games.pacman.ui3d.PlayScene3D;

public class TengenMsPacManGameSceneConfiguration3D extends TengenMsPacManGameSceneConfiguration {

    public TengenMsPacManGameSceneConfiguration3D() {
        set(GameSceneID.PLAY_SCENE_3D, new PlayScene3D());
    }
}
