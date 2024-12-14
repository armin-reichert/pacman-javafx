/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.assets.AssetStorage;
import de.amr.games.pacman.ui.lib.Ufx;
import javafx.animation.*;
import javafx.animation.Animation.Status;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.requirePositive;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class Ghost3D {

    public final ObjectProperty<DrawMode> drawModePy    = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final ObjectProperty<Color> dressColorPy    = new SimpleObjectProperty<>(this, "dressColor", Color.ORANGE);
    private final ObjectProperty<Color> eyeballsColorPy = new SimpleObjectProperty<>(this, "eyeballsColor", Color.WHITE);
    private final ObjectProperty<Color> pupilsColorPy   = new SimpleObjectProperty<>(this, "pupilsColor", Color.BLUE);

    private final byte id;
    private final AssetStorage assets;
    private final String assetPrefix;
    private final Group root = new Group();
    private final Shape3D dress;

    private final RotateTransition dressAnimation;
    private Animation flashingAnimation;

    private Color color(String keySuffix) { return assets.color(assetPrefix + keySuffix); }

    private Color normalDressColor()        { return color(".ghost.%d.color.normal.dress".formatted(id));  }
    private Color normalPupilsColor()       { return color(".ghost.%d.color.normal.pupils".formatted(id)); }
    private Color normalEyeballsColor()     { return color(".ghost.%d.color.normal.eyeballs".formatted(id)); }
    private Color frightenedDressColor()    { return color(".ghost.color.frightened.dress"); }
    private Color frightenedPupilsColor()   { return color(".ghost.color.frightened.pupils"); }
    private Color frightenedEyeballsColor() { return color(".ghost.color.frightened.eyeballs"); }
    private Color flashingDressColor()      { return color(".ghost.color.flashing.dress"); }
    private Color flashingPupilsColor()     { return color(".ghost.color.flashing.pupils"); }

    public Ghost3D(
        byte id,
        Shape3D dressShape, Shape3D pupilsShape, Shape3D eyeballsShape,
        AssetStorage assets, String assetPrefix, double size) {

        GameLevel.checkGhostID(id);
        requireNonNull(dressShape);
        requireNonNull(pupilsShape);
        requireNonNull(eyeballsShape);
        requireNonNull(assets);
        requireNonNull(assetPrefix);
        requirePositive(size, "Size must be positive but is %f");

        this.id = id;
        this.assets = assets;
        this.assetPrefix = assetPrefix;

        dress = dressShape;
        dress.materialProperty().bind(dressColorPy.map(Ufx::coloredMaterial));
        dress.drawModeProperty().bind(drawModePy);

        pupilsShape.materialProperty().bind(pupilsColorPy.map(Ufx::coloredMaterial));
        pupilsShape.drawModeProperty().bind(drawModePy);

        eyeballsShape.materialProperty().bind(eyeballsColorPy.map(Ufx::coloredMaterial));
        eyeballsShape.drawModeProperty().bind(drawModePy);

        pupilsColorPy.set(normalPupilsColor());
        dressColorPy.set(normalDressColor());
        eyeballsColorPy.set(normalEyeballsColor());

        var eyesGroup = new Group(pupilsShape, eyeballsShape);
        var dressGroup = new Group(dress);
        root.getChildren().setAll(dressGroup, eyesGroup);

        Bounds dressBounds = dress.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());
        dress.getTransforms().add(centeredOverOrigin);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        dressAnimation = new RotateTransition(Duration.seconds(0.3), dressGroup);
        // TODO I expected this should be the z-axis but... (transforms messed-up?)
        dressAnimation.setAxis(Rotate.Y_AXIS);
        dressAnimation.setByAngle(30);
        dressAnimation.setCycleCount(Animation.INDEFINITE);
        dressAnimation.setAutoReverse(true);

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

    public void turnTo(int angle) {
        root.setRotationAxis(Rotate.Z_AXIS);
        root.setRotate(angle);
    }

    public void playDressAnimation() {
        dressAnimation.play();
    }

    public void stopDressAnimation() {
        dressAnimation.stop();
    }

    public void appearFlashing(int numFlashes) {
        dress.setVisible(true);
        if (numFlashes == 0) {
            appearFrightened();
        } else {
            // Note: Total flashing time must be shorter than Pac power fading time (2s)!
            Duration totalFlashingTime = Duration.millis(1966);
            ensureFlashingAnimationIsPlaying(numFlashes, totalFlashingTime);
            Logger.info("{} is flashing {} times", id, numFlashes);
        }
    }

    public void appearFrightened() {
        dress.setVisible(true);
        ensureFlashingAnimationIsStopped();
        dressColorPy.set(frightenedDressColor());
        eyeballsColorPy.set(frightenedEyeballsColor());
        pupilsColorPy.set(frightenedPupilsColor());
        Logger.info("Appear frightened, ghost {}", id);
    }

    public void appearNormal() {
        dress.setVisible(true);
        ensureFlashingAnimationIsStopped();
        dressColorPy.set(normalDressColor());
        eyeballsColorPy.set(normalEyeballsColor());
        pupilsColorPy.set(normalPupilsColor());
        Logger.info("Appear normal, ghost {}", id);
    }

    public void appearEyesOnly() {
        dress.setVisible(false);
        ensureFlashingAnimationIsStopped();
        eyeballsColorPy.set(normalEyeballsColor());
        pupilsColorPy.set(normalPupilsColor());
        Logger.info("Appear eyes, ghost {}", id);
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
        Logger.info("Created flashing animation ({} flashes, total time: {} seconds) for ghost {}",
            numFlashes, totalDuration.toSeconds(), id);
    }

    private void ensureFlashingAnimationIsPlaying(int numFlashes, Duration duration) {
        if (flashingAnimation == null) {
            createFlashingAnimation(numFlashes, duration);
        }
        if (flashingAnimation.getStatus() != Status.RUNNING) {
            Logger.info("Playing flashing animation for ghost {}", id);
            flashingAnimation.play();
        }
    }

    private void ensureFlashingAnimationIsStopped() {
        if (flashingAnimation != null) {
            flashingAnimation.stop();
            flashingAnimation = null;
            Logger.info("Stopped flashing animation for ghost {}", id);
        }
    }
}