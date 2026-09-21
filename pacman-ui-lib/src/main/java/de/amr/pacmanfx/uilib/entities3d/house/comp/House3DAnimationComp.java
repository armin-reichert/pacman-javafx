package de.amr.pacmanfx.uilib.entities3d.house.comp;

import de.amr.basics.Disposable;
import de.amr.basics.ui.ecs.GameEntityComp;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.util.Duration;

public class House3DAnimationComp implements GameEntityComp, Disposable {

    private final ManagedAnimation doorsMeltingAnimation;

    public House3DAnimationComp(AnimationRegistry animationRegistry) {
        doorsMeltingAnimation = new ManagedAnimation("House Doors Melting");
        animationRegistry.register(House3DAnimationID.HOUSE_DOORS_MELTING, doorsMeltingAnimation);
    }

    public void createDoorsMeltingAnimationFactory(DoubleProperty barThicknessProperty) {
        final double barThickness = 2.0 / House3DViewComp.DOOR_VERTICAL_BAR_COUNT;
        doorsMeltingAnimation.setAnimationFactory(() -> new Timeline(
            new KeyFrame(Duration.seconds(0.75), new KeyValue(barThicknessProperty, 0)),
            new KeyFrame(Duration.seconds(1.5),  new KeyValue(barThicknessProperty, barThickness)))
        );
    }

    public ManagedAnimation doorsMeltingAnimation() {
        return doorsMeltingAnimation;
    }

    @Override
    public void dispose() {
        doorsMeltingAnimation.dispose();
    }
}
