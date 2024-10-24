/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.scene.image.Image;

public class TengenArcadeMapsSpriteSheet {

    static final Vector2i SIZE = new Vector2i(28*8, 31*8);

    private RectArea spriteAt(int rowIndex, int colIndex) {
        return new RectArea(colIndex * SIZE.x(), rowIndex * SIZE.y(), SIZE.x(), SIZE.y());
    }

    private final Image image;

    public TengenArcadeMapsSpriteSheet(AssetStorage assets) {
        image = assets.image("tengen.mazes.arcade");
    }

    public ImageArea mapSprite(int rowIndex, int colIndex) {
        return new ImageArea(image, spriteAt(rowIndex, colIndex));
    }

}
