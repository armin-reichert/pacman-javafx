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
import javafx.beans.value.ObservableValue;
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

    private static final double GHOST_OVER_FLOOR_DIST = 2.0;

    public static GhostAppearance selectAppearance(
        GhostState ghostState,
        boolean powerActive,
        boolean powerFading,
        boolean killedInCurrentPhase)
    {
        return switch (ghostState) {
            case LEAVING_HOUSE, LOCKED -> {
                if (powerActive && !killedInCurrentPhase) {
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

    private final Ghost ghost;
    private final Color lightColor;
    private final double size;

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
        this.ghost = requireNonNull(ghost);
        this.lightColor = requireNonNull(colorSet).normal().dressColor();
        requireNonNull(meshes);
        requireNonNull(materials);
        this.size = requireNonNegative(size);

        final var ghostShape3D = new Ghost3D(
            animationRegistry,
            ghost,
            colorSet,
            meshes,
            materials,
            size
        );

        getChildren().setAll(ghostShape3D);

        pointsAnimation = new Ghost3DPointsAnimation(animationRegistry, this);
        brakeAnimation = new Ghost3DBrakeAnimation(animationRegistry, this);

        addListeners();

        update3DTransform();
        appearance.set(GhostAppearance.NORMAL);
    }

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = requireNonNegativeInt(numFlashes);
    }

    @Override
    public void dispose() {
        removeListeners();

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

    public Ghost3D ghost3D() {
        if (getChildren().isEmpty()) {
            throw new IllegalStateException("MutableGhost3D already disposed?");
        }
        return (Ghost3D) getChildren().getFirst();
    }

    public void setNumberShape3D(Shape3D numberShape3D) {
        this.numberShape3D = numberShape3D;
        if (getChildren().size() == 1) {
            getChildren().add(numberShape3D);
        } else {
            getChildren().set(1, numberShape3D);
        }
        numberShape3D.setVisible(false);
    }

    public Optional<Shape3D> optNumberShape3D() {
        return Optional.ofNullable(numberShape3D);
    }

    public Ghost ghost() {
        return ghost;
    }

    public Color lightColor() {
        return lightColor;
    }

    public void stopAllAnimations() {
        if (brakeAnimation != null)  brakeAnimation.stop();
        if (pointsAnimation != null) pointsAnimation.stop();
        if (ghost3D() != null) {
            if (ghost3D().dressAnimation() != null) {
                ghost3D().dressAnimation().stop();
            }
            if (ghost3D().flashingAnimation() != null) {
                ghost3D().flashingAnimation().stop();
            }
        }
    }

    public void init(GameLevel gameLevel) {
        stopAllAnimations();
        update3DTransform();
        updateAppearance(gameLevel);
    }

    /**
     * Called on each clock tick (frame).
     *
     * @param gameLevel the game level
     */
    public void update(GameLevel gameLevel) {
        updateAppearance(gameLevel);
        if (ghost.isVisible()) {
            ghost3D().dressAnimation().playOrContinue();
        } else {
            ghost3D().dressAnimation().stop();
        }
        if (ghost.moveInfo().tunnelEntered && !brakeAnimation.isRunning()) {
            brakeAnimation.playFromStart();
        }
    }

    // private area, no trespassing

    private final ChangeListener<Vector2f> positionChangeListener = (_, _, _) -> update3DTransform();

    private final ChangeListener<Direction> wishDirChangeListener = (_, _, _) -> update3DTransform();

    // Cannot use lambda here because it references fields of this class
    private final ChangeListener<GhostAppearance> appearanceChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends GhostAppearance> obs, GhostAppearance oldAppearance, GhostAppearance newAppearance) {
            if (newAppearance == GhostAppearance.NUMBER) {
                if (numberShape3D != null) {
                    numberShape3D.setVisible(true);
                }
                else {
                    Logger.error("Number shape 3D not set for ghost {}, cannot show points", ghost.name());
                }
                ghost3D().setVisible(false);
                ghost3D().dressAnimation().stop();
                pointsAnimation.playFromStart();
            }
            else {
                if (numberShape3D != null) {
                    numberShape3D.setVisible(false);
                }
                ghost3D().setVisible(true);
                switch (newAppearance) {
                    case NORMAL     -> ghost3D().setNormalLook();
                    case FRIGHTENED -> ghost3D().setFrightenedLook();
                    case EYES       -> ghost3D().setEyesOnlyLook();
                    case FLASHING   -> ghost3D().setFlashingLook(numFlashes);
                }
                pointsAnimation.stop();
            }
            Logger.debug("{} 3D appearance set to {}", ghost.name(), newAppearance);
        }
    };

    private void addListeners() {
        ghost.positionProperty().addListener(positionChangeListener);
        ghost.wishDirProperty().addListener(wishDirChangeListener);
        appearance.addListener(appearanceChangeListener);
    }

    private void removeListeners() {
        ghost.positionProperty().removeListener(positionChangeListener);
        ghost.wishDirProperty().removeListener(wishDirChangeListener);
        appearance.removeListener(appearanceChangeListener);
    }

    private void update3DTransform() {
        final Vector2f center = ghost.center();
        setTranslateX(center.x());
        setTranslateY(center.y());
        setTranslateZ(-0.5 * size - GHOST_OVER_FLOOR_DIST);
        ghost3D().turnTowards(ghost.wishDir());
    }

    private void updateAppearance(GameLevel gameLevel) {
        final boolean powerActive = gameLevel.pac().powerTimer().isRunning();
        final boolean powerFading = gameLevel.pac().isPowerFading(gameLevel);
        // ghost that got already killed in the current power phase do not look frightened anymore
        final boolean killedInCurrentPhase = gameLevel.energizerVictims().contains(ghost);
        final GhostAppearance newAppearance = selectAppearance(
            ghost.state(),
            powerActive,
            powerFading,
            killedInCurrentPhase);
        appearance.set(newAppearance);
    }
}