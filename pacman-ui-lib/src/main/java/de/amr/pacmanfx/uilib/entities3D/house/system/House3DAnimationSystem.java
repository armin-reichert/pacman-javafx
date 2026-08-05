package de.amr.pacmanfx.uilib.entities3D.house.system;

import de.amr.pacmanfx.core.entities.house.House;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.house.comp.House3DViewComp;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class House3DAnimationSystem {

    public static void update(House house, boolean accessRequested, boolean lightOn) {
        final House3DAnimationComp animation = house.requireComponent(House3DAnimationComp.class);
        animation.setAccessRequested(accessRequested);
        animation.setLightOn(lightOn);
        if (accessRequested) {
            if (!animation.doorsMeltingAnimation().isRunning()) {
                playDoorsMeltingAnimation(house);
            }
        }
        final House3DViewComp view3D = house.requireComponent(House3DViewComp.class);
        view3D.light().lightOnProperty().set(animation.lightOn());
    }

    private static void playDoorsMeltingAnimation(House house) {
        final House3DViewComp view3D = house.requireComponent(House3DViewComp.class);
        final House3DAnimationComp animation = house.requireComponent(House3DAnimationComp.class);
        final double barThickness = 2.0 / House3DViewComp.DOOR_VERTICAL_BAR_COUNT;
        animation.doorsMeltingAnimation().setFactory(() -> new Timeline(
            new KeyFrame(Duration.seconds(0.75), new KeyValue(view3D.barThicknessProperty(), 0)),
            new KeyFrame(Duration.seconds(1.5),  new KeyValue(view3D.barThicknessProperty(), barThickness)))
        );
        animation.doorsMeltingAnimation().playFromStart();
    }
}
