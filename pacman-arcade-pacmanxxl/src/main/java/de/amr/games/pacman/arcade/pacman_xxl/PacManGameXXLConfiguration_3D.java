/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui3d.GlobalProperties3d;
import de.amr.games.pacman.ui3d.level.WorldRenderer3D;
import de.amr.games.pacman.ui3d.scene3d.GameConfiguration3D;
import de.amr.games.pacman.ui3d.scene3d.PlayScene3D;

public class PacManGameXXLConfiguration_3D extends PacManGameXXLConfiguration implements GameConfiguration3D {

    public PacManGameXXLConfiguration_3D(AssetStorage assets) {
        super(assets);
        setGameScene("PlayScene3D", new PlayScene3D());
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        GameScene gameScene2D = super.selectGameScene(context);
        if (GlobalProperties3d.PY_3D_ENABLED.get() && gameSceneHasID(gameScene2D, "PlayScene2D")) {
            GameScene playScene3D = getGameScene("PlayScene3D");
            return playScene3D != null ? playScene3D : gameScene2D;
        }
        return gameScene2D;
    }

    @Override
    public WorldRenderer3D createWorldRenderer() {
        return new WorldRenderer3D();
    }
}
