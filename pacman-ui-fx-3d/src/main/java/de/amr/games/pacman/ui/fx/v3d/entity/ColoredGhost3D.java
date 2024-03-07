/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.animation.ColorFlashing;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.Animation.Status;
import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

import static de.amr.games.pacman.lib.Globals.checkGhostID;
import static de.amr.games.pacman.lib.Globals.requirePositive;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class ColoredGhost3D {

    public static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
    public static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
    public static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

    private final byte id;
    private final Theme theme;
    private final Group root;
    private final Group eyesGroup;
    private final Group dressGroup;
    private final Shape3D dressShape;
    private final Shape3D eyeballsShape;
    private final Shape3D pupilsShape;

    private final ObjectProperty<Color> dressColorPy = new SimpleObjectProperty<>(this, "dressColor", Color.ORANGE);
    private final ObjectProperty<Color> eyeballsColorPy = new SimpleObjectProperty<>(this, "eyeballsColor", Color.WHITE);
    private final ObjectProperty<Color> pupilsColorPy = new SimpleObjectProperty<>(this, "pupilsColor", Color.BLUE);

    private ParallelTransition flashingAnimation;
    private ColorFlashing dressFlashingAnimation;
    private ColorFlashing pupilsFlashingAnimation;

    public ColoredGhost3D(Model3D model3D, Theme theme, byte id, double size) {
        requireNonNull(model3D);
        requireNonNull(theme);
        checkGhostID(id);
        requirePositive(size, "ColoredGhost3D size must be positive but is %f");

        this.theme = theme;
        this.id = id;

        dressShape = new MeshView(model3D.mesh(MESH_ID_GHOST_DRESS));
        dressShape.setMaterial(Ufx.createColorBoundMaterial(dressColorPy));
        dressColorPy.set(theme.color("ghost.%d.color.normal.dress".formatted(id)));

        eyeballsShape = new MeshView(model3D.mesh(MESH_ID_GHOST_EYEBALLS));
        eyeballsShape.setMaterial(Ufx.createColorBoundMaterial(eyeballsColorPy));
        eyeballsColorPy.set(theme.color("ghost.%d.color.normal.eyeballs".formatted(id)));

        pupilsShape = new MeshView(model3D.mesh(MESH_ID_GHOST_PUPILS));
        pupilsShape.setMaterial(Ufx.createColorBoundMaterial(pupilsColorPy));
        pupilsColorPy.set(theme.color("ghost.%d.color.normal.pupils".formatted(id)));

        var centerTransform = Model3D.centerOverOrigin(dressShape);
        dressShape.getTransforms().add(centerTransform);

        dressGroup = new Group(dressShape);

        eyesGroup = new Group(pupilsShape, eyeballsShape);
        eyesGroup.getTransforms().add(centerTransform);

        root = new Group(dressGroup, eyesGroup);

        // TODO fix orientation in obj file
        root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
        root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        root.getTransforms().add(Model3D.scale(root, size));
    }

    public Node root() {
        return root;
    }

    public Group getEyesGroup() {
        return eyesGroup;
    }

    public Group getDressGroup() {
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
        dressFlashingAnimation = new ColorFlashing(theme.color("ghost.color.frightened.dress"),
            theme.color("ghost.color.flashing.dress"), durationSeconds, numFlashes);

        pupilsFlashingAnimation = new ColorFlashing(theme.color("ghost.color.frightened.pupils"),
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
}