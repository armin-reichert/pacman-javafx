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
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
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

    public class FlashingAnimation extends ManagedAnimation {
        private Duration totalDuration = Duration.seconds(3);
        private int numFlashes = 5;

        public FlashingAnimation(AnimationManager animationManager, String ghostName) {
            super(animationManager, "Ghost_Flashing_%s".formatted(ghostName));
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
        protected Animation createAnimation() {
            Duration flashEndTime = totalDuration.divide(numFlashes), highlightTime = flashEndTime.divide(3);
            var flashingTimeline = new Timeline(
                new KeyFrame(highlightTime,
                    new KeyValue(dressColorPy,  coloring.flashingDressColor()),
                    new KeyValue(pupilsColorPy, coloring.flashingPupilsColor())
                ),
                new KeyFrame(flashEndTime,
                    new KeyValue(dressColorPy,  coloring.frightenedDressColor()),
                    new KeyValue(pupilsColorPy, coloring.frightenedPupilsColor())
                )
            );
            flashingTimeline.setCycleCount(numFlashes);
            flashingTimeline.setOnFinished(e -> {
                dressColorPy.set(coloring.frightenedDressColor());
                pupilsColorPy.set(coloring.frightenedPupilsColor());
            });
            return flashingTimeline;
        }
    }

    private final ObjectProperty<Color> dressColorPy    = new SimpleObjectProperty<>(Color.ORANGE);
    private final ObjectProperty<Color> eyeballsColorPy = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> pupilsColorPy   = new SimpleObjectProperty<>(Color.BLUE);

    private MeshView dressShape;
    private MeshView pupilsShape;
    private MeshView eyeballsShape;

    private Group dressGroup;

    private final AnimationManager animationManager;
    private ManagedAnimation dressAnimation;
    private FlashingAnimation flashingAnimation;

    private final GhostColoring coloring;

    public Ghost3D(
        AnimationManager animationManager,
        Ghost ghost,
        GhostColoring coloring,
        MeshView dressShape,
        MeshView pupilsShape,
        MeshView eyeballsShape,
        double size)
    {
        this.animationManager = requireNonNull(animationManager);
        requireNonNull(ghost);

        this.dressShape = requireNonNull(dressShape);
        this.pupilsShape = requireNonNull(pupilsShape);
        this.eyeballsShape = requireNonNull(eyeballsShape);

        this.coloring = coloring;
        requireNonNull(pupilsShape);
        requireNonNull(eyeballsShape);
        requireNonNegative(size);

        dressShape.materialProperty().bind(dressColorPy.map(Ufx::coloredPhongMaterial));
        pupilsShape.materialProperty().bind(pupilsColorPy.map(Ufx::coloredPhongMaterial));
        eyeballsShape.materialProperty().bind(eyeballsColorPy.map(Ufx::coloredPhongMaterial));

        pupilsColorPy.set(coloring.normalPupilsColor());
        dressColorPy.set(coloring.normalDressColor());
        eyeballsColorPy.set(coloring.normalEyeballsColor());

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

        dressAnimation = new ManagedAnimation(animationManager, "Ghost_DressMoving_%s".formatted(ghost.name())) {
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

        flashingAnimation = new FlashingAnimation(animationManager, ghost.name());
    }

    @Override
    public void dispose() {
        dressColorPy.unbind();
        eyeballsColorPy.unbind();
        pupilsColorPy.unbind();
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
    }

    public ManagedAnimation dressAnimation() {
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
        dressColorPy.set(coloring.frightenedDressColor());
        eyeballsColorPy.set(coloring.frightenedEyeballsColor());
        pupilsColorPy.set(coloring.frightenedPupilsColor());
        dressShape.setVisible(true);
    }

    public void setNormalLook() {
        flashingAnimation.stop();
        dressAnimation.playOrContinue();
        dressColorPy.set(coloring.normalDressColor());
        eyeballsColorPy.set(coloring.normalEyeballsColor());
        pupilsColorPy.set(coloring.normalPupilsColor());
        dressShape.setVisible(true);
    }

    public void setEyesOnlyLook() {
        flashingAnimation.stop();
        dressAnimation.pause();
        eyeballsColorPy.set(coloring.normalEyeballsColor());
        pupilsColorPy.set(coloring.normalPupilsColor());
        dressShape.setVisible(false);
    }
}