package de.amr.pacmanfx.core.gameplay;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.event.pac.PacPowerStartsEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.GameRules;

import static java.util.Objects.requireNonNull;

public class FoodEventHandler implements DefaultGameEventListener {

    private final GameContext game;

    private GameSystems systems() {
        return game.variant().systems();
    }

    public FoodEventHandler(GameContext game) {
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
}
