/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.settings.world;

import de.amr.basics.json.JsonLoader;
import de.amr.pacmanfx.ui.entities3D.livescounter.comp.LivesCounter3DSettings;
import de.amr.pacmanfx.uilib.entities.d3.bonus.comp.Bonus3DSettings;
import de.amr.pacmanfx.uilib.entities.d3.ghost.comp.GhostSettings;
import de.amr.pacmanfx.uilib.entities.d3.levelcounter.comp.LevelCounter3DSettings;
import de.amr.pacmanfx.uilib.entities.d3.pac.comp.PacSettings;

import java.util.List;

public record WorldSettings(
    PacSettings pac,
    List<GhostSettings> ghosts,
    Bonus3DSettings bonus,
    LevelCounter3DSettings levelCounter,
    LivesCounter3DSettings livesCounter,
    Maze3DSettings maze,
    House3DSettings house,
    Floor3DSettings floor,
    Pellet3DSettings pellet,
    Energizer3DSettings energizer)
{
    public static final WorldSettings DEFAULT_SETTINGS = JsonLoader.load(
        WorldSettings.class.getResource("/de/amr/pacmanfx/ui/settings/world/world.json"), WorldSettings.class);
}
