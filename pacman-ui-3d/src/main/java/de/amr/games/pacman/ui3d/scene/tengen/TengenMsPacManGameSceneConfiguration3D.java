package de.amr.games.pacman.ui3d.scene.tengen;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameSceneID;
import de.amr.games.pacman.ui2d.scene.tengen.TengenMsPacManGameSceneConfiguration;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui3d.PlayScene3D;

import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_ENABLED;

public class TengenMsPacManGameSceneConfiguration3D extends TengenMsPacManGameSceneConfiguration {

    public TengenMsPacManGameSceneConfiguration3D(AssetStorage assets) {
        super(assets);
        set(GameSceneID.PLAY_SCENE_3D, new PlayScene3D());
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        GameScene gameScene2D = super.selectGameScene(context);
        if (PY_3D_ENABLED.get() && gameSceneHasID(gameScene2D, GameSceneID.PLAY_SCENE)) {
            GameScene playScene3D = get(GameSceneID.PLAY_SCENE_3D);
            return playScene3D != null ? playScene3D : gameScene2D;
        }
        return gameScene2D;
    }
}