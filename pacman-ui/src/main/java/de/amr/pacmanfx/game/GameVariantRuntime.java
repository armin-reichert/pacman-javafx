/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationContainer;

import static java.util.Objects.requireNonNull;

public class GameVariantRuntime {

   private final CoinMechanism coinMechanism = new CoinMechanism(99);

    private final SpriteAnimationContainer spriteAnimationContainer = new SpriteAnimationContainer();

    private final GameVariantPlayConfig playConfig;
    private final GameVariantUIConfig uiConfig;

    public GameVariantRuntime(GameBox gameBox, Cartridge cartridge) {
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

    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    public SpriteAnimationContainer spriteAnimContainer() {
        return spriteAnimationContainer;
    }
}
