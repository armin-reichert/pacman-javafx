/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariantPlayConfig;

public interface GameVariantRuntime {

    <T> T extensionValue(Named id, Class<T> type);

    GameVariantPlayConfig playConfig();

    CoinMechanism coinMechanism();
}
