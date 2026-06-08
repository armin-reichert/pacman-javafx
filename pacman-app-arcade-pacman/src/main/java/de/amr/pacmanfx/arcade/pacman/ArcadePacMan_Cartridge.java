package de.amr.pacmanfx.arcade.pacman;

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.ui.game.GameVariantCartridge;

public class ArcadePacMan_Cartridge {

    public static final GameVariantCartridge CARTRIDGE = new GameVariantCartridge(
        Arcade_GameFlow::new,
        ArcadePacMan_GameModel::new,
        ArcadePacMan_GameRules::new,
        ArcadePacMan_UIConfig::new
    );
}
