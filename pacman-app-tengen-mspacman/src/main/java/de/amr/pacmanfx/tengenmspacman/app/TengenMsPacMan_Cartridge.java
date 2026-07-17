package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.Cartridge;
import de.amr.pacmanfx.game.GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacManGameVariant;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;

import java.util.Set;

public class TengenMsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        GameVariantID.TENGEN_MS_PACMAN,
        TengenMsPacMan_GamePlay::new,
        TengenMsPacManGameVariant::createGameFlow,
        TengenMsPacMan_GameModel::new,
        TengenMsPacManGameVariant::new,
        Set.of(
            new GameExtension(TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings::new),
            new GameExtension(TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions::new)
        )
    );
}
