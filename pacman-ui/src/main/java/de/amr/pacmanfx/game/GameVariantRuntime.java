/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.ui.action.core.GameApp;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class GameVariantRuntime {

   private final CoinMechanism coinMechanism = new CoinMechanism(99);

    private final SpriteAnimationContainer spriteAnimationContainer = new SpriteAnimationContainer();

    private final GameVariantPlayConfig playConfig;
    private final GameVariantUIConfig uiConfig;

    private final Map<Named, Object> extensions = new HashMap<>();

    public GameVariantRuntime(GameBox gameBox, Cartridge cartridge, GameApp app) {
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

        extensions.putAll(uiConfig.createExtensions(app));
        Logger.info("Added {} extension(s) to game variant:", extensions.size());
        extensions.forEach((name, ext) -> Logger.info("- Name: {}, type: {}", name, ext.getClass().getSimpleName()));
    }

    public <T> T extensionValue(Named id, Class<T> type) {
        requireNonNull(id);
        requireNonNull(type);
        final Object value = extensions.get(id);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        throw new IllegalArgumentException("Extension value " + value + " of type " + type.getName() + " not found");
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
