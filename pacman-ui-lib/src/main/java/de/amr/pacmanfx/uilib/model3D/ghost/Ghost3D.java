/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostBrakeAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostDressAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.GhostFlashingAnimation3D;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static java.util.Objects.requireNonNull;

/**
 * Represents the different 3D appearances of a ghost. One of:
 * <ul>
 * <li>{@link GhostAppearance#NORMAL}: colored ghost, blue eyes,
 * <li>{@link GhostAppearance#FRIGHTENED}: blue ghost, empty, "pinkish" eyes (looking blind),
 * <li>{@link GhostAppearance#FLASHING}: blue-white flashing skin, pink-red flashing eyes,
 * <li>{@link GhostAppearance#EYES} eyes only,
 * </ul>
 */
public class Ghost3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID {
        GHOST_BRAKING, GHOST_DRESS, GHOST_FLASHING;

        public String forGhost(Ghost ghost) {
            requireNonNull(ghost);
            return "%s_%d".formatted(name(), ghost.personality());
        }
    }

    private static final double HEIGHT_OVER_FLOOR = 2.0;

    private final ManagedAnimationsRegistry animations;

    private final Ghost ghost;

    private final GhostAppearanceColors colors;
    private GhostMaterialSet materialSet;
    private final double size;

    private Group dressGroup;
    private MeshView dressShape;
    private MeshView pupilsShape;
    private MeshView eyeballsShape;

    private final Rotate facing = new Rotate(0, Rotate.Z_AXIS);
    private final Scale scaling = new Scale(1, 1, 1);

    private int numFlashes;

    private ChangeListener<Vector2f> positionChangeListener = (_, _, _) -> updateTransform(ghost());
    private ChangeListener<Direction> wishDirChangeListener = (_, _, _) -> updateTransform(ghost());

    public Ghost3D(
        ManagedAnimationsRegistry animations,
        Ghost ghost,
        GhostAppearanceColors colors,
        GhostMeshSet meshSet,
        GhostMaterialSet materialSet,
        double size)
    {
        this.animations = requireNonNull(animations);
        this.ghost = requireNonNull(ghost);
        this.colors = requireNonNull(colors);

        requireNonNull(meshSet);
        this.dressShape    = new MeshView(meshSet.dress());
        this.pupilsShape   = new MeshView(meshSet.pupils());
        this.eyeballsShape = new MeshView(meshSet.eyeballs());

        this.materialSet = requireNonNull(materialSet);
        this.size = requireNonNegative(size);

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

        setSize(size);

        animations.register(AnimationID.GHOST_DRESS.forGhost(ghost),    new GhostDressAnimation3D(ghost, dressGroup));
        animations.register(AnimationID.GHOST_FLASHING.forGhost(ghost), new GhostFlashingAnimation3D(ghost, materialSet, colors));
        animations.register(AnimationID.GHOST_BRAKING.forGhost(ghost), new GhostBrakeAnimation3D(this));

        getTransforms().addAll(scaling, facing, PacManWorld3D.ORIENTATION_ADJUSTMENT);
        updateTransform(ghost);

        addPropertyChangeListeners();

        setGhostAppearance(GhostAppearance.NORMAL);
    }

    public void setSize(double size) {
        final Bounds b = getBoundsInLocal();
        scaling.setX(size / b.getWidth());
        scaling.setY(size / b.getHeight());
        scaling.setZ(size / b.getDepth());
    }

    public void setFlashingLook(int numFlashes) {
        if (numFlashes == 0) {
            setFrightenedLook();
            return;
        }
        setMaterialSet(materialSet.flashingMaterial());
        dressShape.setVisible(true);

        animations.optAnimation(AnimationID.GHOST_FLASHING.forGhost(ghost), GhostFlashingAnimation3D.class).ifPresent(flashing -> {
            // TODO: this is crap
            if (flashing.numFlashes() != numFlashes) {
                flashing.stop();
                flashing.setNumFlashes(numFlashes);
                flashing.setTotalDuration(Duration.millis(1990));
            }
            flashing.playOrContinue();
        });
    }

    public void turnTowards(Direction dir) {
        requireNonNull(dir);
        facing.setAngle(switch (dir) {
            case LEFT  -> 0;
            case UP    -> 90;
            case RIGHT -> 180;
            case DOWN  -> 270;
        });
    }

    private void setMaterialSet(GhostComponentMaterialSet materialSet) {
        dressShape.setMaterial(materialSet.dressMaterial());
        pupilsShape.setMaterial(materialSet.pupilsMaterial());
        eyeballsShape.setMaterial(materialSet.eyeballsMaterial());
    }

    public void setNormalLook() {
        animations.animation(AnimationID.GHOST_FLASHING.forGhost(ghost)).stop();
        animations.animation(AnimationID.GHOST_DRESS.forGhost(ghost)).playOrContinue();
        dressShape.setVisible(true);
        setMaterialSet(materialSet.normalMaterial());
    }

    public void setFrightenedLook() {
        animations.animation(AnimationID.GHOST_FLASHING.forGhost(ghost)).stop();
        animations.animation(AnimationID.GHOST_DRESS.forGhost(ghost)).playOrContinue();
        dressShape.setVisible(true);
        setMaterialSet(materialSet.frightenedMaterial());
    }

    public void setEyesOnlyLook() {
        animations.animation(AnimationID.GHOST_FLASHING.forGhost(ghost)).stop();
        animations.animation(AnimationID.GHOST_DRESS.forGhost(ghost)).stop();
        dressShape.setVisible(false);
        setMaterialSet(materialSet.normalMaterial());
    }

    public void animateDress(boolean on) {
        animations.optAnimation(AnimationID.GHOST_DRESS.forGhost(ghost)).ifPresent(dressAnimation -> {
            if (on) {
                dressAnimation.playOrContinue();
            } else {
                dressAnimation.stop();
            }
        });
    }

    @Override
    public void dispose() {
        removePropertyChangeListeners();
        positionChangeListener = null;
        wishDirChangeListener = null;
        stopAllAnimations();
        animations.optAnimation(AnimationID.GHOST_BRAKING.forGhost(ghost())).ifPresent(ManagedAnimation::dispose);
        cleanupGroup(this, true);

        animations.optAnimation(AnimationID.GHOST_DRESS.forGhost(ghost)).ifPresent(ManagedAnimation::dispose);
        animations.optAnimation(AnimationID.GHOST_FLASHING.forGhost(ghost)).ifPresent(ManagedAnimation::dispose);
        cleanupGroup(this, true);
        materialSet = null;
        dressShape = null;
        pupilsShape = null;
        eyeballsShape = null;
        dressGroup = null;
    }

    @Override
    public void init(GameLevel level) {
        stopAllAnimations();
        updateTransform(ghost());
        setGhostAppearance(GhostAppearance.NORMAL);
    }

    @Override
    public void update(GameLevel level) {
        updateVisibility(level.worldMap());
        updateTransform(ghost());
        updateAppearance(level);
        if (ghost().moveInfo().tunnelEntered) {
            animations.animation(AnimationID.GHOST_BRAKING.forGhost(ghost())).playFromStart();
        }
        animateDress(isVisible());
    }

    private void updateVisibility(WorldMap worldMap) {
        final boolean outsideWorld = getTranslateX() < HTS || getTranslateX() > TS * worldMap.numCols() - HTS;
        setVisible(ghost.isVisible() && !outsideWorld);
    }

    public Ghost ghost() {
        return ghost;
    }

    public GhostAppearanceColors colors() {
        return colors;
    }

    public GhostMaterialSet materials() {
        return materialSet;
    }

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = requireNonNegativeInt(numFlashes);
    }

    public void stopAllAnimations() {
        for (AnimationID animationID : AnimationID.values()) {
            animations.optAnimation(animationID.forGhost(ghost)).ifPresent(ManagedAnimation::stop);
        }
    }

    // private area, no trespassing

    private void setGhostAppearance(GhostAppearance ghostAppearance) {
        switch (ghostAppearance) {
            case NORMAL     -> setNormalLook();
            case FRIGHTENED -> setFrightenedLook();
            case EYES       -> setEyesOnlyLook();
            case FLASHING   -> setFlashingLook(numFlashes);
        }
        animateDress(true);

        Logger.debug("Ghost appearance for {} is now {}", ghost.name(), ghostAppearance);
    }

    private void addPropertyChangeListeners() {
        ghost().positionProperty().addListener(positionChangeListener);
        ghost().wishDirProperty().addListener(wishDirChangeListener);
    }

    private void removePropertyChangeListeners() {
        ghost().positionProperty().removeListener(positionChangeListener);
        ghost().wishDirProperty().removeListener(wishDirChangeListener);
    }

    private void updateTransform(Ghost ghost) {
        final Vector2f center = ghost.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(- (0.5 * size + HEIGHT_OVER_FLOOR));
        turnTowards(ghost.wishDir());
    }

    private void updateAppearance(GameLevel level) {
        final GhostState ghostState = ghost().state();

        // Let ghost shown as number alone
        if (ghostState == GhostState.EATEN) return;

        final boolean powerActive = level.pac().powerTimer().isRunning();
        final boolean powerFading = level.pac().isPowerFading(level);
        // ghosts that already got killed in the current power phase do not look frightened anymore
        final boolean killedAlready = level.energizerVictims().contains(ghost());

        setGhostAppearance(switch (ghostState) {
            case LOCKED, LEAVING_HOUSE -> powerActive && !killedAlready
                ? frightenedAppearance(powerFading)
                : GhostAppearance.NORMAL;
            case FRIGHTENED -> frightenedAppearance(powerFading);
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            default -> GhostAppearance.NORMAL;
        });
    }

    private GhostAppearance frightenedAppearance(boolean powerFading) {
        return powerFading ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
    }
}