/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.levelcounter.system;

import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.ui.entities3D.levelcounter.LevelCounter3DFactory;
import de.amr.pacmanfx.uilib.entities3D.levelcounter.comp.LevelCounter3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.levelcounter.comp.LevelCounter3DViewComp;
import javafx.scene.Group;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

public class LevelCounter3DViewSystem {

    public static void updateLevelCounter3D(GameVariantConfig gameVariantConfig, LevelCounter levelCounter, GameLevel level) {

        final Group root = LevelCounter3DFactory.buildLevelCounter3D(
            levelCounter,
            gameVariantConfig.worldSettings().levelCounter(),
            gameVariantConfig.renderConfig()
        );

        final TerrainLayer terrain = level.worldMap().terrainLayer();
        root.setTranslateX(tilesPx(terrain.numCols() - 2));
        root.setTranslateY(tilesPx(2));
        root.setTranslateZ(-gameVariantConfig.worldSettings().levelCounter().elevation());

        final LevelCounter3DViewComp view3D = levelCounter.requireComp(LevelCounter3DViewComp.class);
        view3D.setRoot(root);

        final LevelCounter3DAnimationComp anim3D = levelCounter.requireComp(LevelCounter3DAnimationComp.class);
        anim3D.spinningAnimation().invalidate(); // stops animation if present
        anim3D.spinningAnimation().playFromStart();
    }
}
