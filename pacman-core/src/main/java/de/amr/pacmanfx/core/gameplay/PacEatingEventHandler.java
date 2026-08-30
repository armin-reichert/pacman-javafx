/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.BonusPoints;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerStartsEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.GameRules;
import org.tinylog.Logger;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class PacEatingEventHandler implements DefaultGameEventListener {

    public static final Set<GhostState> GHOST_TURNBACK_STATES = Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC);

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
        level.gateKeeper().registerFoodEaten(level);

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
        level.gateKeeper().registerFoodEaten(level);

        // Update Elroy state of red ghost (Arcade Pac-Man only)
        systems().ghostState().updateElroyState(game);

        // The "kill chain" starts: 200, 400, 800, 1600 points for ghosts eaten with same energizer power
        level.setGhostKillCount(0);

        // Ghosts turn back even if the Pac power time is zero and no event is published!
        level.entities().ghostsInAnyOfStates(GHOST_TURNBACK_STATES).forEach(systems().navigator()::requestTurnBack);

        // Pac-Man "digests" and takes a 3 tick nap
        systems().pacDigestion().digestEnergizer(pac, rules);

        final long powerDurationTicks = TickTimer.secToTicks(rules.pacPowerSeconds(level.number()));
        if (powerDurationTicks > 0) {
            game.eventManager().publishGameEvent(new PacPowerStartsEvent(pac, powerDurationTicks));
        }
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        final GameRules rules = game.variant().rules();
        final GamePlay gamePlay = game.variant().gamePlay();

        final GameSession session = game.session();
        final GameLevel level = session.level();
        final Bonus bonus = e.bonus();

        // Bonus value depends on game variant and bonus type
        final int bonusValue = rules.scoringRules().pointsForBonus(bonus.data().symbolCode());
        gamePlay.scorePoints(game, bonusValue, level.number());
        Logger.info("Scored {} points for eating bonus {}", bonusValue, bonus);

        level.entities().remove(bonus);

        // Eaten bonus is displayed as points for short time
        final var bonusPoints = new BonusPoints(bonusValue);
        bonusPoints.pos().set(bonus.pos().asVector2f());
        bonusPoints.setLifetime(rules.eatenBonusDisplaySeconds());
        bonusPoints.show();
        level.entities().add(bonusPoints);
    }
}
