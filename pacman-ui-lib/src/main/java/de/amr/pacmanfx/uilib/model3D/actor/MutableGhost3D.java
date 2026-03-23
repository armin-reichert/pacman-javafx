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
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

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

    private static final int NUMBER_BOX_SIZE_X = 14;
    private static final int NUMBER_BOX_SIZE_Y = 8;
    private static final int NUMBER_BOX_SIZE_Z = 8;

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

    private final Map<Image, PhongMaterial> numberMaterialCache = new HashMap<>();

    private final Ghost ghost;
    private final Color lightColor;
    private final double size;
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

        final var numberShape3D = new Box(NUMBER_BOX_SIZE_X, NUMBER_BOX_SIZE_Y, NUMBER_BOX_SIZE_Z);

        getChildren().setAll(ghostShape3D, numberShape3D);

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
        numberMaterialCache.clear();

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

    public Shape3D numberShape3D() {
        if (getChildren().isEmpty()) {
            throw new IllegalStateException("MutableGhost3D already disposed?");
        }
        return (Shape3D) getChildren().getLast();
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

    public void setNumberImage(Image numberImage) {
        if (!numberMaterialCache.containsKey(numberImage)) {
            var numberMaterial = new PhongMaterial();
            numberMaterial.setDiffuseMap(numberImage);
            numberMaterialCache.put(numberImage, numberMaterial);
        }
        numberShape3D().setMaterial(numberMaterialCache.get(numberImage));
    }

    // private area, no trespassing

    private final ChangeListener<Vector2f> positionChangeListener = (_, _, _) -> update3DTransform();

    private final ChangeListener<Direction> wishDirChangeListener = (_, _, _) -> update3DTransform();

    // Cannot use lambda here because it references fields of this class
    private final ChangeListener<GhostAppearance> appearanceChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends GhostAppearance> obs, GhostAppearance oldAppearance, GhostAppearance newAppearance) {
            if (newAppearance == GhostAppearance.NUMBER) {
                numberShape3D().setVisible(true);
                ghost3D().setVisible(false);
                ghost3D().dressAnimation().stop();
                pointsAnimation.playFromStart();
            }
            else {
                numberShape3D().setVisible(false);
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