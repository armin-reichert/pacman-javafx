/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerStartsEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.GameRules;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class PacEatingEventHandler implements DefaultGameEventListener {

    private final GameContext game;

    private GameSystems systems() {
        return game.variant().systems();
    }

    public PacEatingEventHandler(GameContext game) {
        this.game = requireNonNull(game);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        if (e.energizer()) {
            onPacEatsEnergizer(e);
        } else {
            onPacEatsPellet(e);
        }
    }

    private void onPacEatsPellet(PacEatsFoodEvent e) {
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final GameRules rules = game.variant().rules();
        final Pac pac = e.pac();

        // Eating a pellet earns 10 points in Arcade Pac-Man
        game.variant().gamePlay().scorePoints(game, rules.scoringRules().pointsForPellet(), level.number());

        // The "gatekeeper" of the ghost house has counters for the eaten food driving its behavior
        session.gateKeeper().registerFoodEaten(level);

        // Update Elroy state of red ghost (Arcade Pac-Man only)
        systems().ghostState().updateElroyState(game);

        // Pac-Man "digests" and takes a 1 tick nap
        systems().pacDigestion().digestPellet(pac, rules);
    }

    private void onPacEatsEnergizer(PacEatsFoodEvent e) {
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final GameRules rules = game.variant().rules();
        final Pac pac = e.pac();

        // Eating an energizer earns 50 points in Arcade Pac-Man
        game.variant().gamePlay().scorePoints(game, rules.scoringRules().pointsForEnergizer(), level.number());

        // The "gatekeeper" of the ghost house has counters for the eaten food driving its behavior
        session.gateKeeper().registerFoodEaten(level);

        // Update Elroy state of red ghost (Arcade Pac-Man only)
        systems().ghostState().updateElroyState(game);

        // The "kill chain" starts: 200, 400, 800, 1600 points for ghosts eaten with same energizer power
        level.clearGhostKillChain();

        // Ghosts turn back even if the Pac power time is zero and no event is published!
        level.entities().ghostsInAnyOfStates(CommonGamePlay.GHOST_TURNBACK_STATES).forEach(systems().worldNavigator()::requestTurnBack);

        // Pac-Man "digests" and takes a 3 tick nap
        systems().pacDigestion().digestEnergizer(pac, rules);

        final long powerDurationTicks = TickTimer.secToTicks(rules.pacPowerSeconds(level.number()));
        if (powerDurationTicks > 0) {
            game.eventManager().publishGameEvent(new PacPowerStartsEvent(pac, powerDurationTicks));
        }
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        final Bonus bonus = e.bonus();
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final GameSystems systems = game.variant().systems();
        final GameRules rules = game.variant().rules();

        // Bonus value depends on game variant and bonus type
        game.variant().gamePlay().scorePoints(game, bonus.data().points(), level.number());
        Logger.info("Scored {} points for eating bonus {}", bonus.data().points(), bonus);

        // Eaten bonus is displayed as points for short time
        systems.bonusState().showEatenForSeconds(bonus, rules.eatenBonusDisplaySeconds());

        // A bonus moving through the world stops
        bonus.optComp(WorldNavigationComp.class).ifPresent(_ -> systems.worldNavigator().setMoveDirSpeed(bonus, 0));
    }
}
