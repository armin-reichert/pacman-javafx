/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.pac;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.actors.PacState;
import de.amr.pacmanfx.core.model.comp.pac.PacDigestionComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.steering.Steering;

import static java.util.Objects.requireNonNull;

public class PacStateSystem {

    private final WorldNavigationSystem navigator;
    private final WorldMovementPolicy movementPolicy;
    private final PacDigestionSystem digestionSystem;
    private final PacPowerSystem powerSystem;
    private final SpriteAnimSystem spriteAnimSystem;

    private Steering<Pac> automaticSteering;

    public PacStateSystem(
        WorldNavigationSystem navigator,
        WorldMovementPolicy movementPolicy,
        PacDigestionSystem digestionSystem,
        PacPowerSystem powerSystem,
        SpriteAnimSystem spriteAnimSystem)
    {
        this.navigator = navigator;
        this.movementPolicy = movementPolicy;
        this.digestionSystem = digestionSystem;
        this.powerSystem = powerSystem;
        this.spriteAnimSystem = spriteAnimSystem;
    }

    public void update(GameContext gameContext) {
        requireNonNull(gameContext);
        final GameLevel level = gameContext.assertLevel();
        update(gameContext.cheats(), level, level.entities().pac());
    }

    private void update(
        GameCheats cheats,
        GameLevel level,
        Pac pac)
    {
        final PacState state = pac.state(); //TODO entity component
        final PacDigestionComp digestion = pac.digestion();
        final ActorSpeedRules speedRules = level.gameModel().rules().actorSpeedRules();

        if (state == PacState.DEAD || digestion.restingTicks() == PacDigestionComp.REST_FOREVER) {
            return;
        }

        digestionSystem.update(pac);
        if (digestionSystem.mustRest(pac)) {
            return;
        }

        if (cheats.isPacUsingAutopilot()) {
            automaticSteering.steer(pac, level);
        }

        final float speed = powerSystem.isPowerActive(pac)
            ? speedRules.pacSpeedWhenHasPower(level)
            : speedRules.pacSpeed(level);

        navigator.setSpeed(pac, speed);
        navigator.tryMovingOrTeleporting(pac, level, movementPolicy);

        //TODO This should be called outside, leave it here for now
        if (pac.worldNavigation().info.moved) {
            spriteAnimSystem.playSelected(pac);
        } else {
            spriteAnimSystem.stopSelected(pac);
        }
    }

    public void setAutomaticSteering(Steering<Pac> steering) {
        automaticSteering = requireNonNull(steering);
    }
}
