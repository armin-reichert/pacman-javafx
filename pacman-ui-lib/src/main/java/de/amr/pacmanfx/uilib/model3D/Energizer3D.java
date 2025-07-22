/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Destroyable;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.SquirtingAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import java.util.function.Predicate;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * 3D energizer pellet.
 */
public class Energizer3D implements Eatable3D, Destroyable {

    public class Explosion extends ManagedAnimation {

        private static final byte MIN_PARTICLE_COUNT = 42;
        private static final byte MAX_PARTICLE_COUNT = 100;
        private static final Duration DURATION = Duration.seconds(6);

        private final Group particlesGroupContainer;
        private final Predicate<SquirtingAnimation.Particle> particleReachedEndPosition;

        public Explosion(AnimationManager animationManager, Group particlesGroupContainer,
                         Predicate<SquirtingAnimation.Particle> particleReachedEndPosition) {
            super(animationManager, "Energizer_Explosion");
            this.particlesGroupContainer = requireNonNull(particlesGroupContainer);
            this.particleReachedEndPosition = requireNonNull(particleReachedEndPosition);
        }

        @Override
        protected Animation createAnimation() {
            return new SquirtingAnimation(DURATION, MIN_PARTICLE_COUNT, MAX_PARTICLE_COUNT, sphere.getMaterial(), location()) {
                @Override
                public boolean particleReachedEndPosition(Particle p) { return particleReachedEndPosition.test(p); }
            };
        }

        @Override
        public void playFromStart() {
            super.playFromStart();
            if (animation instanceof SquirtingAnimation squirtingAnimation) {
                particlesGroupContainer.getChildren().add(squirtingAnimation.particlesGroup());
            }
        }

        @Override
        public void stop() {
            super.stop();
            if (animation instanceof SquirtingAnimation squirtingAnimation) {
                particlesGroupContainer.getChildren().remove(squirtingAnimation.particlesGroup());
            }
        }

        @Override
        public void destroy() {
            if (animation instanceof SquirtingAnimation squirtingAnimation) {
                particlesGroupContainer.getChildren().remove(squirtingAnimation.particlesGroup());
                squirtingAnimation.destroy();
            }
            super.destroy();
        }
    }

    private final Sphere sphere;

    private ManagedAnimation pumpingAnimation;
    private ManagedAnimation eatenAnimation;

    public Energizer3D(AnimationManager animationManager, double radius, double minScaling, double maxScaling) {
        requireNonNegative(radius, "Energizer radius must be positive but is %f");
        requireNonNull(animationManager);

        sphere = new Sphere(radius);

        pumpingAnimation = new ManagedAnimation(animationManager, "Energizer_Pumping") {
            @Override
            protected Animation createAnimation() {
                // 3 full blinks per second
                var scaleTransition = new ScaleTransition(Duration.millis(166.6), sphere);
                scaleTransition.setAutoReverse(true);
                scaleTransition.setCycleCount(Animation.INDEFINITE);
                scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
                scaleTransition.setFromX(maxScaling);
                scaleTransition.setFromY(maxScaling);
                scaleTransition.setFromZ(maxScaling);
                scaleTransition.setToX(minScaling);
                scaleTransition.setToY(minScaling);
                scaleTransition.setToZ(minScaling);
                return scaleTransition;
            }
        };
    }

    @Override
    public void destroy() {
        if (pumpingAnimation != null) {
            pumpingAnimation.stop();
            pumpingAnimation.destroy();
            pumpingAnimation = null;
        }
        if (eatenAnimation != null) {
            eatenAnimation.stop();
            eatenAnimation.destroy();
            eatenAnimation = null;
        }
    }

    public void pump() {
        pumpingAnimation.playOrContinue();
    }

    public void noPumping() {
        pumpingAnimation.pause();
    }

    public void setEatenAnimation(ManagedAnimation animation) {
        eatenAnimation = requireNonNull(animation);
    }

    public Point3D location() {
        return new Point3D(sphere.getTranslateX(), sphere.getTranslateY(), sphere.getTranslateZ());
    }

    @Override
    public Shape3D shape3D() {
        return sphere;
    }

    @Override
    public void onEaten() {
        pumpingAnimation.stop();
        sphere.setVisible(false);
        if (eatenAnimation != null) {
            eatenAnimation.playFromStart();
        }
    }
}