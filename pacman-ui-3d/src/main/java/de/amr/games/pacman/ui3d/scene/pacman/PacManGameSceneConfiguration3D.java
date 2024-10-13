package de.amr.games.pacman.ui3d.scene.pacman;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGameSceneConfiguration;
import de.amr.games.pacman.ui3d.PlayScene3D;

import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_ENABLED;

public class PacManGameSceneConfiguration3D extends PacManGameSceneConfiguration {

    public PacManGameSceneConfiguration3D() {
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
