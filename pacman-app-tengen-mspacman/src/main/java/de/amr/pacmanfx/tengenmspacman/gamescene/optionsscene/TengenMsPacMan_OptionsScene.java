/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene;

import de.amr.basics.EnumMethods;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.entities.hud.score.Score;
import de.amr.basics.ui.entities.props.imagedisplay.ImageView;
import de.amr.basics.ui.entities.props.textview.TextView;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.hud.ScoreSystem;
import de.amr.pacmanfx.core.event.HighScoreAccessErrorEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.input.Keyboard;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacManSoundID;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.engine.input.Joypad;
import de.amr.pacmanfx.engine.input.JoypadButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.util.stream.Stream;

import static de.amr.basics.TileDimension.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptionValues;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

/**
 * Options scene for Ms. Pac-Man Tengen.
 *
 * <p>The high-score is cleared if player type (1 player, 2 players etc.), map category or difficulty are
 * changed.</p>
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly/blob/main/MsPacManTENGENDis.asm:9545">Disassembly</a>.
 */
public class TengenMsPacMan_OptionsScene extends AbstractGameScene {

    public enum PlayOption implements EnumMethods<PlayOption> {
        PLAY_MODE, PAC_BOOSTER, DIFFICULTY, MAP_CATEGORY, STARTING_LEVEL;

        @Override
        public Class<PlayOption> enumClass() {
            return PlayOption.class;
        }
    }

    private static final int MIN_START_LEVEL = 1;
    private static final int MAX_START_LEVEL = 32;

    private static final int INITIAL_DELAY = 20; //TODO verify
    private static final int IDLE_TIMEOUT = 1530; // 25,5 sec TODO verify

    private final ObjectProperty<PlayOption> selectedOption = new SimpleObjectProperty<>(PlayOption.PLAY_MODE) {
        @Override
        protected void invalidated() {
            actionContext().soundManager().play(TengenMsPacManSoundID.OPTION_SELECTION_CHANGE);
            idleTicks = 0;
        }
    };

    private int idleTicks;
    private int initialDelay;

    private final GameEntityView titleTextView;
    private final GameEntityView moveArrowTextView;
    private final GameEntityView chooseOptionsTextView;
    private final GameEntityView pressStartTextView;
    private final MenuSeparatorBarView topBarView;
    private final MenuSeparatorBarView botBarView;

    private final MenuOptionView playModeOptionView;
    private final MenuOptionView boosterModeOptionView;
    private final MenuOptionView difficultyOptionView;
    private final MenuOptionView mapCategoryOptionView;
    private final MenuOptionView startingLevelOptionView;

    private final ImageView numContinuesImageView;
    private final GameEntityView numContinuesView;

    public TengenMsPacMan_OptionsScene() {
        view2D().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        view2D().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        titleTextView = new GameEntityView(
            TextView.create("MS PAC-MAN OPTIONS", NES_Palette.color(0x28), GlobalFonts.ARCADE.font(TS), 7, 6),
            RenderingLayer.PROPS, 0, Vector2f.ZERO);

        playModeOptionView      = new MenuOptionView("TYPE", 8, new Vector2f(0, 4.5f * TS));
        boosterModeOptionView   = new MenuOptionView("PAC BOOSTER", 19, new Vector2f(0, 6 * TS));
        difficultyOptionView    = new MenuOptionView("GAME DIFFICULTY", 19, new Vector2f(0, 7.5f * TS));
        mapCategoryOptionView   = new MenuOptionView("MAZE SELECTION", 19, new Vector2f(0, 9f * TS));
        startingLevelOptionView = new MenuOptionView("STARTING LEVEL", 19, new Vector2f(0, 10.5f * TS));

        numContinuesImageView = new ImageView();
        numContinuesImageView.pos().set(24 * TS, 20 * TS);
        numContinuesView = new GameEntityView(numContinuesImageView, RenderingLayer.PROPS, 0, Vector2f.ZERO);

        moveArrowTextView = new GameEntityView(
            TextView.create("MOVE ARROW WITH JOYPAD", NES_Palette.color(0x28), GlobalFonts.ARCADE.font(TS), 4, 24),
            RenderingLayer.PROPS, 0, Vector2f.ZERO);

        chooseOptionsTextView = new GameEntityView(
            TextView.create("CHOOSE OPTIONS WITH A AND B", NES_Palette.color(0x28), GlobalFonts.ARCADE.font(TS), 2, 25),
            RenderingLayer.PROPS, 0, Vector2f.ZERO);

        pressStartTextView = new GameEntityView(
            TextView.create("PRESS START TO START GAME", NES_Palette.color(0x28), GlobalFonts.ARCADE.font(TS), 3, 26),
            RenderingLayer.PROPS, 0, Vector2f.ZERO);

        topBarView = new MenuSeparatorBarView(NES_SCREEN_WIDTH, TS, new Vector2f(0,  2.5f * TS));
        botBarView = new MenuSeparatorBarView(NES_SCREEN_WIDTH, TS, new Vector2f(0, 26.5f * TS));
    }

