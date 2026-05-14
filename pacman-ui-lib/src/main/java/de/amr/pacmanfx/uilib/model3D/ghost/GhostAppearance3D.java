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
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.animation.GhostBrakeAnimation3D;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import org.tinylog.Logger;

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
public class GhostAppearance3D extends Group implements GameLevelEntity, DisposableGraphicsObject {

    public enum AnimationID {
        GHOST_BRAKING;

        public String forGhost(Ghost ghost) {
            requireNonNull(ghost);
            return "%s_%d".formatted(name(), ghost.personality());
        }
    }

    private static final double HEIGHT_OVER_FLOOR = 2.0;

    private final ManagedAnimationsRegistry animations;
    private final Ghost3D ghost3D;
    //private NumberBox3D numberBox3D;

    private int numFlashes;

    private ChangeListener<Vector2f> positionChangeListener = (_, _, _) -> updateTransform(ghost());
    private ChangeListener<Direction> wishDirChangeListener = (_, _, _) -> updateTransform(ghost());

    public GhostAppearance3D(
        ManagedAnimationsRegistry animations,
        Ghost ghost,
        GhostAppearanceColors colors,
        GhostMeshSet meshes,
        GhostMaterialSet materials,
        double size)
    {
        this.animations = requireNonNull(animations);
        requireNonNull(ghost);
        requireNonNull(colors);
        requireNonNull(meshes);
        requireNonNull(materials);
        requireNonNegative(size);

        ghost3D = new Ghost3D(animations, ghost, colors, meshes, materials, size);

        animations.register(AnimationID.GHOST_BRAKING.forGhost(ghost), new GhostBrakeAnimation3D(this));
//        animations.register(AnimationID.GHOST_POINTS.forGhost(ghost), new GhostPointsAnimation3D(this));

        getChildren().add(ghost3D);
        updateTransform(ghost);
        setTranslateZ(-0.5 * size - HEIGHT_OVER_FLOOR);

        addPropertyChangeListeners();

        setGhostAppearance(GhostAppearance.NORMAL);
    }

    @Override
    public void dispose() {
        removePropertyChangeListeners();
        positionChangeListener = null;
        wishDirChangeListener = null;
        stopAllAnimations();
        animations.optAnimation(AnimationID.GHOST_BRAKING.forGhost(ghost())).ifPresent(ManagedAnimation::dispose);
        cleanupGroup(this, true);
    }

    @Override
    public void init(GameLevel level) {
        stopAllAnimations();
        updateTransform(ghost());
        setGhostAppearance(GhostAppearance.NORMAL);
    }

    @Override
    public void update(GameLevel level) {
        updateTransform(ghost());
        updateAppearance(level);
        if (ghost().moveInfo().tunnelEntered) {
            animations.animation(AnimationID.GHOST_BRAKING.forGhost(ghost())).playFromStart();
        }
        ghost3D.animateDress(ghost3D.isVisible());
    }

    public void hideGhostAppearance() {
        ghost3D.stopAnimations();
        ghost3D.setVisible(false);
    }

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = requireNonNegativeInt(numFlashes);
    }

    public Ghost3D ghost3D() {
        return ghost3D;
    }

    public Ghost ghost() {
        return ghost3D.ghost();
    }

    public void stopAllAnimations() {
        animations.optAnimation(AnimationID.GHOST_BRAKING.forGhost(ghost())).ifPresent(ManagedAnimation::stop);
        if (ghost3D != null) {
            ghost3D.stopAnimations();
        }
    }

    // private area, no trespassing

    private void setGhostAppearance(GhostAppearance ghostAppearance) {
        switch (ghostAppearance) {
            case NORMAL     -> ghost3D.setNormalLook();
            case FRIGHTENED -> ghost3D.setFrightenedLook();
            case EYES       -> ghost3D.setEyesOnlyLook();
            case FLASHING   -> ghost3D.setFlashingLook(numFlashes);
        }
        ghost3D.setVisible(true);
        ghost3D.animateDress(true);

        Logger.debug("Ghost appearance for {} is now {}", ghost3D.ghost().name(), ghostAppearance);
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
        ghost3D.turnTowards(ghost.wishDir());
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