/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.levelcounter;

import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterData;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.uilib.entities3D.levelcounter.comp.LevelCounter3DSettings;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.List;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.HTS;

public class LevelCounter3DFactory {

    private static final int CUBE_SPACING = 6;

    public static Group buildLevelCounter3D(
        LevelCounter levelCounter,
        LevelCounter3DSettings config,
        GameVariantRenderConfig renderConfig)
    {
        final LevelCounterData data = levelCounter.requireComp(LevelCounterData.class);
        final List<Integer> symbolCodes = data.symbolCodes();

        final Group root = new Group();
        for (int i = 0; i < symbolCodes.size(); ++i) {
            final int code = symbolCodes.get(i);
            final Image image = renderConfig.bonusSymbolImage(code);
            final float cubeSize = config.symbolSize();
            // negative x position: cubes are placed from right to left!
            final Box cube = createCube(cubeSize, image, -(cubeSize + CUBE_SPACING) * i);
            root.getChildren().add(cube);
        }

        return root;
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
