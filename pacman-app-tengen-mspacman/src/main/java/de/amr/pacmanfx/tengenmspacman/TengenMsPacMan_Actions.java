/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.SteeringActions;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.CommonGameSceneID;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.JoypadButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

import java.util.Set;

import static de.amr.basics.util.Ufx.toggleBooleanProperty;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

public final class TengenMsPacMan_Actions {

    private final GameAction actionEnterStartScreen;
    private final GameAction actionQuitDemoLevel;
    private final GameAction actionStartPlaying;
    private final GameAction actionTogglePlaySceneDisplayMode;
    private final GameAction actionToggleJoypadBindingsDisplayed;
    private final GameAction actionTogglePacBooster;
    private final GameAction actionSelectNextJoypadKeyBinding;

    private final Set<ActionKeyBinding> steeringBindings;
    private final Set<ActionKeyBinding> localBindings;

    public TengenMsPacMan_Actions(Joypad joypad, CommonGameActions commonGameActions) {

        actionEnterStartScreen = new GameAction("enter_start_screen") {
            @Override
            public void execute(GameAppContext app) {
                app.game().session().gameFlow().enterState(app.game(), CommonGameStateID.GAME_PREPARATION);
            }
        };

        actionQuitDemoLevel = new GameAction("quit_demo_level") {
            @Override
            public void execute(GameAppContext app) {
                app.game().session().gameFlow().enterState(app.game(), CommonGameStateID.GAME_PREPARATION);
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                return app.game().session().isAttractMode();
            }
        };

        actionStartPlaying = new GameAction("start_playing") {
            @Override
            public void execute(GameAppContext app) {
                app.game().session().gameFlow().enterState(app.game(), CommonGameStateID.GAME_OR_LEVEL_STARTING);
            }
        };

        actionTogglePlaySceneDisplayMode = new GameAction("toggle_play_scene_display_mode") {
            @Override
            public void execute(GameAppContext app) {
                final var uiSettings = app.currentGameVariantConfig().getExtensionValue(
                    TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

                final SceneDisplay mode = uiSettings.playSceneDisplay.get();
                uiSettings.playSceneDisplay.set(mode == SceneDisplay.SCROLLING
                    ? SceneDisplay.SCALED_TO_FIT
                    : SceneDisplay.SCROLLING);
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                return app.ui().gameScenes().currentGameSceneHasID(CommonGameSceneID.PLAY_SCENE_2D);
            }
        };

        actionToggleJoypadBindingsDisplayed = new GameAction("toggle_joypad_bindings_displayed") {
            @Override
            public void execute(GameAppContext app) {
                final var uiSettings = app.currentGameVariantConfig().getExtensionValue(
                    TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

                toggleBooleanProperty(uiSettings.joypadBindingsDisplayed);
            }
        };

        actionTogglePacBooster = new GameAction("toggle_pac_booster") {
            @Override
            public void execute(GameAppContext app) {
                final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) app.game().gamePlay();
                final GameSession session = app.game().session();
                session.optLevel().ifPresent(gameLevel -> {
                    gamePlay.activateBooster(app.game(), gameLevel.entities().pac(), !gamePlay.isBoosterOn(session));
                    if (gamePlay.isBoosterOn(session)) {
                        app.ui().shortMessage("Booster ON!"); //TODO localize
                    }
                });
            }

            @Override
            public boolean isEnabled(GameAppContext app) {
                final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) app.game().gamePlay();
                final GameSession session = app.game().session();
                return gamePlay.boosterMode(session) == BoosterMode.ACTIVATE_WITH_A_OR_B && session.optLevel().isPresent();
            }
        };

        actionSelectNextJoypadKeyBinding = new GameAction("select_next_joypad_binding") {
            @Override
            public void execute(GameAppContext app) {
                app.input().joypad().selectNextBinding();
            }
        };

        final SteeringActions steeringActions = commonGameActions.steeringActions();

        steeringBindings = Set.of(
            new ActionKeyBinding(steeringActions.actionSteer(Direction.UP),    keyForJoypadButton(joypad, JoypadButton.UP),    combine().ctrl().key(KeyCode.UP)),
            new ActionKeyBinding(steeringActions.actionSteer(Direction.DOWN),  keyForJoypadButton(joypad, JoypadButton.DOWN),  combine().ctrl().key(KeyCode.DOWN)),
            new ActionKeyBinding(steeringActions.actionSteer(Direction.LEFT),  keyForJoypadButton(joypad, JoypadButton.LEFT),  combine().ctrl().key(KeyCode.LEFT)),
            new ActionKeyBinding(steeringActions.actionSteer(Direction.RIGHT), keyForJoypadButton(joypad, JoypadButton.RIGHT), combine().ctrl().key(KeyCode.RIGHT))
        );

        localBindings = Set.of(
            new ActionKeyBinding(actionQuitDemoLevel(),                 keyForJoypadButton(joypad, JoypadButton.START)),
            new ActionKeyBinding(actionEnterStartScreen(),              keyForJoypadButton(joypad, JoypadButton.START)),
            new ActionKeyBinding(actionStartPlaying(),                  keyForJoypadButton(joypad, JoypadButton.START)),
            new ActionKeyBinding(actionTogglePacBooster(),              keyForJoypadButton(joypad, JoypadButton.A),
                                                                        keyForJoypadButton(joypad, JoypadButton.B)),
            new ActionKeyBinding(actionTogglePlaySceneDisplayMode(),    combine().alt().key(KeyCode.C)),
            new ActionKeyBinding(actionToggleJoypadBindingsDisplayed(), bareKey(KeyCode.SPACE))
        );
    }

    public Set<ActionKeyBinding> steeringBindings() {
        return steeringBindings;
    }

    public Set<ActionKeyBinding> localBindings() {
        return localBindings;
    }

    public GameAction actionEnterStartScreen() {
        return actionEnterStartScreen;
    }

    public GameAction actionQuitDemoLevel() {
        return actionQuitDemoLevel;
    }

    public GameAction actionStartPlaying() {
        return actionStartPlaying;
    }

    public GameAction actionTogglePlaySceneDisplayMode() {
        return actionTogglePlaySceneDisplayMode;
    }

    public GameAction actionToggleJoypadBindingsDisplayed() {
        return actionToggleJoypadBindingsDisplayed;
    }

    public GameAction actionTogglePacBooster() {
        return actionTogglePacBooster;
    }

    public GameAction actionSelectNextJoypadKeyBinding() {
        return actionSelectNextJoypadKeyBinding;
    }

    private static KeyCodeCombination keyForJoypadButton(Joypad joypad, JoypadButton button) {
        return joypad.keyForButton(button);
    }
}