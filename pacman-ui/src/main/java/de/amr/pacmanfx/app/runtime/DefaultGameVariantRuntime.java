/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.basics.Named;
import de.amr.pacmanfx.app.config.GameVariantUIConfig;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.engine.EngineContext;
import de.amr.pacmanfx.engine.GameVariantRuntime;
import de.amr.pacmanfx.engine.config.Cartridge;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class DefaultGameVariantRuntime implements GameVariantRuntime {

    private final Map<Named, Object> extensions = new HashMap<>();

    private final GameVariantPlayConfig playConfig;

    private final CoinMechanism coinMechanism = new CoinMechanism(99);

    private final GameVariantUIConfig uiConfig;

    public DefaultGameVariantRuntime(Cartridge cartridge, EngineContext engineContext) {
        requireNonNull(cartridge);
        requireNonNull(engineContext);

        playConfig = new GameVariantPlayConfig(
            cartridge.systemsFactory().get(),
            cartridge.gamePlayFactory().get(),
            cartridge.gameFlowFactory().get(),
            cartridge.gameRulesFactory().get(),
            cartridge.worldMapManagerFactory().get()
        );

        try {
            uiConfig = (GameVariantUIConfig) cartridge.uiConfigClass().getConstructor().newInstance();
            extensions.putAll(uiConfig.createExtensions(engineContext));
            Logger.info("Added {} extension(s) to game variant (cartridge ID={}):", extensions.size(), cartridge.id());
            extensions.forEach((name, ext) -> Logger.info("- Extension Name: {}, Type: {}", name, ext.getClass().getSimpleName()));
        } catch (Exception cause) {
            throw new RuntimeException("Could not create UI configuration from class " + cartridge.uiConfigClass(), cause);
        }
    }

    public GameVariantUIConfig uiConfig() {
        return uiConfig;
    }

    @Override
    public <T> T extensionValue(Named id, Class<T> type) {
        requireNonNull(id);
        requireNonNull(type);
        final Object value = extensions.get(id);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        throw new IllegalArgumentException("Extension value " + value + " of type " + type.getName() + " not found");
    }

    @Override
    public GameVariantPlayConfig playConfig() {
        return playConfig;
    }

    @Override
    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }
}
