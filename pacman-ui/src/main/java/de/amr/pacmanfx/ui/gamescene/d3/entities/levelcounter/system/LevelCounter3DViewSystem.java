/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3.entities.levelcounter.system;

import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterData;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.ui.gamescene.d3.entities.levelcounter.comp.LevelCounter3DSettings;
import de.amr.pacmanfx.ui.gamescene.d3.entities.levelcounter.comp.LevelCounter3DViewComp;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.List;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.HTS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

public class LevelCounter3DViewSystem {

    private static final int CUBE_SPACING = 6;

    public static void updateLevelCounter3D(GameVariantConfig gameVariantConfig, LevelCounter levelCounter, GameLevel level) {
        final LevelCounterData data = levelCounter.requireComp(LevelCounterData.class);
        final LevelCounter3DViewComp view3D = levelCounter.requireComp(LevelCounter3DViewComp.class);

        final LevelCounter3DSettings config = gameVariantConfig.worldSettings().levelCounter();
        final float cubeSize = config.symbolSize();
        final List<Integer> symbolCodes = data.symbolCodes();

        view3D.root().getChildren().clear();
        for (int i = 0; i < symbolCodes.size(); ++i) {
            final Integer symbolCode = symbolCodes.get(i);
            final Image symbolImage = gameVariantConfig.renderConfig().bonusSymbolImage(symbolCode);
            // negative x position: cubes are placed from right to left!
            final Box cube = createCube(cubeSize, symbolImage, -(cubeSize + CUBE_SPACING) * i);
            view3D.root().getChildren().add(cube);
        }

        // Let factory of managed animation create a new JavaFX animation
        view3D.spinningAnimation().invalidate();

        final TerrainLayer terrain = level.worldMap().terrainLayer();
        view3D.root().setTranslateX(tilesPx(terrain.numCols() - 2));
        view3D.root().setTranslateY(tilesPx(2));
        view3D.root().setTranslateZ(-gameVariantConfig.worldSettings().levelCounter().elevation());

        view3D.spinningAnimation().stop();
        view3D.spinningAnimation().playFromStart();

    }

    private static Box createCube(float cubeSize, Image symbolImage, double x) {
        final var cube = new Box(cubeSize, cubeSize, cubeSize);
        final var texture = new PhongMaterial(Color.WHITE);
        texture.setDiffuseMap(symbolImage);
        cube.setMaterial(texture);

        cube.setTranslateX(x);
        cube.setTranslateY(0);
        cube.setTranslateZ(-HTS);

        return cube;
    }
}
