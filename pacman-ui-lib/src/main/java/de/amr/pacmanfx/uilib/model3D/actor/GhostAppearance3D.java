/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelEntity;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.GhostMaterials;
import de.amr.pacmanfx.uilib.model3D.animation.Ghost3DBrakeAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.Ghost3DPointsAnimation;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.shape.Shape3D;
import org.tinylog.Logger;

import java.util.Optional;

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

    private static final double HEIGHT_OVER_FLOOR = 2.0;

    private final Ghost3D ghost3D;
    private Shape3D numberShape3D;

    private int numFlashes;

    private Ghost3DBrakeAnimation brakeAnimation;
    private Ghost3DPointsAnimation numberAnimation;

    private ChangeListener<Vector2f> positionChangeListener = (_, _, _) -> updateTransform(ghost());
    private ChangeListener<Direction> wishDirChangeListener = (_, _, _) -> updateTransform(ghost());

    public GhostAppearance3D(
        AnimationRegistry animationRegistry,
        Ghost ghost,
        GhostColorSet colorSet,
        GhostMeshes meshes,
        GhostMaterials materials,
        double size)
    {
        requireNonNull(animationRegistry);
        requireNonNull(ghost);
        requireNonNull(colorSet);
        requireNonNull(meshes);
        requireNonNull(materials);
        requireNonNegative(size);

        ghost3D = new Ghost3D(animationRegistry, ghost, colorSet, meshes, materials, size);

        brakeAnimation = new Ghost3DBrakeAnimation(this);
        animationRegistry.register("Ghost_Braking_%d".formatted(ghost().personality()), brakeAnimation);

        numberAnimation = new Ghost3DPointsAnimation(this);
        animationRegistry.register("Ghost_Points_%d".formatted(ghost().personality()), numberAnimation);

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
        if (brakeAnimation != null) {
            brakeAnimation.dispose();
            brakeAnimation = null;
        }
        if (numberAnimation != null) {
            numberAnimation.dispose();
            numberAnimation = null;
        }
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
            brakeAnimation.playFromStart();
        }
        ghost3D.animateDress(ghost3D.isVisible());
    }

    public void showAsNumber(Shape3D numberShape3D) {
        this.numberShape3D = requireNonNull(numberShape3D);
        if (getChildren().size() == 1) {
            getChildren().add(numberShape3D);
        } else {
            getChildren().set(1, numberShape3D);
        }
        ghost3D.stopAnimations();
        ghost3D.setVisible(false);

        enableNumberAppearance();
    }

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = requireNonNegativeInt(numFlashes);
    }

    public Ghost3D ghost3D() {
        return ghost3D;
    }

    public Optional<Shape3D> optNumberShape3D() {
        return Optional.ofNullable(numberShape3D);
    }

    public Ghost ghost() {
        return ghost3D.ghost();
    }

    public void stopAllAnimations() {
        if (brakeAnimation != null)  brakeAnimation.stop();
        if (numberAnimation != null) numberAnimation.stop();
        if (ghost3D != null) {
            ghost3D.stopAnimations();
        }
    }

    // private area, no trespassing

    private void disableNumberAppearance() {
        if (numberShape3D != null) {
            numberShape3D.setVisible(false);
            numberAnimation.stop();
        }
    }

    private void enableNumberAppearance() {
        if (numberShape3D != null) {
            numberShape3D.setVisible(true);
            numberAnimation.playFromStart();
            Logger.info("Ghost {} is now shown as number", ghost3D.ghost().name());
        }
        else Logger.error("Cannot enable number appearance: no number shape exists");
    }

    private void setGhostAppearance(GhostAppearance ghostAppearance) {
        disableNumberAppearance();
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