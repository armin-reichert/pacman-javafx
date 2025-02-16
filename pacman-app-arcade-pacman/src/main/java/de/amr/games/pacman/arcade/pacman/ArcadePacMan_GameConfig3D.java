/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui3d.GlobalProperties3d;
import de.amr.games.pacman.ui3d.scene3d.GameConfiguration3D;
import de.amr.games.pacman.ui3d.scene3d.PlayScene3D;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.scene.Node;

public class ArcadePacMan_GameConfig3D extends ArcadePacMan_GameConfig implements GameConfiguration3D {

    public ArcadePacMan_GameConfig3D(AssetStorage assets) {
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
    public TerrainRenderer3D createTerrainRenderer3D() {
        return new TerrainRenderer3D();
    }

    @Override
    public Node createLivesCounterShape(AssetStorage assets) {
        String akp = assetKeyPrefix();
        return PacModel3D.createPacShape(
            assets.get("model3D.pacman"), 10,
            assets.color(akp + ".pac.color.head"),
            assets.color(akp + ".pac.color.eyes"),
            assets.color(akp + ".pac.color.palate")
        );
    }
}
