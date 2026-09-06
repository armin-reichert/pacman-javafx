package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.pac.system.*;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.tengenmspacman.entities.pac.system.TengenMsPacMan_PacAnimationSystem;

public class TengenMsPacMan_GameSystems extends GameSystems {

    @Override
    protected void createHUDSystems() {
        scoreSystem = new ScoreSystem();
        levelCounterSystem = new LevelCounterSystem();
        hudUpdateSystem = new TengenMsPacMan_HUD_UpdateSystem();
    }

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