    @Override
    public Stream<Renderable> renderables() {
        if (initialDelay > 0) return Stream.empty();

        return Ufx.streamOf(
            createJoypadKeyBindingsView(actionContext().input()), // dynamic, depends on current bindings and visibility
            playModeOptionView,
            boosterModeOptionView,
            difficultyOptionView,
            mapCategoryOptionView,
            numContinuesView,
            startingLevelOptionView,
            titleTextView,
            moveArrowTextView,
            chooseOptionsTextView,
            pressStartTextView,
            topBarView,
            botBarView
        );
    }

    @Override
    public void onActivate() {
        final GameSession session = actionContext().currentGame().session();
        session.setHudVisible(false);

        final var actions = actionContext().gameVariantManager().currentRuntime()
            .extensionValue(TengenMsPacMan_GameExtension.EXT_ACTIONS, TengenMsPacMan_Actions.class);

        actionBindingsRegistry().selectAnyMatchingBinding(actions.actionStartPlaying(), actions.localBindings());
        actionBindingsRegistry().selectAnyMatchingBinding(actions.actionToggleJoypadBindingsDisplayed(), actions.localBindings());
        actionBindingsRegistry().bindActionToKeyCombination(actions.actionSelectNextJoypadKeyBinding(), combine().alt().key(KeyCode.J));
        actionBindingsRegistry().registerAllBindings(CommonGameActions.instance().sceneTestActions().bindings());

        selectedOption.set(PlayOption.PAC_BOOSTER);
        gameOptionValues(session).setCanStartNewGame(true);

        idleTicks = 0;
        initialDelay = INITIAL_DELAY;
    }

    @Override
    public void onTick(GameContext game) {
        if (initialDelay > 0) {
            --initialDelay;
            return;
        }
        if (idleTicks < IDLE_TIMEOUT) {
            idleTicks += 1;
        } else {
            actionContext().gameVariantManager().currentRuntime().playConfig().gameFlow()
                .enterGameState(game, CommonGameStateID.GAME_INTRO);
        }
        updateOptions(game.session());
    }

    @Override
    public void onInput() {
        final GameSession session = actionContext().currentGame().session();
        final Keyboard keyboard = actionContext().input().keyboard();
        final Joypad joypad = actionContext().input().joypad();

        if (joypad.isButtonPressed(JoypadButton.DOWN)) {
            selectedOption.set(selectedOption.get().succ());
        }
        else if (joypad.isButtonPressed(JoypadButton.UP)) {
            selectedOption.set(selectedOption.get().pred());
        }
        // Button "A" on the joypad is located right of "B": select next value
        else if (joypad.isButtonPressed(JoypadButton.A) || keyboard.isKeyPressed(KeyCode.RIGHT)) {
            switch (selectedOption.get()) {
                case PAC_BOOSTER    -> setNextPacBoosterValue(session);
                case DIFFICULTY     -> setNextDifficultyValue(session);
                case MAP_CATEGORY   -> setNextMapCategoryValue(session);
                case STARTING_LEVEL -> setNextStartLevelValue(session);
            }
        }
        // Button "B" is left of "A": select previous value
        else if (joypad.isButtonPressed(JoypadButton.B) || keyboard.isKeyPressed(KeyCode.LEFT)) {
            switch (selectedOption.get()) {
                case PAC_BOOSTER    -> setPrevPacBoosterValue(session);
                case DIFFICULTY     -> setPrevDifficultyValue(session);
                case MAP_CATEGORY   -> setPrevMapCategoryValue(session);
                case STARTING_LEVEL -> setPrevStartLevelValue(session);
            }
        }
        else {
            super.onInput();
        }
    }

