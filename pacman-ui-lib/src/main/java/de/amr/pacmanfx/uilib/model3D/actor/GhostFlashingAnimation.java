package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.GhostMaterialSet;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class GhostFlashingAnimation extends ManagedAnimation {

    private final GhostMaterialSet materialSet;
    private final GhostColorSet colorSet;
    private Duration totalDuration = Duration.seconds(3);
    private int numFlashes = 5;

    public GhostFlashingAnimation(Ghost ghost, GhostMaterialSet materialSet, GhostColorSet colorSet) {
        super("Ghost Flashing (%s)".formatted(ghost.name()));
        this.materialSet = materialSet;
        this.colorSet = colorSet;
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
                new KeyValue(flashingMaterialSet.dressMaterial().diffuseColorProperty(), colorSet.flashing().dress()),
                new KeyValue(flashingMaterialSet.pupilsMaterial().diffuseColorProperty(), colorSet.flashing().pupils())
            ),
            new KeyFrame(flashEndTime,
                new KeyValue(flashingMaterialSet.dressMaterial().diffuseColorProperty(), colorSet.frightened().dress()),
                new KeyValue(flashingMaterialSet.pupilsMaterial().diffuseColorProperty(), colorSet.frightened().pupils())
            )
        );
        flashingTimeline.setCycleCount(numFlashes);
        flashingTimeline.setOnFinished(_ -> {
            flashingMaterialSet.dressMaterial().setDiffuseColor(colorSet.frightened().dress());
            flashingMaterialSet.dressMaterial().setSpecularColor(colorSet.frightened().dress().brighter());
            flashingMaterialSet.pupilsMaterial().setDiffuseColor(colorSet.frightened().pupils());
            flashingMaterialSet.pupilsMaterial().setSpecularColor(colorSet.frightened().pupils().brighter());
        });
        return flashingTimeline;
    }
}
