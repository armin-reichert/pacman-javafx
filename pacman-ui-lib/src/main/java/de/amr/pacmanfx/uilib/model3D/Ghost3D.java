/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of a ghost.
 */
public class Ghost3D extends Group {

    private final ObjectProperty<Color> dressColorPy    = new SimpleObjectProperty<>(Color.ORANGE);
    private final ObjectProperty<Color> eyeballsColorPy = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> pupilsColorPy   = new SimpleObjectProperty<>(Color.BLUE);

    private final byte personality;
    private final AssetStorage assets;
    private final String assetNamespace;
    private final Shape3D dressShape;
    private final Group dressGroup;

    private final ManagedAnimation dressAnimation;
    private final FlashingAnimation flashingAnimation;

    public Color normalDressColor()        { return assets.color(assetNamespace + ".ghost.%d.color.normal.dress".formatted(personality));  }
    public Color normalPupilsColor()       { return assets.color(assetNamespace + ".ghost.%d.color.normal.pupils".formatted(personality)); }
    public Color normalEyeballsColor()     { return assets.color(assetNamespace + ".ghost.%d.color.normal.eyeballs".formatted(personality)); }
    public Color frightenedDressColor()    { return assets.color(assetNamespace + ".ghost.color.frightened.dress"); }
    public Color frightenedPupilsColor()   { return assets.color(assetNamespace + ".ghost.color.frightened.pupils"); }
    public Color frightenedEyeballsColor() { return assets.color(assetNamespace + ".ghost.color.frightened.eyeballs"); }
    public Color flashingDressColor()      { return assets.color(assetNamespace + ".ghost.color.flashing.dress"); }
    public Color flashingPupilsColor()     { return assets.color(assetNamespace + ".ghost.color.flashing.pupils"); }

    public Ghost3D(
        AnimationManager animationManager,
        AssetStorage assets,
        String assetNamespace,
        byte personality,
        Shape3D dressShape,
        Shape3D pupilsShape,
        Shape3D eyeballsShape,
        double size)
    {
        requireNonNull(animationManager);
        this.assets = requireNonNull(assets);
        this.assetNamespace = requireNonNull(assetNamespace);
        this.personality = requireValidGhostPersonality(personality);
        this.dressShape = requireNonNull(dressShape);

        requireNonNull(pupilsShape);
        requireNonNull(eyeballsShape);
        requireNonNegative(size);

        dressShape.materialProperty().bind(dressColorPy.map(Ufx::coloredPhongMaterial));
        pupilsShape.materialProperty().bind(pupilsColorPy.map(Ufx::coloredPhongMaterial));
        eyeballsShape.materialProperty().bind(eyeballsColorPy.map(Ufx::coloredPhongMaterial));

        pupilsColorPy.set(normalPupilsColor());
        dressColorPy.set(normalDressColor());
        eyeballsColorPy.set(normalEyeballsColor());

        var eyesGroup = new Group(pupilsShape, eyeballsShape);
        dressGroup = new Group(dressShape);
        getChildren().setAll(dressGroup, eyesGroup);

        Bounds dressBounds = dressShape.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());
        dressShape.getTransforms().add(centeredOverOrigin);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        // TODO: fix orientation in OBJ file
        getTransforms().addAll(new Rotate(90, Rotate.X_AXIS), new Rotate(180, Rotate.Y_AXIS), new Rotate(180, Rotate.Z_AXIS));
        Bounds bounds = getBoundsInLocal();
        Scale scale = new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
        getTransforms().add(scale);

        dressAnimation = new ManagedAnimation(animationManager, "Ghost_DressMoving") {
            @Override
            protected Animation createAnimation() {
                var animation = new RotateTransition(Duration.seconds(0.3), dressGroup);
                // TODO I expected this should be the z-axis but... (transforms messed-up?)
                animation.setAxis(Rotate.Y_AXIS);
                animation.setByAngle(30);
                animation.setCycleCount(Animation.INDEFINITE);
                animation.setAutoReverse(true);
                return animation;
            }
        };

        flashingAnimation = new FlashingAnimation(animationManager);
    }

    public class FlashingAnimation extends ManagedAnimation {

        private Duration totalDuration = Duration.seconds(3);
        private int numFlashes = 5;

        public FlashingAnimation(AnimationManager animationManager) {
            super(animationManager, "Ghost_Flashing");
        }

        public void setTotalDuration(Duration totalDuration) {
            this.totalDuration = totalDuration;
            animation = null; // trigger creation
        }

        public void setNumFlashes(int numFlashed) {
            this.numFlashes = numFlashed;
            animation = null; // trigger creation
        }

        @Override
        protected Animation createAnimation() {
            Duration flashEndTime = totalDuration.divide(numFlashes), highlightTime = flashEndTime.divide(3);
            var flashingTimeline = new Timeline(
                new KeyFrame(highlightTime,
                    new KeyValue(dressColorPy,  flashingDressColor()),
                    new KeyValue(pupilsColorPy, flashingPupilsColor())
                ),
                new KeyFrame(flashEndTime,
                    new KeyValue(dressColorPy,  frightenedDressColor()),
                    new KeyValue(pupilsColorPy, frightenedPupilsColor())
                )
            );
            flashingTimeline.setCycleCount(numFlashes);
            return flashingTimeline;
        }
    }

    public ManagedAnimation dressAnimation() {
        return dressAnimation;
    }

    public FlashingAnimation flashingAnimation() {
        return flashingAnimation;
    }

    public Group root() {
        return this;
    }

    public void turnTowards(Direction dir) {
        setRotationAxis(Rotate.Z_AXIS);
        setRotate(switch (dir) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });
    }

    public void setFlashingAppearance(int numFlashes) {
        dressShape.setVisible(true);
        if (numFlashes == 0) {
            setFrightenedAppearance();
        } else {
            flashingAnimation.setNumFlashes(numFlashes);
            // TODO (fixme): Total flashing time must be shorter than Pac power fading time (2s)!
            flashingAnimation.setTotalDuration(Duration.millis(1950));
            flashingAnimation.play(ManagedAnimation.FROM_START);
        }
    }

    public void setFrightenedAppearance() {
        flashingAnimation.stop();
        dressColorPy.set(frightenedDressColor());
        eyeballsColorPy.set(frightenedEyeballsColor());
        pupilsColorPy.set(frightenedPupilsColor());
        dressShape.setVisible(true);
    }

    public void setNormalAppearance() {
        flashingAnimation.stop();
        dressColorPy.set(normalDressColor());
        eyeballsColorPy.set(normalEyeballsColor());
        pupilsColorPy.set(normalPupilsColor());
        dressShape.setVisible(true);
    }

    public void setEyesOnlyAppearance() {
        flashingAnimation.stop();
        eyeballsColorPy.set(normalEyeballsColor());
        pupilsColorPy.set(normalPupilsColor());
        dressShape.setVisible(false);
    }
}