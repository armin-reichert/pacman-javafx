/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.input.Joypad;
import de.amr.pacmanfx.engine.input.JoypadButton;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.entities.pac.comp.PacBoosterComp;
import de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.ui.EngineContext;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.SteeringActions;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.Set;

import static de.amr.basics.util.Ufx.toggleBooleanProperty;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptionValues;

public final class TengenMsPacMan_Actions {

    private static class SingletonHolder {
        static final TengenMsPacMan_Actions SINGLETON = new TengenMsPacMan_Actions();
    }

    public static TengenMsPacMan_Actions instance() {
        return SingletonHolder.SINGLETON;
    }

    private final GameAction<EngineContext> actionEnterStartScreen;
    private final GameAction<EngineContext> actionQuitDemoLevel;
    private final GameAction<EngineContext> actionStartPlaying;
    private final GameAction<EngineContext> actionTogglePlaySceneDisplayMode;
    private final GameAction<EngineContext> actionToggleJoypadBindingsDisplayed;
    private final GameAction<EngineContext> actionTogglePacBooster;
    private final GameAction<EngineContext> actionSelectNextJoypadKeyBinding;

    private Set<ActionKeyBinding<EngineContext>> steeringBindings;
    private Set<ActionKeyBinding<EngineContext>> localBindings;

    private TengenMsPacMan_Actions() {

        actionEnterStartScreen = new GameAction<>("enter_start_screen") {
            @Override
            public void execute(EngineContext engine) {
                engine.optCurrentGame().ifPresent(game -> game.runtime().gameFlow()
                    .enterGameState(game, CommonGameStateID.GAME_PREPARATION));
            }
        };

        actionQuitDemoLevel = new GameAction<>("quit_demo_level") {
            @Override
            public void execute(EngineContext engine) {
                engine.optCurrentGame().ifPresent(game -> game.runtime().gameFlow()
                    .enterGameState(game, CommonGameStateID.GAME_PREPARATION));
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                final GameContext game = engine.optCurrentGame().orElse(null);
                return game != null && game.session().booleanValue(SessionKey.DEMO_LEVEL_MODE);
            }
        };

        actionStartPlaying = new GameAction<>("start_playing") {
            @Override
            public void execute(EngineContext engine) {
                engine.optCurrentGame().ifPresent(game -> game.runtime().gameFlow()
                    .enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING));
            }
        };

