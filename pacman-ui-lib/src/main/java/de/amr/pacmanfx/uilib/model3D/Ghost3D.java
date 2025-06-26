/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.assets.AssetStorage;
import javafx.animation.*;
import javafx.animation.Animation.Status;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
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
public class Ghost3D {

    private final ObjectProperty<Color> dressColorPy    = new SimpleObjectProperty<>(Color.ORANGE);
    private final ObjectProperty<Color> eyeballsColorPy = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> pupilsColorPy   = new SimpleObjectProperty<>(Color.BLUE);

    private final byte personality;
    private final AssetStorage assets;
    private final String assetNamespace;
    private final Group root = new Group();
    private final Shape3D dressShape;
    private final Group dressGroup;

    private final AnimationManager animationManager;
    private Animation dressAnimation;
    private Animation flashingAnimation;

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
        this.animationManager = requireNonNull(animationManager);
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
        root.getChildren().setAll(dressGroup, eyesGroup);

        Bounds dressBounds = dressShape.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());
        dressShape.getTransforms().add(centeredOverOrigin);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        // TODO: fix orientation in OBJ file
        root.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS), new Rotate(180, Rotate.Y_AXIS), new Rotate(180, Rotate.Z_AXIS));
        Bounds bounds = root.getBoundsInLocal();
        Scale scale = new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
        root.getTransforms().add(scale);
    }

    public Group root() {
        return root;
    }

    public void setRotation(double angle) {
        root.setRotationAxis(Rotate.Z_AXIS);
        root.setRotate(angle);
    }

    public void setFlashingAppearance(int numFlashes) {
        dressShape.setVisible(true);
        if (numFlashes == 0) {
            setFrightenedAppearance();
        } else {
            // Note: Total flashing time must be shorter than Pac power fading time (2s)!
            playFlashingAnimation(numFlashes, Duration.millis(1966));
        }
    }

    public void setFrightenedAppearance() {
        stopFlashingAnimation();;
        dressColorPy.set(frightenedDressColor());
        eyeballsColorPy.set(frightenedEyeballsColor());
        pupilsColorPy.set(frightenedPupilsColor());
        dressShape.setVisible(true);
    }

    public void setNormalAppearance() {
        stopFlashingAnimation();
        dressColorPy.set(normalDressColor());
        eyeballsColorPy.set(normalEyeballsColor());
        pupilsColorPy.set(normalPupilsColor());
        dressShape.setVisible(true);
    }

    public void setEyesOnlyAppearance() {
        stopFlashingAnimation();
        eyeballsColorPy.set(normalEyeballsColor());
        pupilsColorPy.set(normalPupilsColor());
        dressShape.setVisible(false);
    }

    // Dress animation

    private RotateTransition createDressAnimation() {
        var animation = new RotateTransition(Duration.seconds(0.3), dressGroup);
        // TODO I expected this should be the z-axis but... (transforms messed-up?)
        animation.setAxis(Rotate.Y_AXIS);
        animation.setByAngle(30);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setAutoReverse(true);
        return animation;
    }

    public void playDressAnimation() {
        if (dressAnimation == null) {
            dressAnimation = createDressAnimation();
            animationManager.register("Ghost_DressMoving", dressAnimation);
        }
        dressAnimation.play();
    }

    public void stopDressAnimation() {
        if (dressAnimation != null) {
            dressAnimation.stop();
        }
    }

    // Flashing animation

    private Animation createFlashingAnimation(int numFlashes, Duration totalDuration) {
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

    private void playFlashingAnimation(int numFlashes, Duration duration) {
        if (flashingAnimation == null) {
            flashingAnimation = createFlashingAnimation(numFlashes, duration);
            animationManager.register("Ghost_Flashing", flashingAnimation);
        }
        flashingAnimation.playFromStart();
    }

    public void stopFlashingAnimation() {
        if (flashingAnimation != null) {
            flashingAnimation.stop();
        }
    }
}