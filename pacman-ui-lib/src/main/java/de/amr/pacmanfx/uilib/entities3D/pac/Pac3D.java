/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.DisposableGraphicsObject;
import javafx.scene.Group;

import static java.util.Objects.requireNonNull;

/**
 * (Ms.) Pac-Man 3D representations.
 */
public class Pac3D extends GameEntity implements UpdatableEntity, DisposableGraphicsObject {

    private final Pac pac;

    public Pac3D(AnimationRegistry animationRegistry, Pac pac, Group body, Group jaw) {
        requireNonNull(body);
        requireNonNull(jaw);

        setComponent(Pac3DViewComp.class, new Pac3DViewComp());
        setComponent(Pac3DTransformComp.class, new Pac3DTransformComp());
        setComponent(Pac3DAnimationComp.class, new Pac3DAnimationComp(animationRegistry));

        this.pac = requireNonNull(pac);

        requireComponent(Pac3DViewComp.class).setBodyAndJaw(body, jaw);
    }

    public Pac pac() {
        return pac;
    }

    @Override
    public void dispose() {
        for (var animID : Pac3DAnimationID.values()) {
//            animations.optAnimation(animID).ifPresent(ManagedAnimation::dispose);
        }
//        cleanupLight(powerLight);
//        cleanupGroup(root, true);
    }

    @Override
    public void init(GameContext gameContext) {
        requireNonNull(gameContext);
        final GameLevel level = gameContext.assertLevel();

        Pac3DTransformSystem.init(this, level);
        Pac3DAnimationSystem.init(this);
        Pac3DAnimationSystem.setPowerMode(this, false);
    }

    @Override
    public void update(GameContext gameContext) {
        requireNonNull(gameContext);
        final GameLevel level = gameContext.assertLevel();

        Pac3DTransformSystem.update(this, level);
        Pac3DAnimationSystem.update(this, gameContext.assertLevel(), gameContext.systems().pacState());
        Pac3DAnimationSystem.updatePowerLight(gameContext.systems().pacPower(), this);
    }
}