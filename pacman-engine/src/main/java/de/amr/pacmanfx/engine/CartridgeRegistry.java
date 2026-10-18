/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine;

import de.amr.pacmanfx.engine.config.Cartridge;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Registry of game variant "cartridges".
 * <p>
 * Buy your cartridge now (super hot Black Friday deal: $0,99 per item)!
 * </p>
 */
public class CartridgeRegistry {

    private static class SingletonHolder {
        static final CartridgeRegistry SINGLETON = new CartridgeRegistry();
    }

    public static CartridgeRegistry instance() {
        return SingletonHolder.SINGLETON;
    }

    private final Set<Cartridge> cartridgeSet = new HashSet<>(6);

    private CartridgeRegistry() {}

    public void register(Cartridge... cartridges) {
        for (var cartridge : cartridges) {
            if (cartridge == null) {
                Logger.error("NULL cartridge detected! Are you kidding me?");
            } else {
                if (cartridgeSet.add(cartridge)) {
                    Logger.info("Cartridge {} has been registered!", cartridge.id().name());
                } else {
                    Logger.info("Cartridge {} was already registered!", cartridge.id().name());
                }
            }
        }
    }

    public Cartridge getCartridgeByName(String name) {
        requireNonNull(name);
        return findByName(name).orElseThrow(() -> new IllegalArgumentException(
            "No cartridge for game variant '%s' exists".formatted(name))
        );
    }

    private Optional<Cartridge> findByName(String name) {
        return cartridgeSet.stream().filter(c -> c.id().name().equals(name)).findFirst();
    }
}