/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.model.world.map.WorldMapColorScheme;
import de.amr.pacmanfx.ui.settings.world.House3DSettings;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;
import javafx.scene.paint.Color;

public class HouseFactory3D {

    public void createHouse3D(House house, House3DSettings settings, WorldMapColorScheme colorScheme) {
        house.removeComp(House3DViewComp.class);
        house.setComp(House3DViewComp.class, new House3DViewComp(
            house.floorplan(),
            settings.baseHeight(),
            settings.wallThickness(),
            settings.opacity()
        ));
        final var house3D = house.reqComp(House3DViewComp.class);

        // apply color scheme
        house3D.setWallBaseColor(Color.valueOf(colorScheme.wallFill()));
        house3D.setWallTopColor(Color.valueOf(colorScheme.wallStroke()));
        house3D.setDoorColor(Color.valueOf(colorScheme.door()));

        house3D.wallBaseHeightProperty().set(settings.baseHeight());
        house3D.setDoorSensitivity(settings.sensitivity());
    }
}
