package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacManConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameFlow;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.ui.game.Cartridge;

import java.util.Map;

public class TengenMsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.TENGEN_MS_PACMAN,
        TengenMsPacMan_GameFlow::new,
        TengenMsPacMan_GameModel::new,
        TengenMsPacMan_GameRules::new,
        TengenMsPacManConfig::new,
        Map.of(
            TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings::new,
            TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions::new
        )
    );
}
