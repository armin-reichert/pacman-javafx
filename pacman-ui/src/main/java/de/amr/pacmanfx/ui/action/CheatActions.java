/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.GameUI_Constants;
import de.amr.pacmanfx.ui.game.Game;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CheatActions {

    abstract class AbstractCheatAction extends GameAction {
        public AbstractCheatAction(String id) {
            super(CheatActions.this.game, id);
        }
    }

    private final Game game;
    
    public CheatActions(Game game) {
        this.game = Objects.requireNonNull(game);
    }
    
    public final GameAction ACTION_ADD_LIVES = new AbstractCheatAction("cheat_add_lives") {

        @Override
        public void doAction() {
            final GameModel gameModel = game.currentGameContext().model();
            gameModel.lives().add(3);
            gameModel.cheats().notifyCheatUsed();

            final String msg = game.ui().translations().translate("message.cheat_add_lives", gameModel.lives().count());
            game.shortMessage(msg);
        }

        @Override
        public boolean isEnabled() { return normalLevel(game).isPresent(); }
    };

    public final GameAction ACTION_EAT_ALL_PELLETS = new AbstractCheatAction("cheat_eat_all_pellets") {

        @Override
        public void doAction() {
            final GameContext gameContext = game.currentGameContext();
            final GameModel gameModel = gameContext.model();
            final GameLevel level = gameContext.requireLevel();

            level.worldMap().foodLayer().eatPellets();
            gameModel.cheats().notifyCheatUsed();

            gameContext.flow().publishGameEvent(new PacEatsFoodEvent(gameContext, level.entities().pac(), false, true));
        }

        @Override
        public boolean isEnabled() {
            final GameState gameState = game.currentGameContext().state();
            return normalLevel(game).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
        }
    };

    public final GameAction ACTION_KILL_GHOSTS = new AbstractCheatAction("cheat_kill_ghosts") {

        @Override
        public void doAction() {
            final GameContext gameContext = game.currentGameContext();
            final GameModel gameModel = gameContext.model();
            final GameLevel level = gameContext.requireLevel();

            gameModel.cheats().notifyCheatUsed();

            final List<Ghost> killableGhosts = level.entities().ghosts().stream()
                .filter(ghost -> GhostState.FRIGHTENED == ghost.state() || GhostState.HUNTING_PAC == ghost.state())
                .toList();

            if (!killableGhosts.isEmpty()) {
                level.clearGhostKillChain(); // start again with lowest number for killing ghost
                killableGhosts.forEach(ghost -> gameModel.onEatGhost(gameContext, level, ghost));
                gameContext.flow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST);
            }
        }

        @Override
        public boolean isEnabled() {
            final GameState gameState = game.currentGameContext().state();
            return normalLevel(game).isPresent() && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState);
        }
    };

    public final GameAction ACTION_ENTER_NEXT_LEVEL = new AbstractCheatAction("cheat_enter_next_level") {

        @Override
        public void doAction() {
            final GameContext gameContext = game.currentGameContext();
            final GameModel gameModel = gameContext.model();

            gameModel.cheats().notifyCheatUsed();
            gameContext.flow().enterState(GameStateID.GAME_LEVEL_COMPLETE);
        }

        @Override
        public boolean isEnabled() {
            final GameContext gameContext = game.currentGameContext();
            final GameState gameState = gameContext.state();
            final GameLevel normalLevel = normalLevel(game).orElse(null);

            return normalLevel != null
                && GameStateID.GAME_LEVEL_PLAYING.identifies(gameState)
                && normalLevel.number() < gameContext.rules().lastLevelNumber();
        }
    };

    public final GameAction ACTION_TOGGLE_AUTOPILOT = new AbstractCheatAction("toggle_autopilot") {

        @Override
        public void doAction() {
            final GameCheats cheats = game.currentGameContext().model().cheats();

            setAutopilot(game, !cheats.isPacUsingAutopilot());
        }

        @Override
        public boolean isEnabled() {
            return normalLevel(game).isPresent();
        }
    };

    public final GameAction ACTION_ACTIVATE_AUTOPILOT = new AbstractCheatAction("activate_autopilot") {

        @Override
        public void doAction() {
            setAutopilot(game, true);
        }

        @Override
        public boolean isEnabled() {
            return normalLevel(game).isPresent();
        }
    };

    public final GameAction ACTION_DEACTIVATE_AUTOPILOT = new AbstractCheatAction("deactivate_autopilot") {

        @Override
        public void doAction() {
            setAutopilot(game, false);
        }

        @Override
        public boolean isEnabled() {
            return normalLevel(game).isPresent();
        }
    };

    public final GameAction ACTION_ACTIVATE_IMMUNITY = new AbstractCheatAction("activate_immunity") {

        @Override
        public void doAction() {
            setPacImmune(game, true);
        }

        @Override
        public boolean isEnabled() {
            return normalLevel(game).isPresent();
        }
    };

    public final GameAction ACTION_DEACTIVATE_IMMUNITY = new AbstractCheatAction("deactivate_immunity") {

        @Override
        public void doAction() {
            setPacImmune(game, false);
        }

        @Override
        public boolean isEnabled() {
            return normalLevel(game).isPresent();
        }
    };

    public final GameAction ACTION_TOGGLE_IMMUNITY = new AbstractCheatAction("toggle_immunity") {

        @Override
        public void doAction() {
            final GameCheats cheats = game.currentGameContext().model().cheats();

            setPacImmune(game, !cheats.isPacImmune());
        }

        @Override
        public boolean isEnabled() {
            return normalLevel(game).isPresent();
        }
    };

    // Helpers

    private void setAutopilot(Game game, boolean auto) {
        final GameCheats cheats = game.currentGameContext().model().cheats();

        cheats.pacUsingAutopilotProperty().set(auto);

        game.shortMessage(game.ui().translations().translate(auto ? "autopilot_on" : "autopilot_off"));
        game.ui().sounds().playVoice(auto ? GameUI_Constants.VOICE_AUTOPILOT_ON : GameUI_Constants.VOICE_AUTOPILOT_OFF);
    }

    private void setPacImmune(Game game, boolean immune) {
        final GameCheats cheats = game.currentGameContext().model().cheats();

        cheats.pacImmuneProperty().set(immune);

        game.shortMessage(game.ui().translations().translate(immune ? "player_immunity_on" : "player_immunity_off"));
        game.ui().sounds().playVoice(immune ? GameUI_Constants.VOICE_IMMUNITY_ON : GameUI_Constants.VOICE_IMMUNITY_OFF);
    }

    private Optional<GameLevel> normalLevel(Game game) {
        return game.currentGameContext().optCurrentLevel().filter(level -> !level.isDemoLevel());
    }
}