package de.amr.pacmanfx.tengenmspacman.systems;

import de.amr.basics.ui.entities.hud.levelCounter.LevelCounterSystem;
import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.core.entities.actor.pac.*;
import de.amr.pacmanfx.core.entities.hud.ScoreSystem;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_HUD_UpdateSystem;
import de.amr.pacmanfx.tengenmspacman.entities.messageview.MessageAnimationSystem;
import de.amr.pacmanfx.tengenmspacman.entities.pac.system.TengenMsPacMan_PacAnimationSystem;
import de.amr.pacmanfx.ui.entities3D.bonus.BonusView3D_System;
import de.amr.pacmanfx.ui.entities3D.house.HouseView3D_System;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3D_System;
import de.amr.pacmanfx.ui.entities3D.world.GameLevel3DUpdateSystem;
import de.amr.pacmanfx.ui.entities3D.ghost.GhostView3D_System;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D_System;

public class TengenMsPacMan_GameSystems extends CommonGameSystems {

    private MessageAnimationSystem messageAnimationSystem;

    public MessageAnimationSystem messageAnimationSystem() {
        return messageAnimationSystem;
    }

    public TengenMsPacMan_GameSystems() {
        addSystem(new BonusView3D_System());
        addSystem(new LivesCounterView3D_System());
        addSystem(new GhostView3D_System());
        addSystem(new HouseView3D_System());
        addSystem(new PacView3D_System());
        addSystem(new GameLevel3DUpdateSystem());
        addSystem(new TengenMsPacMan_Score3DSystem());
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
