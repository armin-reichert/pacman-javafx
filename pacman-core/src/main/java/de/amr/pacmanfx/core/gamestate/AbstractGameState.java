/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.Named;
import de.amr.basics.fsm.State;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;

import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractGameState implements State<GameContext>, Named {

    private final Named id;
    private final TickTimer timer;

    public AbstractGameState(Named id) {
        this.id = Objects.requireNonNull(id);
        this.timer = new TickTimer("GameStateTimer-" + getClass().getSimpleName());
    }

    public abstract void onEnterState(GameContext game);

    public abstract void onUpdate(GameContext game, long globalTickCount, long stateTickCount);

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
    public TickTimer stateTimer() {
        return timer;
    }

    @Override
    public final void onEnter(GameContext game) {
        onEnterState(game);
    }

    @Override
    public final void onUpdate(GameContext game) {
        onUpdate(game, game.session().thisFrame().tickCount(), stateTimer().tickCount());
    }

    protected void lockGhosts(GameContext game, GameLevelEntitySet entities, boolean locked) {
        entities.ghosts().forEach(ghost -> {
            ghost.worldNavigation().setPaused(locked);
            game.systems().ghostAnimation().lockAnimation(ghost, locked);
        });
    }

    protected void lockPacAndGhosts(GameContext game, GameLevelEntitySet entities, boolean locked) {
        final Pac pac = entities.pac();
        pac.worldNavigation().setPaused(locked);
        game.systems().pacAnimation().lockAnimation(pac, locked);

        lockGhosts(game, entities, locked);
    }
}
