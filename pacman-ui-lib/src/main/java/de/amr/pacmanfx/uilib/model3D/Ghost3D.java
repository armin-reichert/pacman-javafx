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
import org.tinylog.Logger;

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

    private Color assetColor(String keySuffix) { return assets.color(assetNamespace + keySuffix); }

    public Color normalDressColor()        { return assetColor(".ghost.%d.color.normal.dress".formatted(personality));  }
    public Color normalPupilsColor()       { return assetColor(".ghost.%d.color.normal.pupils".formatted(personality)); }
    public Color normalEyeballsColor()     { return assetColor(".ghost.%d.color.normal.eyeballs".formatted(personality)); }
    public Color frightenedDressColor()    { return assetColor(".ghost.color.frightened.dress"); }
    public Color frightenedPupilsColor()   { return assetColor(".ghost.color.frightened.pupils"); }
    public Color frightenedEyeballsColor() { return assetColor(".ghost.color.frightened.eyeballs"); }
    public Color flashingDressColor()      { return assetColor(".ghost.color.flashing.dress"); }
    public Color flashingPupilsColor()     { return assetColor(".ghost.color.flashing.pupils"); }

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

        fixOrientation(root);
        resizeTo(size);
    }

    // TODO: fix orientation in OBJ file
    private void fixOrientation(Node shape) {
        shape.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS), new Rotate(180, Rotate.Y_AXIS), new Rotate(180, Rotate.Z_AXIS));
    }

    private void resizeTo(double size) {
        Bounds bounds = root.getBoundsInLocal();
        root.getTransforms().add(new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth()));
    }

    public Group root() {
        return root;
    }

    public void setRotation(double angle) {
        root.setRotationAxis(Rotate.Z_AXIS);
        root.setRotate(angle);
    }

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
        }
        if (dressAnimation.getStatus() != Status.RUNNING) {
            animationManager.registerAndPlayFromStart(root, "Ghost_DressMoving", dressAnimation);
        }
    }

    public void stopDressAnimation() {
        if (dressAnimation != null) {
            dressAnimation.stop();
        }
    }

    public void setFlashingAppearance(int numFlashes) {
        dressShape.setVisible(true);
        if (numFlashes == 0) {
            setFrightenedAppearance();
        } else {
            // Note: Total flashing time must be shorter than Pac power fading time (2s)!
            Duration totalFlashingTime = Duration.millis(1966);
            ensureFlashingAnimationIsPlaying(numFlashes, totalFlashingTime);
            Logger.trace("{} is flashing {} times", personality, numFlashes);
        }
    }

    public void setFrightenedAppearance() {
        ensureFlashingAnimationIsStopped();
        dressColorPy.set(frightenedDressColor());
        eyeballsColorPy.set(frightenedEyeballsColor());
        pupilsColorPy.set(frightenedPupilsColor());
        dressShape.setVisible(true);
        Logger.trace("Appear frightened, ghost {}", personality);
    }

    public void setNormalAppearance() {
        ensureFlashingAnimationIsStopped();
        dressColorPy.set(normalDressColor());
        eyeballsColorPy.set(normalEyeballsColor());
        pupilsColorPy.set(normalPupilsColor());
        dressShape.setVisible(true);
        Logger.trace("Appear normal, ghost {}", personality);
    }

    public void setEyesOnlyAppearance() {
        ensureFlashingAnimationIsStopped();
        eyeballsColorPy.set(normalEyeballsColor());
        pupilsColorPy.set(normalPupilsColor());
        dressShape.setVisible(false);
        Logger.trace("Appear eyes, ghost {}", personality);
    }

    private void createFlashingAnimation(int numFlashes, Duration totalDuration) {
        Duration flashEndTime = totalDuration.divide(numFlashes), highlightTime = flashEndTime.divide(3);
        flashingAnimation = new Timeline(
            new KeyFrame(highlightTime,
                new KeyValue(dressColorPy,  flashingDressColor()),
                new KeyValue(pupilsColorPy, flashingPupilsColor())
            ),
            new KeyFrame(flashEndTime,
                new KeyValue(dressColorPy,  frightenedDressColor()),
                new KeyValue(pupilsColorPy, frightenedPupilsColor())
            )
        );
        flashingAnimation.setCycleCount(numFlashes);
        Logger.trace("Created flashing animation ({} flashes, total time: {} seconds) for ghost {}",
            numFlashes, totalDuration.toSeconds(), personality);
    }

    private void ensureFlashingAnimationIsPlaying(int numFlashes, Duration duration) {
        if (flashingAnimation == null) {
            createFlashingAnimation(numFlashes, duration);
        }
        if (flashingAnimation.getStatus() != Status.RUNNING) {
            Logger.trace("Playing flashing animation for ghost {}", personality);
            flashingAnimation.play();
        }
    }

    private void ensureFlashingAnimationIsStopped() {
        if (flashingAnimation != null) {
            flashingAnimation.stop();
            flashingAnimation = null;
            Logger.trace("Stopped flashing animation for ghost {}", personality);
        }
    }
}