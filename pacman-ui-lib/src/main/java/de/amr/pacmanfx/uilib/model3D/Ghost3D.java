/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * 3D representation of a ghost.
 */
public class Ghost3D extends Group implements Disposable {

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
                    new KeyValue(dressColorProperty,  coloring.flashingDressColor()),
                    new KeyValue(pupilsColorProperty, coloring.flashingPupilsColor())
                ),
                new KeyFrame(flashEndTime,
                    new KeyValue(dressColorProperty,  coloring.frightenedDressColor()),
                    new KeyValue(pupilsColorProperty, coloring.frightenedPupilsColor())
                )
            );
            flashingTimeline.setCycleCount(numFlashes);
            flashingTimeline.setOnFinished(e -> {
                dressColorProperty.set(coloring.frightenedDressColor());
                pupilsColorProperty.set(coloring.frightenedPupilsColor());
            });
            return flashingTimeline;
        }
    }

    private final ObjectProperty<Color> dressColorProperty = new SimpleObjectProperty<>(Color.ORANGE);
    private final ObjectProperty<Color> eyeballsColorProperty = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> pupilsColorProperty = new SimpleObjectProperty<>(Color.BLUE);

    private PhongMaterial dressMaterialNormal;
    private PhongMaterial pupilsMaterialNormal;
    private PhongMaterial eyeballsMaterialNormal;

    private PhongMaterial dressMaterialFrightened;
    private PhongMaterial pupilsMaterialFrightened;
    private PhongMaterial eyeballsMaterialFrightened;

    private PhongMaterial dressMaterialFlashing;
    private PhongMaterial pupilsMaterialFlashing;

    private final ObjectProperty<PhongMaterial> dressMaterial = new SimpleObjectProperty<>();
    private final ObjectProperty<PhongMaterial> pupilsMaterial = new SimpleObjectProperty<>();
    private final ObjectProperty<PhongMaterial> eyeballsMaterial = new SimpleObjectProperty<>();

    private MeshView dressShape;
    private MeshView pupilsShape;
    private MeshView eyeballsShape;

    private Group dressGroup;

    private RegisteredAnimation dressAnimation;
    private FlashingAnimation flashingAnimation;

    private final GhostColoring coloring;

    public Ghost3D(
        AnimationRegistry animationRegistry,
        Ghost ghost,
        GhostColoring coloring,
        MeshView dressShape,
        MeshView pupilsShape,
        MeshView eyeballsShape,
        double size)
    {
        requireNonNull(ghost);

        this.dressShape = requireNonNull(dressShape);
        this.pupilsShape = requireNonNull(pupilsShape);
        this.eyeballsShape = requireNonNull(eyeballsShape);

        this.coloring = coloring;
        requireNonNull(pupilsShape);
        requireNonNull(eyeballsShape);
        requireNonNegative(size);

        dressMaterialNormal = Ufx.coloredPhongMaterial(coloring.normalDressColor());
        eyeballsMaterialNormal = Ufx.coloredPhongMaterial(coloring.normalEyeballsColor());
        pupilsMaterialNormal = Ufx.coloredPhongMaterial(coloring.normalPupilsColor());

        dressMaterialFrightened = Ufx.coloredPhongMaterial(coloring.frightenedDressColor());
        eyeballsMaterialFrightened = Ufx.coloredPhongMaterial(coloring.frightenedEyeballsColor());
        pupilsMaterialFrightened = Ufx.coloredPhongMaterial(coloring.frightenedPupilsColor());

        dressMaterialFlashing = Ufx.coloredPhongMaterial(coloring.flashingDressColor());
        dressMaterialFlashing.diffuseColorProperty().bind(dressColorProperty);
        pupilsMaterialFlashing = Ufx.coloredPhongMaterial(coloring.flashingPupilsColor());
        pupilsMaterialFlashing.diffuseColorProperty().bind(pupilsColorProperty);

        dressColorProperty.set(coloring.normalDressColor());
        dressMaterial.bind(dressColorProperty.map(dressColor -> {
            if (dressColor.equals(coloring.normalDressColor())) {
                return dressMaterialNormal;
            }
            if (dressColor.equals(coloring.frightenedDressColor())) {
                return dressMaterialFrightened;
            }
            return dressMaterialFlashing;
        }));
        dressShape.materialProperty().bind(dressMaterial);

        pupilsColorProperty.set(coloring.normalPupilsColor());
        pupilsMaterial.bind(pupilsColorProperty.map(pupilsColor -> {
            if (pupilsColor.equals(coloring.normalPupilsColor())) {
                return pupilsMaterialNormal;
            }
            if (pupilsColor.equals(coloring.frightenedPupilsColor())) {
                return pupilsMaterialFrightened;
            }
            return pupilsMaterialFlashing;
        }));
        pupilsShape.materialProperty().bind(pupilsMaterial);

        eyeballsColorProperty.set(coloring.normalEyeballsColor());
        eyeballsMaterial.bind(eyeballsColorProperty.map(eyeballsColor -> {
            if (eyeballsColor.equals(coloring.normalEyeballsColor())) {
                return eyeballsMaterialNormal;
            }
            if (eyeballsColor.equals(coloring.frightenedEyeballsColor())) {
                return eyeballsMaterialFrightened;
            }
            // TODO: check this
            return eyeballsMaterialFrightened;
        }));
        eyeballsShape.materialProperty().bind(eyeballsMaterial);

        var eyesGroup = new Group(pupilsShape, eyeballsShape);
        dressGroup = new Group(dressShape);

        getChildren().setAll(dressGroup, eyesGroup);

        Bounds dressBounds = dressShape.getBoundsInLocal();
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
        dressColorProperty.unbind();
        eyeballsColorProperty.unbind();
        pupilsColorProperty.unbind();
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

        dressMaterial.unbind();
        dressMaterialNormal = null;
        dressMaterialFrightened = null;
        dressMaterialFlashing = null;

        eyeballsMaterial.unbind();
        eyeballsMaterialNormal = null;
        eyeballsMaterialFrightened = null;

        pupilsMaterial.unbind();
        pupilsMaterialNormal = null;
        pupilsMaterialFrightened = null;
        pupilsMaterialFlashing = null;
    }

    public PhongMaterial dressMaterialNormal() {
        return dressMaterialNormal;
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
        } else {
            dressShape.setVisible(true);
            flashingAnimation.setNumFlashes(numFlashes);
            // TODO (fixme): Total flashing time must be shorter than Pac power fading time (2s)!
            flashingAnimation.setTotalDuration(Duration.millis(1966));
            flashingAnimation.playFromStart();
        }
    }

    public void setFrightenedLook() {
        flashingAnimation.stop();
        dressAnimation.playOrContinue();
        dressColorProperty.set(coloring.frightenedDressColor());
        eyeballsColorProperty.set(coloring.frightenedEyeballsColor());
        pupilsColorProperty.set(coloring.frightenedPupilsColor());
        dressShape.setVisible(true);
    }

    public void setNormalLook() {
        flashingAnimation.stop();
        dressAnimation.playOrContinue();
        dressColorProperty.set(coloring.normalDressColor());
        eyeballsColorProperty.set(coloring.normalEyeballsColor());
        pupilsColorProperty.set(coloring.normalPupilsColor());
        dressShape.setVisible(true);
    }

    public void setEyesOnlyLook() {
        flashingAnimation.stop();
        dressAnimation.pause();
        eyeballsColorProperty.set(coloring.normalEyeballsColor());
        pupilsColorProperty.set(coloring.normalPupilsColor());
        dressShape.setVisible(false);
    }
}