/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.tengen.ms_pacman.scene.TengenMsPacMan_PlayScene3D;
import de.amr.games.pacman.tilemap.rendering.TerrainRenderer3D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui3d.GlobalProperties3d;
import de.amr.games.pacman.ui3d.scene3d.GameConfiguration3D;
import de.amr.games.pacman.uilib.model3D.PacModel3D;
import javafx.scene.Group;
import javafx.scene.Node;

public class TengenMsPacMan_GameConfig3D extends TengenMsPacMan_GameConfig implements GameConfiguration3D {

    public TengenMsPacMan_GameConfig3D(AssetStorage assets) {
        super(assets);
        setGameScene("PlayScene3D", new TengenMsPacMan_PlayScene3D());
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
        return new Group(
            PacModel3D.createPacShape(
                assets.get("model3D.pacman"), 10,
                assets.color(akp + ".pac.color.head"),
                assets.color(akp + ".pac.color.eyes"),
                assets.color(akp + ".pac.color.palate")
            ),
            PacModel3D.createFemaleParts(10,
                assets.color(akp + ".pac.color.hairbow"),
                assets.color(akp + ".pac.color.hairbow.pearls"),
                assets.color(akp + ".pac.color.boobs")
            )
        );
    }
}