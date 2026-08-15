/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class CartridgeRepository {

    private final Set<Cartridge> cartridges = new HashSet<>(6);

    public CartridgeRepository() {
    }

    private Optional<Cartridge> findCartridgeByName(String name) {
        return cartridges.stream().filter(cartridge -> cartridge.id().name().equals(name)).findFirst();
    }

    public void insertCartridges(Cartridge... cartridgesToInsert) {
        for (var cartridge : cartridgesToInsert) {
            if (cartridge == null) {
                Logger.error("NULL cartridge detected! Are you kidding me?");
            } else {
                final boolean added = cartridges.add(cartridge);
                if (added) {
                    Logger.info("Cartridge {} inserted into machine", cartridge.id().name());
                } else {
                    Logger.info("Cartridge {} already inserted", cartridge.id().name());
                }
            }
        }
    }

    public Cartridge cartridgeByName(String name) {
        requireNonNull(name);
        return findCartridgeByName(name).orElseThrow(
            () -> {
                final String errorMessage = "No cartridge for game variant %s has been inserted!".formatted(name);
                Logger.error(errorMessage);
                return new IllegalArgumentException(errorMessage);
            }
        );
    }

    public boolean containsCartridgeWithName(String name) {
        requireNonNull(name);
        return findCartridgeByName(name).isPresent();
    }
}
