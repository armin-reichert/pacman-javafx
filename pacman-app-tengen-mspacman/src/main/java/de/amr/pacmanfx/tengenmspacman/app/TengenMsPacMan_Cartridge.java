package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameFlow;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.ui.game.Cartridge;

public class TengenMsPacMan_Cartridge {

    public static final Cartridge CARTRIDGE = new Cartridge(
        TengenMsPacMan_GameFlow::new,
        TengenMsPacMan_GameModel::new,
        TengenMsPacMan_GameRules::new,
        TengenMsPacMan_UIConfig::new
    );
}