        actionTogglePlaySceneDisplayMode = new GameAction<>("toggle_play_scene_display_mode") {
            @Override
            public void execute(EngineContext engine) {
                final TengenMsPacMan_UISettings uiSettings = TengenMsPacMan_UISettings.instance();
                final SceneDisplay mode = uiSettings.playSceneDisplay.get();
                uiSettings.playSceneDisplay.set(mode == SceneDisplay.SCROLLING
                    ? SceneDisplay.SCALED_TO_FIT
                    : SceneDisplay.SCROLLING);
            }

            @Override
            public boolean isEnabled(EngineContext context) {
                return context.gameSceneManager().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D);
            }
        };

        actionToggleJoypadBindingsDisplayed = new GameAction<>("toggle_joypad_bindings_displayed") {
            @Override
            public void execute(EngineContext engine) {
                final TengenMsPacMan_UISettings uiSettings = TengenMsPacMan_UISettings.instance();
                toggleBooleanProperty(uiSettings.joypadBindingsDisplayed);
            }
        };

        actionTogglePacBooster = new GameAction<>("toggle_pac_booster") {
            @Override
            public void execute(EngineContext engine) {
                engine.optCurrentGame().ifPresent(game -> {
                    final GameSession session = game.session();
                    session.optLevel().ifPresent(level -> {
                        final boolean nextEnabledState = !gameOptionValues(session).boosterEnabled();
                        gameOptionValues(session).setBoosterEnabled(nextEnabledState);
                        if (nextEnabledState) {
                            engine.ui().showMessage("Booster ON!"); //TODO localize
                        }
                        //TODO hack: this should be done by entity update system!
                        level.entitySet().pac().assertComponent(PacBoosterComp.class).setBoosterEnabled(nextEnabledState);
                    });
                });
            }

            @Override
            public boolean isEnabled(EngineContext engine) {
                final GameContext game = engine.optCurrentGame().orElse(null);
                if (game != null) {
                    final GameSession session = game.session();
                    return gameOptionValues(session).boosterMode() == BoosterMode.ACTIVATE_WITH_A_OR_B && session.optLevel().isPresent();
                }
                return false;
            }
        };

        actionSelectNextJoypadKeyBinding = new GameAction<>("select_next_joypad_binding") {
            @Override
            public void execute(EngineContext context) {
                context.input().joypad().selectNextBinding();
            }
        };
    }

    private void createSteeringBindings(Joypad joypad) {
        final SteeringActions steeringActions = CommonGameActions.instance().steeringActions();
        steeringBindings = Set.of(
            new ActionKeyBinding<>(steeringActions.actionSteer(Direction.UP),
                keyForJoypadButton(joypad, JoypadButton.UP),    combine().ctrl().key(KeyCode.UP)),

            new ActionKeyBinding<>(steeringActions.actionSteer(Direction.DOWN),
                keyForJoypadButton(joypad, JoypadButton.DOWN),  combine().ctrl().key(KeyCode.DOWN)),

            new ActionKeyBinding<>(steeringActions.actionSteer(Direction.LEFT),
                keyForJoypadButton(joypad, JoypadButton.LEFT),  combine().ctrl().key(KeyCode.LEFT)),

            new ActionKeyBinding<>(steeringActions.actionSteer(Direction.RIGHT),
                keyForJoypadButton(joypad, JoypadButton.RIGHT), combine().ctrl().key(KeyCode.RIGHT))
        );
    }

    public Set<ActionKeyBinding<EngineContext>> steeringBindings(Joypad joypad) {
        if (steeringBindings == null) {
            createSteeringBindings(joypad);
        }
        return steeringBindings;
    }

    private void createLocalBindings(Joypad joypad) {
        localBindings = Set.of(
            new ActionKeyBinding<>(actionQuitDemoLevel(),                 keyForJoypadButton(joypad, JoypadButton.START)),
            new ActionKeyBinding<>(actionEnterStartScreen(),              keyForJoypadButton(joypad, JoypadButton.START)),
            new ActionKeyBinding<>(actionStartPlaying(),                  keyForJoypadButton(joypad, JoypadButton.START)),
            new ActionKeyBinding<>(actionTogglePacBooster(),              keyForJoypadButton(joypad, JoypadButton.A),
                keyForJoypadButton(joypad, JoypadButton.B)),
            new ActionKeyBinding<>(actionTogglePlaySceneDisplayMode(),    combine().alt().key(KeyCode.C)),
            new ActionKeyBinding<>(actionToggleJoypadBindingsDisplayed(), bareKey(KeyCode.SPACE))
        );
    }

    public Set<ActionKeyBinding<EngineContext>> localBindings(Joypad joypad) {
        if (localBindings == null) {
            createLocalBindings(joypad);
        }
        return localBindings;
    }

    public GameAction<EngineContext> actionEnterStartScreen() {
        return actionEnterStartScreen;
    }

    public GameAction<EngineContext> actionQuitDemoLevel() {
        return actionQuitDemoLevel;
    }

    public GameAction<EngineContext> actionStartPlaying() {
        return actionStartPlaying;
    }

    public GameAction<EngineContext> actionTogglePlaySceneDisplayMode() {
        return actionTogglePlaySceneDisplayMode;
    }

    public GameAction<EngineContext> actionToggleJoypadBindingsDisplayed() {
        return actionToggleJoypadBindingsDisplayed;
    }

    public GameAction<EngineContext> actionTogglePacBooster() {
        return actionTogglePacBooster;
    }

    public GameAction<EngineContext> actionSelectNextJoypadKeyBinding() {
        return actionSelectNextJoypadKeyBinding;
    }

    private static KeyCodeCombination keyForJoypadButton(Joypad joypad, JoypadButton button) {
        return joypad.keyForButton(button);
    }
}