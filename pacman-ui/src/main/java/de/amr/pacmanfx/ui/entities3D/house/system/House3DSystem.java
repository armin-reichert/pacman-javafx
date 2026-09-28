package de.amr.pacmanfx.ui.entities3D.house.system;

import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.ui.entities3D.house.comp.House3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.house.comp.House3DViewComp;

public class House3DSystem {

    public void hideDoors(House house) {
        final House3DViewComp view3D = house.reqComp(House3DViewComp.class);
        view3D.setDoorsVisible(false);
    }

    public void updateLight(House house, boolean lightOn) {
        final House3DViewComp view3D = house.reqComp(House3DViewComp.class);
        view3D.light().lightOnProperty().set(lightOn);
    }

    public void update(House house, boolean accessRequested) {
        final House3DAnimationComp animation = house.reqComp(House3DAnimationComp.class);
        if (accessRequested) {
            if (!animation.doorsMeltingAnimation().isRunning()) {
                playDoorsMeltingAnimation(house);
            }
        }
    }

    private void playDoorsMeltingAnimation(House house) {
        final House3DAnimationComp animation = house.reqComp(House3DAnimationComp.class);
        animation.doorsMeltingAnimation().playFromStart();
    }

}
