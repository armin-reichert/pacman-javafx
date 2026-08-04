/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DTransformComp;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import javafx.scene.Group;

import static java.util.Objects.requireNonNull;

public class Pac3DSupportSystem {

    public static void makePac3D(Pac pac, AnimationRegistry animationRegistry, Group body, Group jaw) {
        requireNonNull(pac);
        requireNonNull(animationRegistry);
        requireNonNull(body);
        requireNonNull(jaw);

        if (!pac.hasComponent(Pac3DViewComp.class)) {
            pac.setComponent(Pac3DViewComp.class, new Pac3DViewComp());
            pac.setComponent(Pac3DTransformComp.class, new Pac3DTransformComp());
            pac.setComponent(Pac3DAnimationComp.class, new Pac3DAnimationComp(animationRegistry));
        }
        pac.requireComponent(Pac3DViewComp.class).setBodyAndJaw(body, jaw);
    }

    public static void init(Pac pac, GameContext gameContext) {
        requireNonNull(gameContext);
        final GameLevel level = gameContext.assertLevel();

        Pac3DTransformSystem.init(pac, level);
        Pac3DAnimationSystem.init(pac);
        Pac3DAnimationSystem.setPowerMode(pac, false);
    }

    public static void update(Pac pac, GameContext gameContext) {
        requireNonNull(gameContext);
        final GameLevel level = gameContext.assertLevel();

        Pac3DTransformSystem.update(pac, level);
        Pac3DAnimationSystem.update(pac, gameContext.systems().pacState());
        Pac3DAnimationSystem.updatePowerLight(pac, gameContext.systems().pacPower());
    }
}