/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.pacmanfx.app.config.GameVariantUIConfig;
import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.engine.EngineContext;
import de.amr.pacmanfx.engine.GameVariantRuntime;
import de.amr.pacmanfx.engine.config.Cartridge;

import static java.util.Objects.requireNonNull;

public final class DefaultGameVariantRuntime implements GameVariantRuntime {

    private final GameVariantPlayConfig playConfig;

    private final GameVariantUIConfig uiConfig;

    public DefaultGameVariantRuntime(Cartridge cartridge, EngineContext engine) {
        requireNonNull(cartridge);
        requireNonNull(engine);

        playConfig = new GameVariantPlayConfig(
            cartridge.systemsFactory().get(),
            cartridge.gamePlayFactory().get(),
            cartridge.gameFlowFactory().get(),
            cartridge.gameRulesFactory().get(),
            cartridge.worldMapManagerFactory().get()
        );

        try {
            uiConfig = (GameVariantUIConfig) cartridge.uiConfigClass().getConstructor().newInstance();
        } catch (Exception cause) {
            throw new RuntimeException("Could not create UI configuration from class " + cartridge.uiConfigClass(), cause);
        }
    }

    public GameVariantUIConfig uiConfig() {
        return uiConfig;
    }

    @Override
    public GameVariantPlayConfig playConfig() {
        return playConfig;
    }
}
