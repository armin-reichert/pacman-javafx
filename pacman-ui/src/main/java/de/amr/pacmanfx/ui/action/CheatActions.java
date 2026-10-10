/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.event.pac.PacEatsFoodEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.assets.VoiceID;
import javafx.scene.input.KeyCode;
import javafx.scene.media.Media;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public final class CheatActions {

    private final GameAction<GameEngineContext> actionAddLives;
    private final GameAction<GameEngineContext> actionEatAllPellets;
    private final GameAction<GameEngineContext> actionKillGhosts;
    private final GameAction<GameEngineContext> actionEnterNextLevel;
    private final GameAction<GameEngineContext> actionToggleAutopilot;
    private final GameAction<GameEngineContext> actionActivateAutopilot;
    private final GameAction<GameEngineContext> actionDeactivateAutopilot;
    private final GameAction<GameEngineContext> actionActivateImmunity;
    private final GameAction<GameEngineContext> actionDeactivateImmunity;
    private final GameAction<GameEngineContext> actionToggleImmunity;

    private final Set<ActionKeyBinding> bindings;

    public CheatActions() {

        actionAddLives = new GameAction<>("cheat_add_lives") {
            @Override
            public void execute(GameEngineContext context) {
                context.optCurrentGame().ifPresent(game -> {
                    final GameSession session = game.session();
                    session.setNumLives(session.numLives() + 3);
                    session.cheats().notifyCheatUsed();
                    final String msg = context.translationManager().translate("flash.cheat_add_lives", session.numLives());
                    context.ui().shortMessage(msg);
                });
            }

            @Override
            public boolean isEnabled(GameEngineContext context) {
                return context.optCurrentGame()
                    .flatMap(game -> normalLevel(game.session()))
                    .isPresent();            }
        };

        actionEatAllPellets = new GameAction<>("cheat_eat_all_pellets") {
            @Override
            public void execute(GameEngineContext context) {
                context.optCurrentGame().ifPresent(game -> {
                    final GameSession session = game.session();
                    final GameLevel level = session.level();
                    level.food().eatPellets();
                    session.cheats().notifyCheatUsed();
                    game.eventManager().publishEvent(
                        new PacEatsFoodEvent(level.entitySet().pac(), false, true, context.clock().currentTick()));
                });
            }

            @Override
            public boolean isEnabled(GameEngineContext context) {
                if (context.optCurrentGame().isEmpty()) return false;

                return context.optCurrentGame().flatMap(game -> normalLevel(game.session())).isPresent()
                    && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(context.optCurrentGame().get().state());
            }
        };

        actionKillGhosts = new GameAction<>("cheat_kill_ghosts") {
            @Override
            public void execute(GameEngineContext context) {
                context.optCurrentGame().ifPresent(game -> {
                    final GameSession session = game.session();
                    final GameLevel level = session.level();

                    session.cheats().notifyCheatUsed();

                    final List<Ghost> killableGhosts = level.entitySet().ghosts()
                        .filter(ghost -> GhostState.FRIGHTENED == ghost.state().enumValue()
                            || GhostState.HUNTING_PAC == ghost.state().enumValue())
                        .toList();

                    if (!killableGhosts.isEmpty()) {
                        level.setGhostKillCount(0); // start again with lowest number for killing ghost
                        killableGhosts.forEach(ghost -> game.playConfig().gamePlay().pacEatsGhost(game, level, ghost));
                        game.playConfig().gameFlow().enterGameState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
                    }
                });
            }

            @Override
            public boolean isEnabled(GameEngineContext context) {
                if (context.optCurrentGame().isEmpty()) return false;

                return context.optCurrentGame().flatMap(game -> normalLevel(game.session())).isPresent()
                    && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(context.optCurrentGame().get().state());
            }
        };

        actionEnterNextLevel = new GameAction<>("cheat_enter_next_level") {
            @Override
            public void execute(GameEngineContext context) {
                context.optCurrentGame().ifPresent(game -> {
                    game.session().cheats().notifyCheatUsed();
                    game.playConfig().gameFlow().enterGameState(game, CommonGameStateID.GAME_LEVEL_COMPLETE);
                });
            }

            @Override
            public boolean isEnabled(GameEngineContext engineContext) {
                final GameContext game = engineContext.optCurrentGame().orElse(null);
                if (game == null) return false;

                final GameLevel normalLevel = normalLevel(game.session()).orElse(null);
                if (normalLevel == null) return false;

                return CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(game.state())
                    && normalLevel.number() < game.playConfig().rules().lastLevelNumber();
            }
        };

        actionToggleAutopilot = new GameAction<>("toggle_autopilot") {
            @Override
            public void execute(GameEngineContext context) {
                context.optCurrentGame().ifPresent(game -> {
                    final GameCheats cheats = game.session().cheats();
                    setAutopilot(context, game.session(), !cheats.isPacUsingAutopilot());
                });
            }

            @Override
            public boolean isEnabled(GameEngineContext engineContext) {
                final GameContext game = engineContext.optCurrentGame().orElse(null);
                if (game == null) return false;

                final GameLevel normalLevel = normalLevel(game.session()).orElse(null);
                return normalLevel != null;
            }
        };

        actionActivateAutopilot = new GameAction<>("activate_autopilot") {
            @Override
            public void execute(GameEngineContext engineContext) {
                engineContext.optCurrentGame().ifPresent(game -> setAutopilot(engineContext, game.session(), true));
            }

            @Override
            public boolean isEnabled(GameEngineContext engineContext) {
                final GameContext game = engineContext.optCurrentGame().orElse(null);
                if (game == null) return false;

                final GameLevel normalLevel = normalLevel(game.session()).orElse(null);
                return normalLevel != null;
            }
        };

        actionDeactivateAutopilot = new GameAction<>("deactivate_autopilot") {
            @Override
            public void execute(GameEngineContext engineContext) {
                engineContext.optCurrentGame().ifPresent(game -> setAutopilot(engineContext, game.session(), false));
            }

            @Override
            public boolean isEnabled(GameEngineContext engineContext) {
                final GameContext game = engineContext.optCurrentGame().orElse(null);
                if (game == null) return false;

                final GameLevel normalLevel = normalLevel(game.session()).orElse(null);
                return normalLevel != null;
            }
        };

        actionActivateImmunity = new GameAction<>("activate_immunity") {
            @Override
            public void execute(GameEngineContext engineContext) {
                engineContext.optCurrentGame().ifPresent(game -> setPacImmune(engineContext, game.session(), true));
            }

            @Override
            public boolean isEnabled(GameEngineContext engineContext) {
                final GameContext game = engineContext.optCurrentGame().orElse(null);
                if (game == null) return false;

                final GameLevel normalLevel = normalLevel(game.session()).orElse(null);
                return normalLevel != null;
            }
        };

        actionDeactivateImmunity = new GameAction<>("deactivate_immunity") {
            @Override
            public void execute(GameEngineContext engineContext) {
                engineContext.optCurrentGame().ifPresent(game -> setPacImmune(engineContext, game.session(), false));
            }

            @Override
            public boolean isEnabled(GameEngineContext engineContext) {
                final GameContext game = engineContext.optCurrentGame().orElse(null);
                if (game == null) return false;

                final GameLevel normalLevel = normalLevel(game.session()).orElse(null);
                return normalLevel != null;
            }
        };

        actionToggleImmunity = new GameAction<>("toggle_immunity") {
            @Override
            public void execute(GameEngineContext engineContext) {
                engineContext.optCurrentGame().ifPresent(game -> {
                    final GameCheats cheats = game.session().cheats();
                    setPacImmune(engineContext, game.session(), !cheats.isPacImmune());
                });
            }

            @Override
            public boolean isEnabled(GameEngineContext engineContext) {
                final GameContext game = engineContext.optCurrentGame().orElse(null);
                if (game == null) return false;

                final GameLevel normalLevel = normalLevel(game.session()).orElse(null);
                return normalLevel != null;
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionToggleAutopilot(), combine().alt().key(KeyCode.A)),
            new ActionKeyBinding(actionToggleImmunity(),  combine().alt().key(KeyCode.I)),
            new ActionKeyBinding(actionEatAllPellets(),   combine().alt().key(KeyCode.E)),
            new ActionKeyBinding(actionAddLives(),        combine().alt().key(KeyCode.L)),
            new ActionKeyBinding(actionEnterNextLevel(),  combine().alt().key(KeyCode.N)),
            new ActionKeyBinding(actionKillGhosts(),      combine().alt().key(KeyCode.X))
        );
    }

    public GameAction<GameEngineContext> actionAddLives() {
        return actionAddLives;
    }

    public GameAction<GameEngineContext> actionEatAllPellets() {
        return actionEatAllPellets;
    }

    public GameAction<GameEngineContext> actionKillGhosts() {
        return actionKillGhosts;
    }

    public GameAction<GameEngineContext> actionEnterNextLevel() {
        return actionEnterNextLevel;
    }

    public GameAction<GameEngineContext> actionToggleAutopilot() {
        return actionToggleAutopilot;
    }

    public GameAction<GameEngineContext> actionActivateAutopilot() {
        return actionActivateAutopilot;
    }

    public GameAction<GameEngineContext> actionDeactivateAutopilot() {
        return actionDeactivateAutopilot;
    }

    public GameAction<GameEngineContext> actionActivateImmunity() {
        return actionActivateImmunity;
    }

    public GameAction<GameEngineContext> actionDeactivateImmunity() {
        return actionDeactivateImmunity;
    }

    public GameAction<GameEngineContext> actionToggleImmunity() {
        return actionToggleImmunity;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }

    // Helpers

    private void setAutopilot(GameEngineContext context, GameSession session, boolean auto) {
        final GameCheats cheats = session.cheats();
        final GameUI ui = context.ui();

        cheats.pacUsingAutopilotProperty().set(auto);

        final String message = context.translationManager().translate(auto ? "flash.autopilot_on" : "flash.autopilot_off");
        final Media voice = auto ? VoiceID.AUTOPILOT_ON.media() : VoiceID.AUTOPILOT_OFF.media();

        ui.shortMessage(message);
        context.soundManager().voice().playAfterSec(1, voice);
    }

    private void setPacImmune(GameEngineContext context, GameSession session, boolean immune) {
        final GameCheats cheats = session.cheats();
        final GameUI ui = context.ui();

        cheats.pacImmuneProperty().set(immune);

        final String message = context.translationManager().translate(immune ? "flash.player_immunity_on" : "flash.player_immunity_off");
        final Media voice = immune ? VoiceID.IMMUNITY_ON.media() : VoiceID.IMMUNITY_OFF.media();

        ui.shortMessage(message);
        context.soundManager().voice().playAfterSec(1, voice);
    }

    private Optional<GameLevel> normalLevel(GameSession session) {
        return session.optLevel().filter(_ -> !session.isAttractMode());
    }
}