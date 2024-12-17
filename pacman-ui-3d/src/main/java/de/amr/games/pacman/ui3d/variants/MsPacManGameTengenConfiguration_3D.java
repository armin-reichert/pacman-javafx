/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.variants;

import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengenConfiguration;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.scene.GameScene;
import de.amr.games.pacman.ui3d.scene3d.PlayScene3D;

import static de.amr.games.pacman.ui3d.GlobalProperties3d.PY_3D_ENABLED;

public class MsPacManGameTengenConfiguration_3D extends MsPacManGameTengenConfiguration {

    public MsPacManGameTengenConfiguration_3D() {
        set("PlayScene3D", new PlayScene3D());
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        GameScene gameScene2D = super.selectGameScene(context);
        if (PY_3D_ENABLED.get() && gameSceneHasID(gameScene2D, "PlayScene2D")) {
            GameScene playScene3D = get("PlayScene3D");
            return playScene3D != null ? playScene3D : gameScene2D;
        }
        return gameScene2D;
    }
}