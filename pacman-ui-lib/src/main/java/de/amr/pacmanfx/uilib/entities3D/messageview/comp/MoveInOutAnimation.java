/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.messageview.comp;


import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class MoveInOutAnimation extends ManagedAnimation {

    private final MessageView3DComp view3D;

    public MoveInOutAnimation(MessageView3DComp view3D) {
        super("Level Message Movement");
        this.view3D = requireNonNull(view3D);
        setAnimationFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        double hiddenZ = MessageView3DAnimationComp.hiddenZPosition(view3D);
        double visibleZ = -(hiddenZ + 2);

        var moveUp = new TranslateTransition(Duration.seconds(1), view3D.root());
        moveUp.setToZ(visibleZ);

        var moveDown = new TranslateTransition(Duration.seconds(1), view3D.root());
        moveDown.setToZ(hiddenZ);

        var movement = new SequentialTransition(
            moveUp,
            new PauseTransition(Duration.seconds(view3D.displaySeconds())),
            moveDown
        );
        movement.setOnFinished(_ -> view3D.root().setVisible(false));

        return movement;
    }
}
