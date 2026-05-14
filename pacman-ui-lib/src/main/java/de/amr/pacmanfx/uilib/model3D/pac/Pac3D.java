/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.basics.math.Vector2f;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.Model3DHelper;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.model3D.animation.HeadBangingAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.PacChewingAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.PacManDyingAnimation3D;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.Ufx.coloredPhongMaterial;
import static de.amr.pacmanfx.uilib.model3D.Model3DHelper.centerOverOrigin;
import static de.amr.pacmanfx.uilib.model3D.Model3DHelper.scale;
import static java.util.Objects.requireNonNull;

/**
 * (Ms.) Pac-Man 3D representations.
 */
public class Pac3D extends Group
    implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID { PAC_CHEWING, PAC_DYING, PAC_MOVING }

    public static abstract class MovementAnimation extends ManagedAnimation {
        public MovementAnimation(String label) {
            super(label);
        }

        public abstract void update(Pac pac);

        public abstract void setPowerMode(boolean power);
    }

    public static Pac3D createPacMan3D(ManagedAnimationsRegistry animations, Pac pac, PacConfig config) {
        final Pac3D pac3D = new Pac3D(animations, pac);
        pac3D.setBody(createPacBody(config));
        pac3D.setJaw(createBlindPacBody(config));
        animations.register(AnimationID.PAC_CHEWING, new PacChewingAnimation3D(pac3D));
        animations.register(AnimationID.PAC_DYING,   new PacManDyingAnimation3D(pac3D));
        animations.register(AnimationID.PAC_MOVING,  new HeadBangingAnimation3D(pac3D));
        pac3D.setMovementAnimationPowerMode(false);
        return pac3D;
    }

    /**
     * Creates a fully assembled Pac-Man body with head, eyes, and palate.
     *
     * @param config the Pac configuration
     * @return a new Pac body group
     */
    public static Group createPacBody(PacConfig config) {
        requireNonNull(config);
        final MeshView head = createPacHead(config);
        final MeshView eyes = createPacEyes(config);
        final MeshView palate = createPacPalate(config);
        final Group body = new Group(head, eyes, palate);
        centerOverOrigin(head, List.of(eyes, palate));
        scale(body, config.size3D());
        body.getTransforms().add(PacManWorld3D.ORIENTATION_ADJUSTMENT);
        return body;
    }

    /**
     * Creates a Pac-Man body without eyes (used for jaw open/close animation).
     *
     * @param config the Pac configuration
     * @return a Pac body without eyes
     */
    public static Group createBlindPacBody(PacConfig config) {
        requireNonNull(config);
        final MeshView head = createPacHead(config);
        final MeshView palate = createPacPalate(config);
        final Group body = new Group(head, palate);
        centerOverOrigin(head, List.of(palate));
        scale(body, config.size3D());
        body.getTransforms().add(PacManWorld3D.ORIENTATION_ADJUSTMENT);
        return body;
    }

    public static MeshView createPacHead(PacConfig config) {
        final PhongMaterial boringMaterial = coloredPhongMaterial(config.colors().headColor());
        return Model3DHelper.createMeshView(PacManWorld3D.instance().pacHeadMesh(), boringMaterial);
    }

    public static MeshView createPacPalate(PacConfig config) {
        return Model3DHelper.createMeshView(
            PacManWorld3D.instance().pacPalateMesh(),
            coloredPhongMaterial(config.colors().palateColor()));
    }

    public static MeshView createPacEyes(PacConfig config) {
        return Model3DHelper.createMeshView(
            PacManWorld3D.instance().pacEyesMesh(),
            coloredPhongMaterial(config.colors().eyesColor()));
    }

    protected final Pac pac;
    protected final ManagedAnimationsRegistry animations;

    protected PointLight powerLight;

    protected Group body;
    protected Group jaw;

    protected Rotate moveRotation = new Rotate();

    public Group jaw() {
        return jaw;
    }

    protected Pac3D(ManagedAnimationsRegistry animations, Pac pac) {
        this.animations = requireNonNull(animations);
        this.pac = requireNonNull(pac);
        getTransforms().add(moveRotation);
    }

    public void setBody(Group body) {
        this.body = requireNonNull(body);
        if (jaw == null) {
            getChildren().setAll(body);
        } else {
            getChildren().setAll(body, jaw);
        }
    }

    public void setJaw(Group jaw) {
        this.jaw = requireNonNull(jaw);
        if (body == null) {
            getChildren().setAll(jaw);
        } else {
            getChildren().setAll(body, jaw);
        }
    }

    @Override
    public void dispose() {
        for (var animID : AnimationID.values()) {
            animations.optAnimation(animID).ifPresent(ManagedAnimation::dispose);
        }
        cleanupLight(powerLight);
        cleanupGroup(this, true);
    }

    @Override
    public void init(GameLevel level) {
        requireNonNull(level);
        stopAnimations();
        setScaleX(1.0);
        setScaleY(1.0);
        setScaleZ(1.0);
        updatePositionAndRotation();
        updateVisibility(level.worldMap());
        setMovementAnimationPowerMode(false);
    }

    @Override
    public void update(GameLevel level) {
        requireNonNull(level);
        if (pac.isAlive()) {
            updatePositionAndRotation();
            updateVisibility(level.worldMap());
            updatePowerLight();
            animations.optAnimation(AnimationID.PAC_MOVING).ifPresent(movementAnimation -> {
                movementAnimation.playOrContinue();
                updateMovementAnimation();
            });
            animations.optAnimation(AnimationID.PAC_CHEWING).ifPresent(chewingAnimation -> {
                if (pac.isParalyzed()) {
                    chewingAnimation.stop();
                } else {
                    chewingAnimation.playOrContinue();
                }
            });
        } else {
            stopMovementAnimation();
            stopChewingAnimation();
        }
    }

    public Optional<PointLight> powerLight() {
        return Optional.ofNullable(powerLight);
    }

    public void setMovementAnimationPowerMode(boolean power) {
        animations.optAnimation(AnimationID.PAC_MOVING, MovementAnimation.class)
            .ifPresent(movement -> movement.setPowerMode(power));
    }

    public void updateMovementAnimation() {
        animations.optAnimation(AnimationID.PAC_MOVING, MovementAnimation.class)
            .ifPresent(movement -> movement.update(pac));
    }

    protected void stopChewingAnimation() {
        animations.optAnimation(AnimationID.PAC_CHEWING).ifPresent(ManagedAnimation::stop);
    }

    protected void stopMovementAnimation() {
        animations.optAnimation(AnimationID.PAC_MOVING).ifPresent(ManagedAnimation::stop);
    }

    protected void stopDyingAnimation() {
        animations.optAnimation(AnimationID.PAC_DYING).ifPresent(ManagedAnimation::stop);
    }

    protected void stopAnimations() {
        stopChewingAnimation();
        stopMovementAnimation();
        stopDyingAnimation();
    }

    protected void updatePositionAndRotation() {
        final Vector2f center = pac.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        final double angle = switch (pac.moveDir()) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        };
        moveRotation.setAxis(Rotate.Z_AXIS);
        moveRotation.setAngle(angle);
    }

    protected void updateVisibility(WorldMap worldMap) {
        final boolean outsideWorld = getTranslateX() < HTS || getTranslateX() > TS * worldMap.numCols() - HTS;
        setVisible(pac.isVisible() && !outsideWorld);
    }

    public void createPowerLight(PacConfig pacConfig) {
        powerLight = new PointLight();
        powerLight.setColor(pacConfig.colors().headColor().desaturate());
        powerLight.translateXProperty().bind(translateXProperty());
        powerLight.translateYProperty().bind(translateYProperty());
        powerLight.setTranslateZ(-30);
    }

    /**
     * When empowered, Pac-Man is lighted, light range shrinks with ceasing power.
     */
    public void updatePowerLight() {
        if (powerLight == null) return;
        final TickTimer powerTimer = pac.powerTimer();
        if (powerTimer.isRunning() && pac.isVisible() && !pac.isDead()) {
            powerLight.setLightOn(true);
            final long remainingTicks = powerTimer.remainingTicks();
            final float maxRange = (remainingTicks / (float) powerTimer.durationTicks()) * 60 + 30;
            powerLight.setMaxRange(maxRange);
            Logger.debug("Power remaining: {}, light max range: {0.00}", remainingTicks, maxRange);
        } else {
            powerLight.setLightOn(false);
        }
    }
}