/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;

public class Pac3DTransformController {

    public void init(Pac3D pac3D, WorldMap worldMap) {
        update(pac3D, worldMap);
        pac3D.setScaleX(1.0);
        pac3D.setScaleY(1.0);
        pac3D.setScaleZ(1.0);
    }

    public void update(Pac3D pac3D, WorldMap worldMap) {
        final Pac pac = pac3D.pac();
        final Vector2f center = pac.center();

        pac3D.setTranslateX(center.x());
        pac3D.setTranslateY(center.y());
        pac3D.setTranslateZ(-10);

        pac3D.facingRotation().setAngle(switch (pac.moveDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });

        final boolean outsideWorld = pac3D.getTranslateX() < HTS
            || pac3D.getTranslateX() > TS * worldMap.numCols() - HTS;
        pac3D.setVisible(pac.isVisible() && !outsideWorld);
    }
}
