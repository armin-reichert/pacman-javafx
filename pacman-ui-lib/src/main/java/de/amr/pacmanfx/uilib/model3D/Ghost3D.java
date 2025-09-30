/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
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
import static de.amr.pacmanfx.uilib.Ufx.defaultPhongMaterial;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of a ghost.
 */
public class Ghost3D extends Group implements Disposable {

    public record MaterialSet(PhongMaterial dress, PhongMaterial eyeballs, PhongMaterial pupils) {}

    public class FlashingAnimation extends RegisteredAnimation {
        private Duration totalDuration = Duration.seconds(3);
        private int numFlashes = 5;

        public FlashingAnimation(AnimationRegistry animationRegistry, String ghostName) {
            super(animationRegistry, "Ghost_Flashing_%s".formatted(ghostName));
        }

        public void setTotalDuration(Duration totalDuration) {
            this.totalDuration = requireNonNull(totalDuration);
            invalidate();
        }

        public void setNumFlashes(int numFlashes) {
            this.numFlashes = Validations.requireNonNegativeInt(numFlashes);
            invalidate();
        }

        @Override
        protected Animation createAnimationFX() {
            Duration flashEndTime = totalDuration.divide(numFlashes), highlightTime = flashEndTime.divide(3);
            var flashingTimeline = new Timeline(
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
            flashingTimeline.setOnFinished(e -> {
                flashingMaterialSet.dress().setDiffuseColor(colorSet.frightened().dress());
                flashingMaterialSet.dress().setSpecularColor(colorSet.frightened().dress().brighter());
                flashingMaterialSet.pupils().setDiffuseColor(colorSet.frightened().pupils());
                flashingMaterialSet.pupils().setSpecularColor(colorSet.frightened().pupils().brighter());
            });
            return flashingTimeline;
        }
    }

    private MaterialSet normalMaterialSet;
    private MaterialSet frightenedMaterialSet;
    private MaterialSet flashingMaterialSet;

    private MeshView dressShape;
    private MeshView pupilsShape;
    private MeshView eyeballsShape;

    private Group dressGroup;

    private RegisteredAnimation dressAnimation;
    private FlashingAnimation flashingAnimation;

    private final GhostColorSet colorSet;

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
        requireNonNull(ghost);
        this.colorSet      = requireNonNull(colorSet);
        this.dressShape    = requireNonNull(dressShape);
        this.pupilsShape   = requireNonNull(pupilsShape);
        this.eyeballsShape = requireNonNull(eyeballsShape);
        requireNonNegative(size);

        normalMaterialSet = new MaterialSet(
            defaultPhongMaterial(colorSet.normal().dress()),
            defaultPhongMaterial(colorSet.normal().eyeballs()),
            defaultPhongMaterial(colorSet.normal().pupils())
        );

        frightenedMaterialSet = new MaterialSet(
            defaultPhongMaterial(colorSet.frightened().dress()),
            defaultPhongMaterial(colorSet.frightened().eyeballs()),
            defaultPhongMaterial(colorSet.frightened().pupils())
        );

        flashingMaterialSet = new MaterialSet(
            defaultPhongMaterial(colorSet.flashing().dress()),
            defaultPhongMaterial(colorSet.flashing().eyeballs()),
            defaultPhongMaterial(colorSet.flashing().pupils())
        );

        var eyes = new Group(pupilsShape, eyeballsShape);
        dressGroup = new Group(dressShape);

        getChildren().setAll(dressGroup, eyes);

        Bounds dressBounds = dressShape.getBoundsInLocal();
        var centeredOverOrigin = new Translate(
            -dressBounds.getCenterX(),
            -dressBounds.getCenterY(),
            -dressBounds.getCenterZ()
        );
        dressShape.getTransforms().add(centeredOverOrigin);
        eyes.getTransforms().add(centeredOverOrigin);

        // TODO: change orientation in OBJ file?
        getTransforms().addAll(
            new Rotate(90, Rotate.X_AXIS),
            new Rotate(180, Rotate.Y_AXIS),
            new Rotate(180, Rotate.Z_AXIS)
        );

        Bounds bounds = getBoundsInLocal();
        Scale scale = new Scale(
            size / bounds.getWidth(),
            size / bounds.getHeight(),
            size / bounds.getDepth()
        );
        getTransforms().add(scale);

        dressAnimation = new RegisteredAnimation(animationRegistry, "Ghost_DressMoving_%s".formatted(ghost.name())) {
            @Override
            protected Animation createAnimationFX() {
                var animation = new RotateTransition(Duration.seconds(0.3), dressGroup);
                // TODO I expected this should be the z-axis but... (transforms messed-up?)
                animation.setAxis(Rotate.Y_AXIS);
                animation.setByAngle(30);
                animation.setCycleCount(Animation.INDEFINITE);
                animation.setAutoReverse(true);
                return animation;
            }
        };

        flashingAnimation = new FlashingAnimation(animationRegistry, ghost.name());
    }

    @Override
    public void dispose() {
        getChildren().clear();

        dressShape.setMesh(null);
        dressShape.materialProperty().unbind();
        dressShape.setMaterial(null);
        dressShape = null;

        pupilsShape.setMesh(null);
        pupilsShape.materialProperty().unbind();
        pupilsShape.setMaterial(null);
        pupilsShape = null;

        eyeballsShape.setMesh(null);
        eyeballsShape.materialProperty().unbind();
        eyeballsShape.setMaterial(null);
        eyeballsShape = null;

        dressGroup.getChildren().clear();
        dressGroup = null;

        dressAnimation.dispose();
        dressAnimation = null;
        flashingAnimation.dispose();
        flashingAnimation = null;

        normalMaterialSet = null;
        frightenedMaterialSet = null;
        flashingMaterialSet = null;
    }

    public MaterialSet normalMaterialSet() {
        return normalMaterialSet;
    }

    public RegisteredAnimation dressAnimation() {
        return dressAnimation;
    }

    public FlashingAnimation flashingAnimation() {
        return flashingAnimation;
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
}