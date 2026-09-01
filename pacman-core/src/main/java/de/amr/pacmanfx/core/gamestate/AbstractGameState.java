/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.Named;
import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.rules.GameRules;

import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractGameState implements State<GameContext>, Named {

    private final Named id;
    private final TickTimer timer;

    // State context variables
    protected GameRules rules;
    protected GameFlowController flow;
    protected GamePlay gamePlay;
    protected GameSystems systems;
    protected GameSession session;
    protected HUD hud;

    public AbstractGameState(Named id) {
        this.id = Objects.requireNonNull(id);
        this.timer = new TickTimer("GameStateTimer-" + getClass().getSimpleName());
    }

    public abstract void onEnterState(GameContext game);

    public abstract void onUpdateState(GameContext game, long globalTick, long stateTick);

    public Named id() {
        return id;
    }

    public boolean nameIsOneOf(Named... names) {
        return Arrays.asList(names).contains(id);
    }

        @Override
    public String name() {
        return id.name();
    }

    @Override
    public TickTimer timer() {
        return timer;
    }

    @Override
    public final void onEnter(GameContext game) {
        rules = game.variant().rules();
        flow = game.variant().gameFlow();
        gamePlay = game.variant().gamePlay();
        systems = game.variant().systems();
        session = game.session();
        hud = session.hud();
        onEnterState(game);
    }

    @Override
    public final void onUpdate(GameContext game) {
        onUpdateState(game, game.session().thisFrame().tick(), timer().tickCount());
    }

    protected void showPacAndGhosts(GameLevelEntitySet entities) {
        entities.pac().show();
        for (Ghost ghost : entities.ghosts()) {
            ghost.show();
        }
    }

    protected void lockPacAndGhosts(GameLevelEntitySet entities, boolean locked) {
        final Pac pac = entities.pac();
        pac.worldNavigation().setPaused(locked);
        systems.pacAnimation().lockAnimation(pac, locked);

        for (Ghost ghost : entities.ghosts()) {
            ghost.worldNavigation().setPaused(locked);
            systems.ghostAnimation().lockAnimation(ghost, locked);
        }
    }
}
