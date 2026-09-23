package de.amr.pacmanfx.uilib.entities3d.house.system;

import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.uilib.entities3d.house.comp.House3DAnimationComp;

public class House3DAnimationSystem {

    public static void update(House house, boolean accessRequested) {
        final House3DAnimationComp animation = house.reqComp(House3DAnimationComp.class);
        if (accessRequested) {
            if (!animation.doorsMeltingAnimation().isRunning()) {
                playDoorsMeltingAnimation(house);
            }
        }
    }

    private static void playDoorsMeltingAnimation(House house) {
        final House3DAnimationComp animation = house.reqComp(House3DAnimationComp.class);
        animation.doorsMeltingAnimation().playFromStart();
    }
}
