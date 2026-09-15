/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationContainer;

import static java.util.Objects.requireNonNull;

public class GameVariantConfig {

    private final GameVariantPlayConfig playConfig;
    private final GameVariantUIConfig uiConfig;

    public GameVariantConfig(GameBox gameBox, Cartridge cartridge) {
        requireNonNull(gameBox);
        requireNonNull(cartridge);
        playConfig = new GameVariantPlayConfig(
            cartridge.systemsFactory().get(),
            cartridge.gamePlayFactory().get(),
            cartridge.gameFlowFactory().get(),
            cartridge.gameRulesFactory().get(),
            cartridge.worldMapManagerFactory().get()
        );
        uiConfig = cartridge.uiConfigFactory().get();
    }

    public GameVariantPlayConfig playConfig() {
        return playConfig;
    }

    public GameVariantUIConfig uiConfig() {
        return uiConfig;
    }

    //TODO move elsewhere
    private final SpriteAnimationContainer spriteAnimationContainer = new SpriteAnimationContainer();

    public SpriteAnimationContainer spriteAnimContainer() {
        return spriteAnimationContainer;
    }
}
