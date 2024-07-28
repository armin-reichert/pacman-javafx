/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.level;

import de.amr.games.pacman.ui2d.util.Theme;
import de.amr.games.pacman.ui3d.animation.ColorSwitchTransition;
import de.amr.games.pacman.ui3d.model.Model3D;
import javafx.animation.Animation.Status;
import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import static de.amr.games.pacman.lib.Globals.requirePositive;
import static de.amr.games.pacman.model.GameModel.checkGhostID;
import static de.amr.games.pacman.ui2d.util.Ufx.createColorBoundMaterial;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class Ghost3D extends Group {

    public static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
    public static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
    public static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

    private final byte id;
    private final Theme theme;
    private final Group dressGroup;
    private final Shape3D dressShape;
    private final Shape3D eyeballsShape;
    private final Shape3D pupilsShape;

    private final ObjectProperty<Color> dressColorPy = new SimpleObjectProperty<>(this, "dressColor", Color.ORANGE);
    private final ObjectProperty<Color> eyeballsColorPy = new SimpleObjectProperty<>(this, "eyeballsColor", Color.WHITE);
    private final ObjectProperty<Color> pupilsColorPy = new SimpleObjectProperty<>(this, "pupilsColor", Color.BLUE);

    private ParallelTransition flashingAnimation;
    private ColorSwitchTransition dressFlashingAnimation;
    private ColorSwitchTransition pupilsFlashingAnimation;

    public Ghost3D(Model3D model3D, Theme theme, byte id, double size) {
        requireNonNull(model3D);
        requireNonNull(theme);
        checkGhostID(id);
        requirePositive(size, "Size must be positive but is %f");

        this.theme = theme;
        this.id = id;

        dressShape = new MeshView(model3D.mesh(MESH_ID_GHOST_DRESS));
        dressShape.setMaterial(createColorBoundMaterial(dressColorPy));
        dressColorPy.set(theme.color("ghost.%d.color.normal.dress".formatted(id)));

        eyeballsShape = new MeshView(model3D.mesh(MESH_ID_GHOST_EYEBALLS));
        eyeballsShape.setMaterial(createColorBoundMaterial(eyeballsColorPy));
        eyeballsColorPy.set(theme.color("ghost.%d.color.normal.eyeballs".formatted(id)));

        pupilsShape = new MeshView(model3D.mesh(MESH_ID_GHOST_PUPILS));
        pupilsShape.setMaterial(createColorBoundMaterial(pupilsColorPy));
        pupilsColorPy.set(theme.color("ghost.%d.color.normal.pupils".formatted(id)));

        dressGroup = new Group(dressShape);
        Group eyesGroup = new Group(pupilsShape, eyeballsShape);
        getChildren().setAll(dressGroup, eyesGroup);

        Bounds dressBounds = dressShape.getBoundsInLocal();
        var centeredOverOrigin = new Translate(-dressBounds.getCenterX(), -dressBounds.getCenterY(), -dressBounds.getCenterZ());
        dressShape.getTransforms().add(centeredOverOrigin);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        // TODO: fix orientation in OBJ file
        getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
        resizeTo(size);
    }

    private void resizeTo(double size) {
        Bounds bounds = getBoundsInLocal();
        getTransforms().add(new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth()));
    }

    public void appearFlashing(int numFlashes, double durationSeconds) {
        ensureFlashingAnimationIsPlaying(numFlashes, durationSeconds);
        dressColorPy.bind(dressFlashingAnimation.colorPy);
        eyeballsColorPy.set(theme.color("ghost.color.frightened.eyeballs"));
        pupilsColorPy.bind(pupilsFlashingAnimation.colorPy);
        dressShape.setVisible(true);
    }

    public void appearFrightened() {
        dressColorPy.unbind();
        dressColorPy.set(theme.color("ghost.color.frightened.dress"));
        eyeballsColorPy.set(theme.color("ghost.color.frightened.eyeballs"));
        pupilsColorPy.unbind();
        pupilsColorPy.set(theme.color("ghost.color.frightened.pupils"));
        dressShape.setVisible(true);
        ensureFlashingAnimationIsStopped();
    }

    public void appearNormal() {
        dressColorPy.unbind();
        dressColorPy.set(theme.color("ghost.%d.color.normal.dress".formatted(id)));
        eyeballsColorPy.set(theme.color("ghost.%d.color.normal.eyeballs".formatted(id)));
        pupilsColorPy.unbind();
        pupilsColorPy.set(theme.color("ghost.%d.color.normal.pupils".formatted(id)));
        dressShape.setVisible(true);
        ensureFlashingAnimationIsStopped();
    }

    public void appearEyesOnly() {
        appearNormal();
        dressShape.setVisible(false);
    }

    private void createFlashingAnimation(int numFlashes, double durationSeconds) {
        dressFlashingAnimation = new ColorSwitchTransition(theme.color("ghost.color.frightened.dress"),
            theme.color("ghost.color.flashing.dress"), durationSeconds, numFlashes);

        pupilsFlashingAnimation = new ColorSwitchTransition(theme.color("ghost.color.frightened.pupils"),
            theme.color("ghost.color.flashing.pupils"), durationSeconds, numFlashes);

        flashingAnimation = new ParallelTransition(dressFlashingAnimation, pupilsFlashingAnimation);
    }

    private void ensureFlashingAnimationIsPlaying(int numFlashes, double durationSeconds) {
        if (flashingAnimation == null) {
            createFlashingAnimation(numFlashes, durationSeconds);
        }
        if (flashingAnimation.getStatus() != Status.RUNNING) {
            flashingAnimation.playFromStart();
        }
    }

    private void ensureFlashingAnimationIsStopped() {
        if (flashingAnimation != null && flashingAnimation.getStatus() == Status.RUNNING) {
            flashingAnimation.stop();
            flashingAnimation = null;
        }
    }

    public Group dressGroup() {
        return dressGroup;
    }

    public Shape3D dressShape() {
        return dressShape;
    }

    public Shape3D eyeballsShape() {
        return eyeballsShape;
    }

    public Shape3D pupilsShape() {
        return pupilsShape;
    }
}