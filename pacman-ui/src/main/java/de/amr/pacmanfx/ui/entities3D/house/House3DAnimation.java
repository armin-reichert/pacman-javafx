package de.amr.pacmanfx.ui.entities3D.house;

import de.amr.basics.ui.animation.ManagedAnimation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.util.Duration;

public class House3DAnimation extends ManagedAnimation {

    public House3DAnimation(DoubleProperty barThicknessProperty) {
        super("House Doors Melting");
        final double barThickness = 2.0 / HouseView3D.DOOR_VERTICAL_BAR_COUNT;
        setAnimationFactory(() -> new Timeline(
            new KeyFrame(Duration.seconds(0.75), new KeyValue(barThicknessProperty, 0)),
            new KeyFrame(Duration.seconds(1.5),  new KeyValue(barThicknessProperty, barThickness)))
        );
    }
}
