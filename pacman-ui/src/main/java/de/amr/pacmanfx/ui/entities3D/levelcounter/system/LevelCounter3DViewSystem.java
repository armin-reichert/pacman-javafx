/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.levelcounter.system;

import de.amr.basics.ui.entities.hud.levelCounter.LevelCounter;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.engine.config.GameVariantUIConfig;
import de.amr.pacmanfx.ui.entities3D.levelcounter.LevelCounter3DFactory;
import de.amr.pacmanfx.ui.entities3D.levelcounter.comp.LevelCounter3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.levelcounter.comp.LevelCounterView3D;
import javafx.scene.Group;

import static de.amr.basics.TileDimension.tilesPx;

public class LevelCounter3DViewSystem {

    public void updateLevelCounter3D(LevelCounter levelCounter, WorldMap worldMap, GameVariantUIConfig uiConfig) {

        final Group root = LevelCounter3DFactory.buildLevelCounter3D(
            levelCounter,
            uiConfig.worldSettings().levelCounter(),
            uiConfig.renderConfig()
        );

        final TerrainLayer terrain = worldMap.terrainLayer();
        root.setTranslateX(tilesPx(terrain.numCols() - 2));
        root.setTranslateY(tilesPx(2));
        root.setTranslateZ(-uiConfig.worldSettings().levelCounter().elevation());

        final LevelCounterView3D view3D = levelCounter.assertComponent(LevelCounterView3D.class);
        view3D.setRoot(root);

        levelCounter.optComponent(LevelCounter3DAnimationComp.class).ifPresent(animation -> {
            animation.spinningAnimation().invalidate(); // stops animation if present
            animation.spinningAnimation().replay();

        });
    }
}