    private void updateOptions(GameSession session) {
        // This is not strictly necessary because play mode cannot be changed, but...
        playModeOptionView.setSelected(selectedOption.get() == PlayOption.PLAY_MODE);
        playModeOptionView.setValue("1 PLAYER");

        boosterModeOptionView.setSelected(selectedOption.get() == PlayOption.PAC_BOOSTER);
        final BoosterMode boosterMode = gameOptionValues(session).boosterMode();
        boosterModeOptionView.setValue(switch (boosterMode) {
            case BOOSTER_OFF -> "OFF";
            case BOOSTER_ALWAYS_ON -> "ALWAYS ON";
            case ACTIVATE_WITH_A_OR_B -> "USE A OR B";
        });

        final Difficulty difficulty = gameOptionValues(session).difficulty();
        difficultyOptionView.setSelected(selectedOption.get() == PlayOption.DIFFICULTY);
        difficultyOptionView.setValue(difficulty.name());

        final MapCategory mapCategory = gameOptionValues(session).mapCategory();
        mapCategoryOptionView.setSelected(selectedOption.get() == PlayOption.MAP_CATEGORY);
        mapCategoryOptionView.setValue(mapCategory.name());

        final int startLevelNumber = gameOptionValues(session).startLevelNumber();
        startingLevelOptionView.setSelected(selectedOption.get() == PlayOption.STARTING_LEVEL);
        startingLevelOptionView.setValue(String.valueOf(startLevelNumber));

        final int numContinues = gameOptionValues(session).numContinues();
        if (numContinues < 4) {
            final var spriteSheet = TengenMsPacMan_SpriteSheet.instance();
            final RectShort sprite = spriteSheet.findSprite(switch (numContinues) {
                case 0 -> SpriteID.CONTINUES_0;
                case 1 -> SpriteID.CONTINUES_1;
                case 2 -> SpriteID.CONTINUES_2;
                case 3 -> SpriteID.CONTINUES_3;
                default -> throw new IllegalStateException("Unexpected value: " + numContinues);
            });
            numContinuesImageView.image().setImage(spriteSheet.createImage(sprite));
            numContinuesImageView.show();
        }
        else {
            numContinuesImageView.hide();
        }
    }

    private void setPrevStartLevelValue(GameSession session) {
        int current = gameOptionValues(session).startLevelNumber();
        int prev = (current == MIN_START_LEVEL) ? MAX_START_LEVEL : current - 1;
        gameOptionValues(session).setStartLevelNumber(prev);

        optionValueChanged();
    }

    private void setNextStartLevelValue(GameSession session) {
        int current = gameOptionValues(session).startLevelNumber();
        int next = (current < MAX_START_LEVEL) ? current + 1 : MIN_START_LEVEL;
        gameOptionValues(session).setStartLevelNumber(next);

        optionValueChanged();
    }

    private void setPrevMapCategoryValue(GameSession session) {
        final MapCategory category = gameOptionValues(session).mapCategory();
        gameOptionValues(session).setMapCategory(category.pred());
        saveHighScore(actionContext().currentGame());
        optionValueChanged();
    }

    private void setNextMapCategoryValue(GameSession session) {
        final MapCategory category = gameOptionValues(session).mapCategory();
        gameOptionValues(session).setMapCategory(category.succ());
        saveHighScore(actionContext().currentGame());
        optionValueChanged();
    }

    private void setPrevDifficultyValue(GameSession session) {
        final Difficulty difficulty = gameOptionValues(session).difficulty();
        gameOptionValues(session).setDifficulty(difficulty.pred());
        saveHighScore(actionContext().currentGame());
        optionValueChanged();
    }

    private void setNextDifficultyValue(GameSession session) {
        final Difficulty difficulty = gameOptionValues(session).difficulty();
        gameOptionValues(session).setDifficulty(difficulty.succ());
        saveHighScore(actionContext().currentGame());
        optionValueChanged();
    }

    private void setPrevPacBoosterValue(GameSession session) {
        final BoosterMode boosterMode = gameOptionValues(session).boosterMode();
        gameOptionValues(session).setBoosterMode(boosterMode.pred());
        optionValueChanged();
    }

    private void setNextPacBoosterValue(GameSession session) {
        final BoosterMode boosterMode = gameOptionValues(session).boosterMode();
        gameOptionValues(session).setBoosterMode(boosterMode.succ());
        optionValueChanged();
    }

    private void optionValueChanged() {
        actionContext().soundManager().play(TengenMsPacManSoundID.OPTION_VALUE_CHANGE);
        idleTicks = 0;
    }

    private void saveHighScore(GameContext game) {
        final ScoreSystem scoreSystem = game.playConfig().systems().scoreSystem();
        final Score highScore = game.session().hud().highScore();
        try {
            scoreSystem.save(highScore);
        } catch (IOException x) {
            game.eventManager().publishEvent(new HighScoreAccessErrorEvent(x));
        }
    }

    private JoypadKeyBindingsView createJoypadKeyBindingsView(Input input) {
        final Joypad joypad = input.joypad();
        final var uiSettings = actionContext().gameVariantManager().currentRuntime()
            .extensionValue(TengenMsPacMan_GameExtension.EXT_UI_SETTINGS, TengenMsPacMan_UISettings.class);
        final boolean visible = uiSettings.joypadBindingsDisplayed.get();
        return visible ?
            new JoypadKeyBindingsView(joypad.currentKeyBinding(), new Vector2f(0,0))
            : null;
    }
}