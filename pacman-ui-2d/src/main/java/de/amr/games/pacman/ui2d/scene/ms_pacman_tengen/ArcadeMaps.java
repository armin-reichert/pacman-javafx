/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.scene.image.Image;

public class ArcadeMaps {

    private final Image image;

    public ArcadeMaps(AssetStorage assets) {
        image = assets.image(GameAssets2D.PFX_MS_PACMAN_TENGEN + ".mazes.arcade");
    }

    public ImageArea sprite(WorldMap worldMap) {
        int mapNumber = worldMap.getConfigValue("mapNumber");
        NES_ColorScheme colorScheme = worldMap.getConfigValue("nesColorScheme");
        int index = switch (mapNumber) {
            case 1 -> 0;
            case 2 -> 1;
            case 3 -> switch (colorScheme) {
                case _16_20_15_ORANGE_WHITE_RED   -> 2;
                case _35_28_20_PINK_YELLOW_WHITE  -> 4;
                case _17_20_20_BROWN_WHITE_WHITE  -> 6;
                case _0F_20_28_BLACK_WHITE_YELLOW -> 8;
                default -> throw new IllegalArgumentException("No image found for map #3 and color scheme: " + colorScheme);
            };
            case 4 -> switch (colorScheme) {
                case _01_38_20_BLUE_YELLOW_WHITE   -> 3;
                case _36_15_20_PINK_RED_WHITE      -> 5;
                case _13_20_28_VIOLET_WHITE_YELLOW -> 7;
                default -> throw new IllegalArgumentException("No image found for map #4 and color scheme: " + colorScheme);
            };
            default -> throw new IllegalArgumentException("Illegal Arcade map number: " + mapNumber);
        };
        int col = index % 3, row = index / 3;
        int width = 28*8, height = 31*8;
        return new ImageArea(image, new RectArea(col * width, row * height, width, height));
    }
}