/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.systems;

import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.ui.entities3D.bonus.system.BonusView3D_UpdateSystem;
import de.amr.pacmanfx.ui.entities3D.house.HouseView3D_System;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3D_System;
import de.amr.pacmanfx.ui.entities3D.score.Score3DSystem;
import de.amr.pacmanfx.ui.entities3D.world.system.World3DUpdateSystem;
import de.amr.pacmanfx.ui.entities3D.ghost.GhostView3D_System;
import de.amr.pacmanfx.ui.entities3D.pac.PacView3D_System;

public class ArcadePacMan_GameSystems extends CommonGameSystems {

    public ArcadePacMan_GameSystems() {
        addSystem(new BonusView3D_UpdateSystem());
        addSystem(new LivesCounterView3D_System());
        addSystem(new GhostView3D_System());
        addSystem(new HouseView3D_System());
        addSystem(new PacView3D_System());
        addSystem(new World3DUpdateSystem());
        addSystem(new Score3DSystem());
    }
}
