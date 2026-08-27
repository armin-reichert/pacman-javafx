package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.game.Cartridge;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_WorldMapManager;
import de.amr.pacmanfx.tengenmspacman.rules.TengenMsPacMan_GameRules;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly">Ms.Pac-Man-NES-Tengen-Disassembly</a>
 */
public class TengenMsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.TENGEN_MS_PACMAN,
        GameSystems::new,
        TengenMsPacMan_GamePlay::new,
        TengenMsPacMan_GameVariantUIConfig::createGameFlow,
        TengenMsPacMan_GameRules::new,
        TengenMsPacMan_WorldMapManager::new,
        TengenMsPacMan_GameVariantUIConfig::new
    );
}
