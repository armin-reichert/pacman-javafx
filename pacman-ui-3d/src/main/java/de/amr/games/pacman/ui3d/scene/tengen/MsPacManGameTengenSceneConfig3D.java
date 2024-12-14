/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.scene.tengen;

import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenSceneConfig;
import de.amr.games.pacman.ui.AssetStorage;
import de.amr.games.pacman.ui3d.scene.common.PlayScene3D;

import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_ENABLED;

public class MsPacManGameTengenSceneConfig3D extends MsPacManGameTengenSceneConfig {

    public MsPacManGameTengenSceneConfig3D(AssetStorage assets) {
        super(assets);
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