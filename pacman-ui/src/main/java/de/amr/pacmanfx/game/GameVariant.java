/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.GameVariantConfig;

public class GameVariant {

    private final GameVariantConfig config;
    private final GameVariantUIConfig uiConfig;

    private final SpriteAnimContainer spriteAnimContainer = new SpriteAnimContainer();

    public GameVariant(Cartridge cartridge) {
        config = new GameVariantConfig(
            cartridge.systemsFactory().get(),
            cartridge.gamePlayFactory().get(),
            cartridge.gameFlowFactory().get(),
            cartridge.gameRulesFactory().get(),
            cartridge.worldMapManagerFactory().get()
        );
        uiConfig = cartridge.uiConfigFactory().get();
    }

    public GameVariantConfig config() {
        return config;
    }

    public GameVariantUIConfig uiConfig() {
        return uiConfig;
    }

    public SpriteAnimContainer spriteAnimContainer() {
        return spriteAnimContainer;
    }
}
