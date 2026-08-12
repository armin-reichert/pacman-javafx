/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.session.GameSession;

/**
 * Facade giving access to non UI related classes.
 */
public interface GameContext {

    GameSystems systems();

    CoinMechanism coinMechanism();

    GamePlay gamePlay();

    GameEventManager eventManager();

    GameModel model();

    void setSession(GameSession session);

    GameSession session();
}
