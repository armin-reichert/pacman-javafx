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
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.JoypadButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.util.stream.Stream;

import static de.amr.basics.TileDimension.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptions;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

/**
 * Options scene for Ms. Pac-Man Tengen.
 *
 * <p></p>The high-score is cleared if player type (1 player, 2 players etc.), map category or difficulty are
 * changed.
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
            soundManager().play(TengenMsPacManSoundID.OPTION_SELECTION_CHANGE);
            idleTicks = 0;
        }
    };

    private int idleTicks;
    public int initialDelay;

    private final GameEntityView titleTextView;
    private final GameEntityView moveArrowTextView;
    private final GameEntityView chooseOptionsTextView;
    private final GameEntityView pressStartTextView;
    private final MenuSeparatorBarView topBarView;
    private final MenuSeparatorBarView botBarView;

    public TengenMsPacMan_OptionsScene() {
        view2D().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        view2D().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        titleTextView = createTitleTextDisplay();
        moveArrowTextView = createMoveArrowTextDisplay();
        chooseOptionsTextView = createChooseOptionsTextDisplay();
        pressStartTextView = createPressStartTextDisplay();

        topBarView = new MenuSeparatorBarView(NES_SCREEN_WIDTH, TS, new Vector2f(0,  2.5f * TS));
        botBarView = new MenuSeparatorBarView(NES_SCREEN_WIDTH, TS, new Vector2f(0, 26.5f * TS));
    }

    @Override
    public Stream<Renderable> renderables() {
        if (initialDelay > 0) return Stream.empty();

        return Ufx.streamOf(
            createJoypadKeyBindingsView(), // dynamic, depends on current bindings an if set to visible
            createPlayModeOptionView(),
            createBoosterModeOptionView(),
            createGameDifficultyOptionView(),
            createMapCategoryOptionView(),
            createStartingLevelOptionView(),
            titleTextView,
            moveArrowTextView,
            chooseOptionsTextView, pressStartTextView,
            createNumContinuesImageView(),
            topBarView,
            botBarView
        );
    }

    @Override
    public void onActivate() {
        final GameSession session = game().session();
        session.setHudVisible(false);

        final var actions = app().variantManager().currentRuntime()
            .extensionValue(TengenMsPacMan_GameExtension.EXT_ACTIONS, TengenMsPacMan_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.selectAnyMatchingBinding(actions.actionStartPlaying(), actions.localBindings());
        bindingsMap.selectAnyMatchingBinding(actions.actionToggleJoypadBindingsDisplayed(), actions.localBindings());
        bindingsMap.bindActionToKeyCombination(actions.actionSelectNextJoypadKeyBinding(), combine().alt().key(KeyCode.J));
        bindingsMap.registerAllBindings(app().commonActions().sceneTestActions().bindings());

        selectedOption.set(PlayOption.PAC_BOOSTER);
        gameOptions(session).setCanStartNewGame(true);

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
            gameFlow().enterGameState(game(), CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onInput() {
        final GameSession session = game().session();
        final Joypad joypad = app().input().joypad();

        if (joypad.isButtonPressed(JoypadButton.DOWN)) {
            selectedOption.set(selectedOption.get().succ());
        }
        else if (joypad.isButtonPressed(JoypadButton.UP)) {
            selectedOption.set(selectedOption.get().pred());
        }
        // Button "A" on the joypad is located right of "B": select next value
        else if (joypad.isButtonPressed(JoypadButton.A) || app().input().keyboard().isKeyPressed(KeyCode.RIGHT)) {
            switch (selectedOption.get()) {
                case PAC_BOOSTER    -> setNextPacBoosterValue(session);
                case DIFFICULTY     -> setNextDifficultyValue(session);
                case MAP_CATEGORY   -> setNextMapCategoryValue(session);
                case STARTING_LEVEL -> setNextStartLevelValue();
            }
        }
        // Button "B" is left of "A": select previous value
        else if (joypad.isButtonPressed(JoypadButton.B) || app().input().keyboard().isKeyPressed(KeyCode.LEFT)) {
            switch (selectedOption.get()) {
                case PAC_BOOSTER    -> setPrevPacBoosterValue(session);
                case DIFFICULTY     -> setPrevDifficultyValue(session);
                case MAP_CATEGORY   -> setPrevMapCategoryValue(session);
                case STARTING_LEVEL -> setPrevStartLevelValue();
            }
        }
        else {
            super.onInput();
        }
    }

    private void setPrevStartLevelValue() {
        final GameSession session = game().session();

        int current = gameOptions(session).startLevelNumber();
        int prev = (current == MIN_START_LEVEL) ? MAX_START_LEVEL : current - 1;
        gameOptions(session).setStartLevelNumber(prev);

        optionValueChanged();
    }

    private void setNextStartLevelValue() {
        final GameSession session = game().session();

        int current = gameOptions(session).startLevelNumber();
        int next = (current < MAX_START_LEVEL) ? current + 1 : MIN_START_LEVEL;
        gameOptions(session).setStartLevelNumber(next);

        optionValueChanged();
    }

    private void setPrevMapCategoryValue(GameSession session) {
        final MapCategory category = gameOptions(session).mapCategory();
        final var values = MapCategory.values();
        final int current = category.ordinal(), prev = (current == 0) ? values.length - 1 :  current - 1;
        gameOptions(session).setMapCategory(values[prev]);

        saveHighScore();
        optionValueChanged();
    }

    private void setNextMapCategoryValue(GameSession session) {
        final MapCategory category = gameOptions(session).mapCategory();
        var values = MapCategory.values();
        int current = category.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        gameOptions(session).setMapCategory(values[next]);

        saveHighScore();
        optionValueChanged();
    }

    private void setPrevDifficultyValue(GameSession session) {
        final Difficulty difficulty = gameOptions(session).difficulty();
        final var values = Difficulty.values();
        final int current = difficulty.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        gameOptions(session).setDifficulty(values[prev]);

        saveHighScore();
        optionValueChanged();
    }

    private void setNextDifficultyValue(GameSession session) {
        final Difficulty difficulty = gameOptions(session).difficulty();
        final var values = Difficulty.values();
        final int current = difficulty.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        gameOptions(session).setDifficulty(values[next]);

        saveHighScore();
        optionValueChanged();
    }

    private void setPrevPacBoosterValue(GameSession session) {
        final BoosterMode boosterMode = gameOptions(session).boosterMode();
        final var values = BoosterMode.values();
        final int current = boosterMode.ordinal(), prev = (current == 0) ? values.length - 1 : current - 1;
        gameOptions(session).setBoosterMode(values[prev]);

        optionValueChanged();
    }

    private void setNextPacBoosterValue(GameSession session) {
        final BoosterMode boosterMode = gameOptions(session).boosterMode();
        final var values = BoosterMode.values();
        final int current = boosterMode.ordinal(), next = (current == values.length - 1) ? 0 : current + 1;
        gameOptions(session).setBoosterMode(values[next]);

        optionValueChanged();
    }

    private void optionValueChanged() {
        soundManager().play(TengenMsPacManSoundID.OPTION_VALUE_CHANGE);
        idleTicks = 0;
    }

    private void saveHighScore() {
        final ScoreSystem scoreSystem = game().playConfig().systems().scoreSystem();
        final Score highScore = game().session().hud().highScore();
        try {
            scoreSystem.save(highScore);
        } catch (IOException x) {
            game().eventManager().publishEvent(new HighScoreAccessErrorEvent(x));
        }
    }

    private GameEntityView createTitleTextDisplay() {
        return new GameEntityView(
            TextView.create("MS PAC-MAN OPTIONS", NES_Palette.color(0x28), GlobalFonts.ARCADE.font(TS), 7, 6),
            RenderingLayer.PROPS, 0,
            Vector2f.ZERO);
    }

    private GameEntityView createMoveArrowTextDisplay() {
        return new GameEntityView(
            TextView.create("MOVE ARROW WITH JOYPAD", NES_Palette.color(0x28), GlobalFonts.ARCADE.font(TS), 4, 24),
            RenderingLayer.PROPS, 0,
            Vector2f.ZERO);
    }

    private GameEntityView createChooseOptionsTextDisplay() {
        return new GameEntityView(
            TextView.create("CHOOSE OPTIONS WITH A AND B", NES_Palette.color(0x28), GlobalFonts.ARCADE.font(TS), 2, 25),
            RenderingLayer.PROPS, 0,
            Vector2f.ZERO);
    }

    private GameEntityView createPressStartTextDisplay() {
        return new GameEntityView(
            TextView.create("PRESS START TO START GAME", NES_Palette.color(0x28), GlobalFonts.ARCADE.font(TS), 3, 26),
            RenderingLayer.PROPS, 0,
            Vector2f.ZERO);
    }

    private MenuOptionView createPlayModeOptionView() {
        final var option = new MenuOptionView("TYPE", 8, new Vector2f(0, 4.5f * TS));
        option.setSelected(selectedOption.get() == PlayOption.PLAY_MODE);
        option.setValue("1 PLAYER");
        return option;
    }

    private MenuOptionView createBoosterModeOptionView() {
        final BoosterMode boosterMode = gameOptions(game().session()).boosterMode();
        final var option = new MenuOptionView("PAC BOOSTER", 19, new Vector2f(0, 6 * TS));
        option.setSelected(selectedOption.get() == PlayOption.PAC_BOOSTER);
        option.setValue(switch (boosterMode) {
            case BOOSTER_OFF -> "OFF";
            case BOOSTER_ALWAYS_ON -> "ALWAYS ON";
            case ACTIVATE_WITH_A_OR_B -> "USE A OR B";
        });
        return option;
    }

    private MenuOptionView createGameDifficultyOptionView() {
        final Difficulty difficulty = gameOptions(game().session()).difficulty();
        final var option = new MenuOptionView("GAME DIFFICULTY", 19, new Vector2f(0, 7.5f * TS));
        option.setSelected(selectedOption.get() == PlayOption.DIFFICULTY);
        option.setValue(difficulty.name());
        return option;
    }

    private MenuOptionView createMapCategoryOptionView() {
        final MapCategory mapCategory = gameOptions(game().session()).mapCategory();
        final var option = new MenuOptionView("MAZE SELECTION", 19, new Vector2f(0, 9f * TS));
        option.setSelected(selectedOption.get() == PlayOption.MAP_CATEGORY);
        option.setValue(mapCategory.name());
        return option;
    }

    private MenuOptionView createStartingLevelOptionView() {
        final int startLevelNumber = gameOptions(game().session()).startLevelNumber();
        final var option = new MenuOptionView("STARTING LEVEL", 19, new Vector2f(0, 10.5f * TS));
        option.setSelected(selectedOption.get() == PlayOption.STARTING_LEVEL);
        option.setValue(String.valueOf(startLevelNumber));
        return option;
    }

    private GameEntityView createNumContinuesImageView() {
        final int numContinues = gameOptions(game().session()).numContinues();
        final ImageView imageView = new ImageView();
        imageView.pos().set(24 * TS, 20 * TS);
        if (numContinues < 4) {
            final var spriteSheet = TengenMsPacMan_SpriteSheet.instance();
            final RectShort sprite = spriteSheet.findSprite(switch (numContinues) {
                case 0 -> SpriteID.CONTINUES_0;
                case 1 -> SpriteID.CONTINUES_1;
                case 2 -> SpriteID.CONTINUES_2;
                case 3 -> SpriteID.CONTINUES_3;
                default -> throw new IllegalArgumentException("Illegal number of continues: " + numContinues);
            });
            imageView.image().setImage(spriteSheet.createImage(sprite));
            imageView.show();
        }
        else {
            imageView.hide();
        }
        return new GameEntityView(imageView, RenderingLayer.PROPS, 0, Vector2f.ZERO);
    }

    private JoypadKeyBindingsView createJoypadKeyBindingsView() {
        final Joypad joypad = app().input().joypad();
        final var uiSettings = runtime().extensionValue(TengenMsPacMan_GameExtension.EXT_UI_SETTINGS, TengenMsPacMan_UISettings.class);
        final boolean visible = uiSettings.joypadBindingsDisplayed.get();
        return visible ?
            new JoypadKeyBindingsView(joypad.currentKeyBinding(), new Vector2f(0,0))
            : null;
    }
}