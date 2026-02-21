/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of a ghost.
 */
public class Ghost3D extends Group implements Disposable {

    public record MaterialSet(PhongMaterial dress, PhongMaterial eyeballs, PhongMaterial pupils) {}

    private final Ghost ghost;
    private final GhostColorSet colorSet;

    private MaterialSet normalMaterialSet;
    private MaterialSet frightenedMaterialSet;
    private MaterialSet flashingMaterialSet;

    private MeshView dressShape;
    private MeshView pupilsShape;
    private MeshView eyeballsShape;

    private Group dressGroup;

    public class DressAnimation extends ManagedAnimation {

        public DressAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Ghost_DressAnimation_%s".formatted(ghost.name()));
            setFactory(() -> {
                final var animation = new RotateTransition(Duration.seconds(0.3), dressGroup);
                // TODO: I expected this should be the z-axis but...
                animation.setAxis(Rotate.Y_AXIS);
                animation.setByAngle(30);
                animation.setCycleCount(Animation.INDEFINITE);
                animation.setAutoReverse(true);
                return animation;
            });
        }
    }

    public class FlashingAnimation extends ManagedAnimation {

        private Duration totalDuration = Duration.seconds(3);
        private int numFlashes = 5;

        public FlashingAnimation(AnimationRegistry animationRegistry) {
            super(animationRegistry, "Ghost_Flashing_%s".formatted(ghost.name()));
            setFactory(this::createAnimationFX);
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
            final var flashingTimeline = new Timeline(
                new KeyFrame(highlightTime,
                    new KeyValue(flashingMaterialSet.dress().diffuseColorProperty(),  colorSet.flashing().dress()),
                    new KeyValue(flashingMaterialSet.pupils().diffuseColorProperty(), colorSet.flashing().pupils())
                ),
                new KeyFrame(flashEndTime,
                    new KeyValue(flashingMaterialSet.dress().diffuseColorProperty(),  colorSet.frightened().dress()),
                    new KeyValue(flashingMaterialSet.pupils().diffuseColorProperty(), colorSet.frightened().pupils())
                )
            );
            flashingTimeline.setCycleCount(numFlashes);
            flashingTimeline.setOnFinished(_ -> {
                flashingMaterialSet.dress().setDiffuseColor(colorSet.frightened().dress());
                flashingMaterialSet.dress().setSpecularColor(colorSet.frightened().dress().brighter());
                flashingMaterialSet.pupils().setDiffuseColor(colorSet.frightened().pupils());
                flashingMaterialSet.pupils().setSpecularColor(colorSet.frightened().pupils().brighter());
            });
            return flashingTimeline;
        }
    }

    private DressAnimation dressAnimation;
    private FlashingAnimation flashingAnimation;

    public Ghost3D(
        AnimationRegistry animationRegistry,
        Ghost ghost,
        GhostColorSet colorSet,
        MeshView dressShape,
        MeshView pupilsShape,
        MeshView eyeballsShape,
        double size)
    {
        requireNonNull(animationRegistry);
        this.ghost         = requireNonNull(ghost);
        this.colorSet      = requireNonNull(colorSet);
        this.dressShape    = requireNonNull(dressShape);
        this.pupilsShape   = requireNonNull(pupilsShape);
        this.eyeballsShape = requireNonNull(eyeballsShape);
        requireNonNegative(size);

        normalMaterialSet = new MaterialSet(
            coloredPhongMaterial(colorSet.normal().dress()),
            coloredPhongMaterial(colorSet.normal().eyeballs()),
            coloredPhongMaterial(colorSet.normal().pupils())
        );

        frightenedMaterialSet = new MaterialSet(
            coloredPhongMaterial(colorSet.frightened().dress()),
            coloredPhongMaterial(colorSet.frightened().eyeballs()),
            coloredPhongMaterial(colorSet.frightened().pupils())
        );

        flashingMaterialSet = new MaterialSet(
            coloredPhongMaterial(colorSet.flashing().dress()),
            coloredPhongMaterial(colorSet.flashing().eyeballs()),
            coloredPhongMaterial(colorSet.flashing().pupils())
        );

        dressGroup = new Group(dressShape);
        final var eyesGroup = new Group(pupilsShape, eyeballsShape);

        getChildren().setAll(dressGroup, eyesGroup);

        final Bounds dressBounds = dressShape.getBoundsInLocal();
        var centeredOverOrigin = new Translate(
            -dressBounds.getCenterX(),
            -dressBounds.getCenterY(),
            -dressBounds.getCenterZ()
        );
        dressShape.getTransforms().add(centeredOverOrigin);
        eyesGroup.getTransforms().add(centeredOverOrigin);

        // TODO: change orientation in OBJ file?
        getTransforms().addAll(
            new Rotate(90, Rotate.X_AXIS),
            new Rotate(180, Rotate.Y_AXIS),
            new Rotate(180, Rotate.Z_AXIS)
        );

        final Bounds bounds = getBoundsInLocal();
        getTransforms().add(
            new Scale(
                size / bounds.getWidth(),
                size / bounds.getHeight(),
                size / bounds.getDepth())
        );

        dressAnimation = new DressAnimation(animationRegistry);
        flashingAnimation = new FlashingAnimation(animationRegistry);
    }

    public MaterialSet normalMaterialSet() {
        return normalMaterialSet;
    }

    public ManagedAnimation dressAnimation() {
        return dressAnimation;
    }

    public FlashingAnimation flashingAnimation() {
        return flashingAnimation;
    }

    public void turnTowards(Direction dir) {
        requireNonNull(dir);
        setRotationAxis(Rotate.Z_AXIS);
        setRotate(switch (dir) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });
    }

    public void setFlashingLook(int numFlashes) {
        if (numFlashes == 0) {
            setFrightenedLook();
            return;
        }
        setMaterialSet(flashingMaterialSet);
        dressShape.setVisible(true);
        flashingAnimation.setNumFlashes(numFlashes);
        // TODO (fixme): Total flashing time must be shorter than Pac power fading time (2s)!
        flashingAnimation.setTotalDuration(Duration.millis(1990));
        flashingAnimation.playFromStart();
    }

    private void setMaterialSet(MaterialSet materialSet) {
        dressShape.setMaterial(materialSet.dress);
        pupilsShape.setMaterial(materialSet.pupils);
        eyeballsShape.setMaterial(materialSet.eyeballs);
    }

    public void setNormalLook() {
        flashingAnimation.stop();
        dressAnimation.playOrContinue();
        dressShape.setVisible(true);
        setMaterialSet(normalMaterialSet);
    }

    public void setFrightenedLook() {
        flashingAnimation.stop();
        dressAnimation.playOrContinue();
        dressShape.setVisible(true);
        setMaterialSet(frightenedMaterialSet);
    }

    public void setEyesOnlyLook() {
        flashingAnimation.stop();
        dressAnimation.pause();
        dressShape.setVisible(false);
        setMaterialSet(normalMaterialSet);
    }

    @Override
    public void dispose() {
        getChildren().clear();

        if (dressShape != null) {
            dressShape.setMesh(null);
            dressShape.materialProperty().unbind();
            dressShape.setMaterial(null);
            dressShape = null;
        }

        if (pupilsShape != null) {
            pupilsShape.setMesh(null);
            pupilsShape.materialProperty().unbind();
            pupilsShape.setMaterial(null);
            pupilsShape = null;
        }

        if (eyeballsShape != null) {
            eyeballsShape.setMesh(null);
            eyeballsShape.materialProperty().unbind();
            eyeballsShape.setMaterial(null);
            eyeballsShape = null;

        }

        if (dressGroup != null) {
            dressGroup.getChildren().clear();
            dressGroup = null;
        }

        if (dressAnimation != null) {
            dressAnimation.dispose();
            dressAnimation = null;

        }

        if (flashingAnimation != null) {
            flashingAnimation.dispose();
            flashingAnimation = null;
        }

        normalMaterialSet = null;
        frightenedMaterialSet = null;
        flashingMaterialSet = null;
    }
}