package de.amr.games.pacman.ui3d.scene.pacman;

import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameSceneID;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGameSceneConfiguration;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui3d.scene.common.PlayScene3D;
import de.amr.games.pacman.ui3d.scene.common.GameSceneConfiguration3D;
import javafx.beans.value.ObservableDoubleValue;

import static de.amr.games.pacman.ui3d.PacManGames3dApp.PY_3D_ENABLED;

public class PacManGameSceneConfiguration3D extends PacManGameSceneConfiguration implements GameSceneConfiguration3D {

    public PacManGameSceneConfiguration3D(AssetStorage assets) {
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

    @Override
    public void initPlayScene3D(GameContext context, ObservableDoubleValue widthProperty, ObservableDoubleValue heightProperty) {
        PlayScene3D playScene3D = (PlayScene3D) get(GameSceneID.PLAY_SCENE_3D);
        playScene3D.setContext(context);
        playScene3D.widthProperty().bind(widthProperty);
        playScene3D.heightProperty().bind(heightProperty);
    }
}
