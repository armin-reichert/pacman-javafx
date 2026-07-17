/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.event.GameEventManagerImpl;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.gameplay.FrameContext;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gameplay.HuntingStepResult;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.state.GameState;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Context passed to game scenes and game flow state machines for the currently running game variant.
 */
public class GameContextImpl implements GameContext {

    private final CoinMechanism coinMechanism;

    private final GameVariant gameVariant;

    private final GameEventManager eventManager;

    private final HUDState hudState;

    private FrameContext thisFrame;

    public GameContextImpl(CoinMechanism coinMechanism, GameVariant gameVariant) {
        this.coinMechanism = requireNonNull(coinMechanism);
        this.gameVariant = requireNonNull(gameVariant);
        this.hudState = new HUDState();
        this.eventManager = new GameEventManagerImpl();
    }

    @Override
    public GameCheats cheats() {
        return gameVariant.cheats();
    }

    @Override
    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    @Override
    public GameEventManager eventManager() {
        return eventManager;
    }

    @Override
    public GameModel model() {
        return gameVariant.gameModel();
    }

    @Override
    public Optional<GameLevel> optLevel() {
        return model().optLevel();
    }

    @Override
    public GameLevel assertLevel() {
        return model().assertLevel();
    }

    @Override
    public GameFlowController flow() {
        return gameVariant.gameFlow();
    }

    @Override
    public GamePlay gamePlay() {
        return gameVariant.gamePlay();
    }

    @Override
    public HUDState hudState() {
        return hudState;
    }

    @Override
    public GameState state() {
        return flow().state();
    }

    @Override
    public FrameContext thisFrame() {
        return thisFrame;
    }

    @Override
    public void newFrame(long tick) {
        thisFrame = new FrameData(tick, new HuntingStepResult());
    }

    record FrameData(long tick, HuntingStepResult huntingStepResult) implements FrameContext {}
}
