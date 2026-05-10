/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostMaterialSet;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostAppearanceColors;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostComponentMaterialSet;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class GhostFlashingAnimation3D extends ManagedAnimation {

    private final GhostMaterialSet materialSet;
    private final GhostAppearanceColors colors;
    private Duration totalDuration = Duration.seconds(3);
    private int numFlashes = 5;

    public GhostFlashingAnimation3D(Ghost ghost, GhostMaterialSet materialSet, GhostAppearanceColors colors) {
        super("Ghost Flashing (%s)".formatted(ghost.name()));
        this.materialSet = materialSet;
        this.colors = colors;
        setFactory(this::createAnimationFX);
    }

    public int numFlashes() {
        return numFlashes;
    }

    public void setTotalDuration(Duration totalDuration) {
        this.totalDuration = requireNonNull(totalDuration);
        invalidate();
    }

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = Validations.requireNonNegativeInt(numFlashes);
        invalidate();
    }

    private Animation createAnimationFX() {
        final Duration flashEndTime = totalDuration.divide(numFlashes);
        final Duration highlightTime = flashEndTime.divide(3);
        final GhostComponentMaterialSet flashingMaterialSet = materialSet.flashingMaterial();
        final var flashingTimeline = new Timeline(
            new KeyFrame(highlightTime,
                new KeyValue(flashingMaterialSet.dressMaterial().diffuseColorProperty(), colors.flashingColor().dressColor()),
                new KeyValue(flashingMaterialSet.pupilsMaterial().diffuseColorProperty(), colors.flashingColor().pupilsColor())
            ),
            new KeyFrame(flashEndTime,
                new KeyValue(flashingMaterialSet.dressMaterial().diffuseColorProperty(), colors.frightenedColor().dressColor()),
                new KeyValue(flashingMaterialSet.pupilsMaterial().diffuseColorProperty(), colors.frightenedColor().pupilsColor())
            )
        );
        flashingTimeline.setCycleCount(numFlashes);
        flashingTimeline.setOnFinished(_ -> {
            flashingMaterialSet.dressMaterial().setDiffuseColor(colors.frightenedColor().dressColor());
            flashingMaterialSet.dressMaterial().setSpecularColor(colors.frightenedColor().dressColor().brighter());
            flashingMaterialSet.pupilsMaterial().setDiffuseColor(colors.frightenedColor().pupilsColor());
            flashingMaterialSet.pupilsMaterial().setSpecularColor(colors.frightenedColor().pupilsColor().brighter());
        });
        return flashingTimeline;
    }
}
