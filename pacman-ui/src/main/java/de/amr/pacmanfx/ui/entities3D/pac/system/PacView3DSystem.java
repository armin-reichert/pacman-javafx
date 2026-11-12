/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.pac.system;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.actor.pac.PacState;
import de.amr.pacmanfx.core.entities.actor.pac.PacStateComp;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.entities3D.pac.anim.Pac3DMovementAnimation;
import de.amr.pacmanfx.ui.entities3D.pac.comp.Pac3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.pac.comp.PacView3D;
import de.amr.pacmanfx.uilib.view3d.Pac3DShapeFactory;
import de.amr.pacmanfx.uilib.view3d.PacSettings;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import static de.amr.basics.TileDimension.HTS;
import static de.amr.basics.TileDimension.TS;
import static java.util.Objects.requireNonNull;

public class PacView3DSystem {

    public PacView3DSystem() {
    }

    public void initTransform(Pac pac) {
        requireNonNull(pac);

        final PacView3D view3D = pac.assertComponent(PacView3D.class);
        view3D.root().setScaleX(1.0);
        view3D.root().setScaleY(1.0);
        view3D.root().setScaleZ(1.0);

        view3D.root().setVisible(false);

        updateTransform(pac);
    }

    public void updateTransform(Pac pac) {
        requireNonNull(pac);

        final PacView3D view3D = pac.assertComponent(PacView3D.class);
        final Vector2f center = pac.position().bodyCenter();

        final WorldMap worldMap = pac.worldNavigation().worldMap();
        final boolean outsideWorld = center.x() < HTS || center.x() > TS * worldMap.numCols() - HTS;
        view3D.root().setVisible(pac.isVisible() && !outsideWorld);

        view3D.root().setTranslateX(center.x());
        view3D.root().setTranslateY(center.y());
        view3D.root().setTranslateZ(-8); //TODO should depend on size

        final Direction moveDir = pac.worldNavigation().moveDir();
        if (moveDir != null) {
            updateFacing(view3D, moveDir);
        }
    }

    private void updateFacing(PacView3D view3D, Direction dir) {
        final int angle = switch (dir) {
            case LEFT -> 0;
            case UP -> 90;
            case RIGHT -> 180;
            case DOWN -> 270;
        };
        view3D.root().setRotationAxis(Rotate.Z_AXIS);
        view3D.root().setRotate(angle);
    }

    // Animation

    public void stopAnimations(Pac pac) {
        final Pac3DAnimationComp animation = pac.assertComponent(Pac3DAnimationComp.class);
        if (animation.chewing() != null) {
            animation.chewing().stop();
        }
        if (animation.dying() != null) {
            animation.dying().stop();
        }
        if (animation.movement() != null) {
            animation.movement().managedAnimation().stop();
        }
    }

    public void updateAnimations(Pac pac) {
        final Pac3DAnimationComp animation = pac.assertComponent(Pac3DAnimationComp.class);

        final Pac3DMovementAnimation movementAnimation = animation.movement();
        if (movementAnimation != null) {
            movementAnimation.update();
        }

        if (animation.chewing() != null) {
            final boolean moved = pac.worldNavigation().info().moved;
            if (moved) {
                animation.chewing().playOrContinue();
            } else {
                animation.chewing().stop();
            }
        }

        updatePowerLightAnimation(pac);
    }

    public void setPowerModeAnimation(Pac pac, boolean power) {
        final Pac3DAnimationComp animation = pac.assertComponent(Pac3DAnimationComp.class);
        final Pac3DMovementAnimation movementAnimation = animation.movement();
        if (movementAnimation != null) {
            movementAnimation.setPowerMode(power);
        }
    }

    public void playDyingAnimation(
        Pac pac,
        Runnable pacDeadSoundEffect,
        Runnable onFinishedCallback) {
        final Pac3DAnimationComp pacAnimation = pac.assertComponent(Pac3DAnimationComp.class);

        final Animation animation = new SequentialTransition(
            Ufx.pauseSecThen(1.5, pacDeadSoundEffect),
            pacAnimation.dying().delegate(),
            Ufx.pauseSec(0.5)
        );
        animation.setOnFinished(_ -> onFinishedCallback.run());

        if (pacAnimation.chewing() != null) {
            pacAnimation.chewing().stop();
        }
        if (pacAnimation.movement() != null) {
            pacAnimation.movement().managedAnimation().stop();
        }
        animation.play();
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    private void updatePowerLightAnimation(Pac pac) {
        final PacStateComp state = pac.state();
        final PacView3D view3D = pac.assertComponent(PacView3D.class);

        final boolean lighted = state.enumValue() != PacState.DEAD;
        if (lighted) {
            final boolean powerActive      = pac.power().isActive();
            final long powerTicksRemaining = pac.power().ticksRemaining();
            final long powerTicksTotal     = pac.power().ticksTotal();
            if (powerActive && pac.isVisible()) {
                view3D.powerLight().setLightOn(true);
                final float maxRange = (powerTicksRemaining / (float) powerTicksTotal) * 60 + 30;
                view3D.powerLight().setMaxRange(maxRange);
            } else {
                view3D.powerLight().setLightOn(false);
            }
        }
    }

    // View 3D

    public PacView3D createPacManView3D(Pac3DShapeFactory shapeFactory, Pac pac, PacSettings config) {
        final PacView3D view3D = new PacView3D();
        view3D.setBodyAndJaw(shapeFactory.createPacBody(config, true), shapeFactory.createPacBody(config, false));
        configurePowerLight(view3D, config.colors().headColor().desaturate());
        view3D.root().setVisible(pac.isVisible());
        return view3D;
    }

    public PacView3D createMsPacManView3D(Pac3DShapeFactory shapeFactory, Pac msPacMan, PacSettings config) {
        final PacView3D view3D = new PacView3D();
        view3D.setBodyAndJaw(shapeFactory.createPacBody(config, true), shapeFactory.createPacBody(config, false));
        view3D.bodyGroup().getChildren().add(shapeFactory.createFemalePacBodyParts(config));
        configurePowerLight(view3D, config.colors().headColor().desaturate());
        view3D.root().setVisible(msPacMan.isVisible());
        return view3D;
    }

    private void configurePowerLight(PacView3D view3D, Color color) {
        final PointLight powerLight = view3D.powerLight();
        powerLight.setColor(color);
        powerLight.translateXProperty().bind(view3D.root().translateXProperty());
        powerLight.translateYProperty().bind(view3D.root().translateYProperty());
        powerLight.setTranslateZ(-30);
    }
}
