/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.*;
import javafx.animation.Animation.Status;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.requirePositive;
import static de.amr.games.pacman.model.GameModel.checkGhostID;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredMaterial;
import static de.amr.games.pacman.ui2d.util.Ufx.createColorBoundMaterial;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class Ghost3D {

    static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
    static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
    static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

    public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

    private final byte id;
    private final Theme theme;
    private final Group root = new Group();
    private final Shape3D dressShape;

    private final ObjectProperty<Color> dressColorPy = new SimpleObjectProperty<>(this, "dressColor", Color.ORANGE);
    private final ObjectProperty<Color> eyeballsColorPy = new SimpleObjectProperty<>(this, "eyeballsColor", Color.WHITE);
    private final ObjectProperty<Color> pupilsColorPy = new SimpleObjectProperty<>(this, "pupilsColor", Color.BLUE);

    private final RotateTransition dressAnimation;
    private Animation flashingAnimation;

    public Ghost3D(Model3D model3D, Theme theme, byte id, double size) {
        requireNonNull(model3D);
        requireNonNull(theme);
        checkGhostID(id);
        requirePositive(size, "Size must be positive but is %f");

        this.theme = theme;
        this.id = id;

        dressShape = new MeshView(model3D.mesh(MESH_ID_GHOST_DRESS));
        PhongMaterial dressMaterial = coloredMaterial(dressColorPy.get());
        dressMaterial.diffuseColorProperty().bind(dressColorPy);
        dressMaterial.specularColorProperty().bind(dressColorPy.map(Color::brighter));
        dressShape.setMaterial(dressMaterial);
        dressShape.drawModeProperty().bind(drawModePy);
        dressColorPy.set(theme.color("ghost.%d.color.normal.dress".formatted(id)));

        var dressGroup = new Group(dressShape);

        dressAnimation = new RotateTransition(Duration.seconds(0.3), dressGroup);
        // TODO I expected this should be the z-axis but... (transforms messed-up?)
        dressAnimation.setAxis(Rotate.Y_AXIS);
        dressAnimation.setByAngle(30);
        dressAnimation.setCycleCount(Animation.INDEFINITE);
        dressAnimation.setAutoReverse(true);

        var pupilsShape = new MeshView(model3D.mesh(MESH_ID_GHOST_PUPILS));
        PhongMaterial pupilsMaterial = coloredMaterial(pupilsColorPy.get());
        pupilsMaterial.diffuseColorProperty().bind(pupilsColorPy);
        pupilsMaterial.specularColorProperty().bind(pupilsColorPy.map(Color::brighter));
        pupilsShape.setMaterial(pupilsMaterial);
        pupilsShape.drawModeProperty().bind(drawModePy);
        pupilsColorPy.set(theme.color("ghost.%d.color.normal.pupils".formatted(id)));

        var eyeballsShape = new MeshView(model3D.mesh(MESH_ID_GHOST_EYEBALLS));
        eyeballsShape.setMaterial(createColorBoundMaterial(eyeballsColorPy));
        eyeballsShape.drawModeProperty().bind(drawModePy);
        eyeballsColorPy.set(theme.color("ghost.%d.color.normal.eyeballs".formatted(id)));

        var eyesGroup = new Group(pupilsShape, eyeballsShape);
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

    public void turnTo(double angle) {
        root.setRotationAxis(Rotate.Z_AXIS);
        root.setRotate(angle);
        //TODO doesn't work as wanted: should turn clockwise/anticlockwise depending on orientation change
        //new Timeline(new KeyFrame(Duration.millis(80), new KeyValue(root.rotateProperty(), angle))).play();
    }

    public void playDressAnimation() {
        dressAnimation.play();
    }

    public void stopDressAnimation() {
        dressAnimation.stop();
    }

    public void appearFlashing(int numFlashes) {
        if (numFlashes == 0) {
            appearFrightened();
        } else {
            // Note: Total flashing time must be shorter than Pac power fading time (2s)!
            Duration totalFlashingTime = Duration.seconds(1.95);
            ensureFlashingAnimationIsPlaying(numFlashes, totalFlashingTime);
            eyeballsColorPy.set(theme.color("ghost.color.frightened.eyeballs"));
            dressShape.setVisible(true);
            Logger.info("Appear flashing, ghost {}", id);
        }
    }

    public void appearFrightened() {
        ensureFlashingAnimationIsStopped();
        dressColorPy.set(theme.color("ghost.color.frightened.dress"));
        eyeballsColorPy.set(theme.color("ghost.color.frightened.eyeballs"));
        pupilsColorPy.set(theme.color("ghost.color.frightened.pupils"));
        dressShape.setVisible(true);
        Logger.info("Appear frightened, ghost {}", id);
    }

    public void appearNormal() {
        ensureFlashingAnimationIsStopped();
        dressColorPy.set(theme.color("ghost.%d.color.normal.dress".formatted(id)));
        pupilsColorPy.set(theme.color("ghost.%d.color.normal.pupils".formatted(id)));
        eyeballsColorPy.set(theme.color("ghost.%d.color.normal.eyeballs".formatted(id)));
        dressShape.setVisible(true);
        Logger.info("Appear normal, ghost {}", id);
    }

    public void appearEyesOnly() {
        ensureFlashingAnimationIsStopped();
        pupilsColorPy.set(theme.color("ghost.%d.color.normal.pupils".formatted(id)));
        eyeballsColorPy.set(theme.color("ghost.%d.color.normal.eyeballs".formatted(id)));
        dressShape.setVisible(false);
        Logger.info("Appear eyes, ghost {}", id);
    }

    private void createFlashingAnimation(int numFlashes, Duration totalDuration) {
        Duration flashEndTime = totalDuration.divide(numFlashes), highlightTime = flashEndTime.divide(3);
        flashingAnimation = new Timeline(
            new KeyFrame(highlightTime,
                new KeyValue(dressColorPy,  theme.color("ghost.color.flashing.dress")),
                new KeyValue(pupilsColorPy, theme.color("ghost.color.flashing.pupils"))
            ),
            new KeyFrame(flashEndTime,
                new KeyValue(dressColorPy,  theme.color("ghost.color.frightened.dress")),
                new KeyValue(pupilsColorPy, theme.color("ghost.color.frightened.pupils"))
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

    public Group root() {
        return root;
    }
}