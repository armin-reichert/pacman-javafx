package de.amr.pacmanfx.uilib.entities3D.house.system;

import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;

public class House3DSystem {

    public static void hideDoors(House house) {
        final House3DViewComp view3D = house.requireComponent(House3DViewComp.class);
        view3D.setDoorsVisible(false);
    }

    public static void showLight(House house, boolean lightOn) {
        final House3DViewComp view3D = house.requireComponent(House3DViewComp.class);
        view3D.light().lightOnProperty().set(lightOn);
    }
}
