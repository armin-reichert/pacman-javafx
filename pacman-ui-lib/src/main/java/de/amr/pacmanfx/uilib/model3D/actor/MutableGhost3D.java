/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.DisposableGraphicsObject;
import de.amr.pacmanfx.uilib.model3D.GhostMaterials;
import de.amr.pacmanfx.uilib.model3D.animation.Ghost3DBrakeAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.Ghost3DPointsAnimation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.paint.Color;
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
 * <li>{@link GhostAppearance#NUMBER}: eaten ghost's point value.
 * </ul>
 */
public class MutableGhost3D extends Group implements DisposableGraphicsObject {

    private static final double HEIGHT_OVER_FLOOR = 2.0;

    public static GhostAppearance computeAppearance(
        GhostState ghostState,
        boolean powerActive,
        boolean powerFading,
        boolean killedInCurrentPowerPhase)
    {
        return switch (ghostState) {
            case LEAVING_HOUSE, LOCKED -> {
                if (powerActive && !killedInCurrentPowerPhase) {
                    yield powerFading ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
                }
                yield GhostAppearance.NORMAL;
            }
            case FRIGHTENED -> powerFading ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            case EATEN -> GhostAppearance.NUMBER;
            default -> GhostAppearance.NORMAL;
        };
    }

    private final ObjectProperty<GhostAppearance> appearance = new SimpleObjectProperty<>();

    private final Color lightColor;

    private final Ghost3D ghost3D;
    private Shape3D numberShape3D;

    private int numFlashes;

    private Ghost3DBrakeAnimation brakeAnimation;
    private Ghost3DPointsAnimation pointsAnimation;

    public MutableGhost3D(
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
        lightColor = colorSet.normal().dressColor();

        pointsAnimation = new Ghost3DPointsAnimation(animationRegistry, this);
        brakeAnimation = new Ghost3DBrakeAnimation(animationRegistry, this);

        getChildren().add(ghost3D);
        addPropertyChangeListeners();
        updateTransform();
        setTranslateZ(-0.5 * size - HEIGHT_OVER_FLOOR);

        appearance.set(GhostAppearance.NORMAL);
    }

    @Override
    public void dispose() {
        removePropertyChangeListeners();
        stopAllAnimations();
        if (brakeAnimation != null) {
            brakeAnimation.dispose();
            brakeAnimation = null;
        }
        if (pointsAnimation != null) {
            pointsAnimation.dispose();
            pointsAnimation = null;
        }
        cleanupGroup(this, true);
    }

    public void init(GameLevel level) {
        stopAllAnimations();
        updateTransform();
        updateAppearance(level);
    }

    /**
     * Called on each clock tick (frame).
     *
     * @param level the game level
     */
    public void update(GameLevel level) {
        updateAppearance(level);
        if (ghost().isVisible()) {
            ghost3D.dressAnimation().playOrContinue();
        } else {
            ghost3D.dressAnimation().stop();
        }
        if (ghost().moveInfo().tunnelEntered) {
            brakeAnimation.playFromStart();
        }
    }

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = requireNonNegativeInt(numFlashes);
    }

    public Ghost3D ghost3D() {
        return ghost3D;
    }

    public void setNumberShape3D(Shape3D numberShape3D) {
        this.numberShape3D = requireNonNull(numberShape3D);
        numberShape3D.setVisible(false);
        if (getChildren().size() == 1) {
            getChildren().add(numberShape3D);
        } else {
            getChildren().set(1, numberShape3D);
        }
    }

    public Optional<Shape3D> optNumberShape3D() {
        return Optional.ofNullable(numberShape3D);
    }

    public Ghost ghost() {
        return ghost3D.ghost();
    }

    public Color lightColor() {
        return lightColor;
    }

    public void stopAllAnimations() {
        if (brakeAnimation != null)  brakeAnimation.stop();
        if (pointsAnimation != null) pointsAnimation.stop();
        if (ghost3D != null) {
            ghost3D.stopAnimations();
        }
    }

    // private area, no trespassing

    private final ChangeListener<Vector2f> positionChangeListener = (_, _, _) -> updateTransform();

    private final ChangeListener<Direction> wishDirChangeListener = (_, _, _) -> updateTransform();

    private final ChangeListener<GhostAppearance> appearanceChangeListener = (_, _, ghostAppearance) -> {
        if (requireNonNull(ghostAppearance) == GhostAppearance.NUMBER) {
            setNumberAppearance();
        } else {
            setGhostAppearance(ghostAppearance);
        }
        Logger.debug("{} 3D appearance set to {}", ghost().name(), ghostAppearance);
    };

    private void setNumberAppearance() {
        if (numberShape3D == null) {
            Logger.error("Number shape 3D not set for ghost {}, cannot show points", ghost().name());
            return;
        }
        ghost3D.stopAnimations();
        ghost3D.setVisible(false);
        numberShape3D.setVisible(true);
        pointsAnimation.playFromStart();
    }

    private void setGhostAppearance(GhostAppearance appearance) {
        if (numberShape3D != null) {
            pointsAnimation.stop();
            numberShape3D.setVisible(false);
        }
        ghost3D.setVisible(true);
        switch (appearance) {
            case NORMAL     -> ghost3D.setNormalLook();
            case FRIGHTENED -> ghost3D.setFrightenedLook();
            case EYES       -> ghost3D.setEyesOnlyLook();
            case FLASHING   -> ghost3D.setFlashingLook(numFlashes);
        }
    }

    private void addPropertyChangeListeners() {
        ghost().positionProperty().addListener(positionChangeListener);
        ghost().wishDirProperty().addListener(wishDirChangeListener);
        appearance.addListener(appearanceChangeListener);
    }

    private void removePropertyChangeListeners() {
        ghost().positionProperty().removeListener(positionChangeListener);
        ghost().wishDirProperty().removeListener(wishDirChangeListener);
        appearance.removeListener(appearanceChangeListener);
    }

    private void updateTransform() {
        final Vector2f center = ghost().center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        ghost3D.turnTowards(ghost().wishDir());
    }

    private void updateAppearance(GameLevel gameLevel) {
        final boolean powerActive = gameLevel.pac().powerTimer().isRunning();
        final boolean powerFading = gameLevel.pac().isPowerFading(gameLevel);
        // ghost that got already killed in the current power phase do not look frightened anymore
        final boolean killedInCurrentPhase = gameLevel.energizerVictims().contains(ghost());
        final GhostAppearance newAppearance = computeAppearance(
            ghost().state(),
            powerActive,
            powerFading,
            killedInCurrentPhase);
        appearance.set(newAppearance);
    }
}