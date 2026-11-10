/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.systems;

import de.amr.pacmanfx.core.CommonGameSystems;
import de.amr.pacmanfx.ui.entities3D.bonus.system.Bonus3DUpdateSystem;
import de.amr.pacmanfx.ui.entities3D.house.system.House3DSystem;
import de.amr.pacmanfx.ui.entities3D.levelcounter.system.LevelCounter3DViewSystem;
import de.amr.pacmanfx.ui.entities3D.livescounter.LivesCounterView3DSystem;
import de.amr.pacmanfx.ui.entities3D.score.Score3DSystem;
import de.amr.pacmanfx.ui.entities3D.world.system.World3DUpdateSystem;
import de.amr.pacmanfx.ui.systems.GhostSystems3D;
import de.amr.pacmanfx.ui.systems.PacSystems3D;

public class ArcadePacMan_GameSystems extends CommonGameSystems {

    public ArcadePacMan_GameSystems() {
        addSystem(new Bonus3DUpdateSystem());
        addSystem(new LevelCounter3DViewSystem());
        addSystem(new LivesCounterView3DSystem());
        addSystem(new GhostSystems3D());
        addSystem(new House3DSystem());
        addSystem(new PacSystems3D());
        addSystem(new World3DUpdateSystem());
        addSystem(new Score3DSystem());
    }
}
