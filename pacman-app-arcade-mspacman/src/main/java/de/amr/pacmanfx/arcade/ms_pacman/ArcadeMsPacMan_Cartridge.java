package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.ui.game.Cartridge;

public class ArcadeMsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        Arcade_GameFlow::new,
        ArcadeMsPacMan_GameModel::new,
        ArcadeMsPacMan_GameRules::new,
        ArcadeMsPacMan_UIConfig::new
    );
}
