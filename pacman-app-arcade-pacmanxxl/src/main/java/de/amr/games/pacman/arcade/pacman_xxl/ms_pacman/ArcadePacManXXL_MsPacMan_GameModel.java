/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl.ms_pacman;

import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.CustomMapSelectionMode;
import de.amr.games.pacman.steering.RuleBasedPacSteering;

import java.io.File;

public class ArcadePacManXXL_MsPacMan_GameModel extends ArcadeMsPacMan_GameModel {

    @Override
    public void init(File homeDir) {
        super.init(homeDir);
        mapSelectionMode = CustomMapSelectionMode.CUSTOM_MAPS_FIRST;
        demoLevelSteering = new RuleBasedPacSteering(this); // super class uses predefined steering
        scoreManager.setHighScoreFile(new File(homeDir, "highscore-mspacman_xxl.xml"));
        builtinMaps.clear(); // super class constructor adds Aracde maps
        loadMapsFromModule("maps/masonic_%d.world", 8);
        updateCustomMaps();
    }

    @Override
    protected WorldMap selectWorldMap(int levelNumber) {
        return super.selectWorldMap(levelNumber);
    }
}
