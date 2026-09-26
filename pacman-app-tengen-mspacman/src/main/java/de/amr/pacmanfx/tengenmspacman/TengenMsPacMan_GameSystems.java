package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.ui.entities.hud.levelCounter.LevelCounterSystem;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.actor.pac.*;
import de.amr.pacmanfx.core.entities.hud.ScoreSystem;
import de.amr.pacmanfx.tengenmspacman.entities.messageview.MessageAnimationSystem;
import de.amr.pacmanfx.tengenmspacman.entities.pac.system.TengenMsPacMan_PacAnimationSystem;

public class TengenMsPacMan_GameSystems extends GameSystems {

    private MessageAnimationSystem messageAnimationSystem;

    public MessageAnimationSystem messageAnimationSystem() {
        return messageAnimationSystem;
    }

    @Override
    protected void createHUDSystems() {
        scoreSystem = new ScoreSystem();
        levelCounterSystem = new LevelCounterSystem();
        hudUpdateSystem = new TengenMsPacMan_HUD_UpdateSystem();
        messageAnimationSystem = new MessageAnimationSystem(motor);
    }

    @Override
    protected void createPacSystems() {
        pacWorldMovementPolicy = new PacWorldMovementPolicy();

        pacAutoSteeringSystem = new PacAutoSteeringSystem();
        pacPowerSystem = new PacPowerSystem();
        pacDigestionSystem = new PacDigestionSystem();

        // Use specific system
        pacAnimationSystem = new TengenMsPacMan_PacAnimationSystem(actorSpriteAnimController);

        pacUpdateSystem = new PacUpdateSystem(
            pacDigestionSystem,
            pacPowerSystem,
            pacAutoSteeringSystem,
            pacAnimationSystem,
            pacWorldMovementPolicy,
            navigator);
    }

}
