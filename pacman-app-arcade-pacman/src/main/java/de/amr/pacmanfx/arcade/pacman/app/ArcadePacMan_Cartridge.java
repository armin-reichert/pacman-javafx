package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacManGameVariant;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Cartridge;
import de.amr.pacmanfx.ui.game.GameExtension;

import java.util.Set;

public class ArcadePacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_PACMAN,
        Arcade_GameFlow::new,
        ArcadePacMan_GameModel::new,
        ArcadePacMan_GameRules::new,
        ArcadePacManGameVariant::new,
        Set.of(new GameExtension(Arcade_GameExtensions.ACTIONS, Arcade_Actions::new))
    );
}
