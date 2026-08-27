package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GamePlay;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameVariantUIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_WorldMapManager;
import de.amr.pacmanfx.arcade.pacman.rules.ArcadePacMan_GameRules;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.game.Cartridge;

public class ArcadePacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.ARCADE_PACMAN,
        GameSystems::new,
        ArcadePacMan_GamePlay::new,
        ArcadePacMan_GameVariantUIConfig::createGameFlow,
        ArcadePacMan_GameRules::new,
        ArcadePacMan_WorldMapManager::new,
        ArcadePacMan_GameVariantUIConfig::new
    );
}
