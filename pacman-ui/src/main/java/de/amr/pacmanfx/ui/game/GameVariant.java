package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.DefaultCheatsImpl;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.simulation.GamePlay;
import de.amr.pacmanfx.ui.GameVariantConfig;

public class GameVariant {
    private final GamePlay gamePlay;
    private final GameFlow gameFlow;
    private final GameModel gameModel;
    private final GameCheats cheats;
    private final GameVariantConfig config;

    public GameVariant(Cartridge cartridge) {
        gamePlay = cartridge.gamePlayFactory().get();
        gameFlow = cartridge.gameFlowFactory().get();
        gameModel = cartridge.gameModelFactory().get();
        cheats = new DefaultCheatsImpl();
        config = cartridge.uiConfigFactory().get();
    }

    public GamePlay gamePlay() {
        return gamePlay;
    }

    public GameFlow gameFlow() {
        return gameFlow;
    }

    public GameModel gameModel() {
        return gameModel;
    }

    public GameCheats cheats() {
        return cheats;
    }

    public GameVariantConfig config() {
        return config;
    }
}
