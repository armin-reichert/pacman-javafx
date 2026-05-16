/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostComponentMaterialSet;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostStateColors;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;

public class GhostFlashingAnimation3D extends ManagedAnimation {

    private int numFlashes;

    public GhostFlashingAnimation3D(Ghost3D ghost3D) {
        super("Ghost Flashing (%s)".formatted(ghost3D.ghost().name()));
        setFactory(() -> createAnimationFX(ghost3D));
    }

    public int numFlashes() {
        return numFlashes;
    }

    public void setNumFlashes(int numFlashes) {
        requireNonNegativeInt(numFlashes);
        if (this.numFlashes != numFlashes) {
            stop();
            this.numFlashes = numFlashes;
        }
    }

    private Animation createAnimationFX(Ghost3D ghost3D) {
        final Duration flashEndTime = Duration.seconds(2).divide(numFlashes);
        final Duration highlightTime = flashEndTime.divide(3);
        final GhostComponentMaterialSet flashingMaterials = ghost3D.materials().flashingMaterial();
        final GhostStateColors colors = ghost3D.config().colors();
        final var timeline = new Timeline(
            new KeyFrame(highlightTime,
                new KeyValue(flashingMaterials.dressMaterial().diffuseColorProperty(),  colors.flashingColors().dressColor()),
                new KeyValue(flashingMaterials.pupilsMaterial().diffuseColorProperty(), colors.flashingColors().pupilsColor())
            ),
            new KeyFrame(flashEndTime,
                new KeyValue(flashingMaterials.dressMaterial().diffuseColorProperty(), colors.frightenedColors().dressColor()),
                new KeyValue(flashingMaterials.pupilsMaterial().diffuseColorProperty(), colors.frightenedColors().pupilsColor())
            )
        );
        timeline.setCycleCount(numFlashes);
        timeline.setOnFinished(_ -> {
            flashingMaterials.dressMaterial().setDiffuseColor(colors.frightenedColors().dressColor());
            flashingMaterials.dressMaterial().setSpecularColor(colors.frightenedColors().dressColor().brighter());
            flashingMaterials.pupilsMaterial().setDiffuseColor(colors.frightenedColors().pupilsColor());
            flashingMaterials.pupilsMaterial().setSpecularColor(colors.frightenedColors().pupilsColor().brighter());
        });
        return timeline;
    }
}
