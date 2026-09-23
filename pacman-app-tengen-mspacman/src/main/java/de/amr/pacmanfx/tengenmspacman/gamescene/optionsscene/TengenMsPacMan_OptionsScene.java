/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.entities.hud.score.Score;
import de.amr.basics.ui.entities.props.imagedisplay.ImageDisplay;
import de.amr.basics.ui.entities.props.textdisplay.TextDisplay;
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
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.input.JoypadButton;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.util.stream.Stream;

import static de.amr.basics.ui.rendering.Renderer.TS;
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

    public record RenderableMenuOption(
        boolean selected,
        String label,
        String value,
        int separatorTileX,
        RenderingLayer layer, int z,
        Vector2f offset
    ) implements Renderable {}

    public record RenderableMenuSeparatorBar(
        float width,
        float height,
        Vector2f offset
    ) implements Renderable {
        @Override
        public RenderingLayer layer() {
            return RenderingLayer.SCENE;
        }
    }

    public static final byte OPTION_PLAY_MODE = 0;
    public static final byte OPTION_PAC_BOOSTER = 1;
    public static final byte OPTION_DIFFICULTY = 2;
    public static final byte OPTION_MAP_CATEGORY = 3;
    public static final byte OPTION_STARTING_LEVEL = 4;

    public static final byte NUM_OPTIONS = 5;

    private static final byte MIN_START_LEVEL = 1;
    private static final byte MAX_START_LEVEL = 32;

    private static final int INITIAL_DELAY = 20; //TODO verify
    private static final int IDLE_TIMEOUT = 1530; // 25,5 sec TODO verify

    private final IntegerProperty selectedOption = new SimpleIntegerProperty() {
        @Override
        protected void invalidated() {
            soundManager().play(TengenMsPacManSoundID.OPTION_SELECTION_CHANGE);
            idleTicks = 0;
        }
    };

    private int idleTicks;
    public int initialDelay;

    private final TextDisplay titleTextDisplay;
    private final TextDisplay moveArrowTextDisplay;
    private final TextDisplay chooseOptionsTextDisplay;
    private final TextDisplay pressStartTextDisplay;
    private final RenderableMenuSeparatorBar topBar;
    private final RenderableMenuSeparatorBar botBar;

    public TengenMsPacMan_OptionsScene() {
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        reqCanvasRendering().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        reqCanvasRendering().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        titleTextDisplay = createTitleTextDisplay();
        moveArrowTextDisplay = createMoveArrowTextDisplay();
        chooseOptionsTextDisplay = createChooseOptionsTextDisplay();
        pressStartTextDisplay = createPressStartTextDisplay();

        topBar = new RenderableMenuSeparatorBar(NES_SCREEN_WIDTH, 8, new Vector2f(0,  2.5f * TS));
        botBar = new RenderableMenuSeparatorBar(NES_SCREEN_WIDTH, 8, new Vector2f(0, 26.5f * TS));
    }

    private TextDisplay createTitleTextDisplay() {
        final TextDisplay textDisplay = new TextDisplay();
        textDisplay.pos().set(7 * TS, 6 * TS);
        textDisplay.data().setFont(GlobalFonts.ARCADE.font(TS));
        textDisplay.data().setText("MS PAC-MAN OPTIONS");
        textDisplay.data().setFillColor(NES_Palette.color(0x28));
        textDisplay.show();
        return textDisplay;
    }

    private TextDisplay createMoveArrowTextDisplay() {
        final TextDisplay textDisplay = new TextDisplay();
        textDisplay.pos().set(4 * TS, 24 * TS);
        textDisplay.data().setFont(GlobalFonts.ARCADE.font(TS));
        textDisplay.data().setText("MOVE ARROW WITH JOYPAD");
        textDisplay.data().setFillColor(NES_Palette.color(0x28));
        textDisplay.show();
        return textDisplay;
    }

    private TextDisplay createChooseOptionsTextDisplay() {
        final TextDisplay textDisplay = new TextDisplay();
        textDisplay.pos().set(2 * TS, 25 * TS);
        textDisplay.data().setFont(GlobalFonts.ARCADE.font(TS));
        textDisplay.data().setText("CHOOSE OPTIONS WITH A AND B");
        textDisplay.data().setFillColor(NES_Palette.color(0x28));
        textDisplay.show();
        return textDisplay;
    }

    private TextDisplay createPressStartTextDisplay() {
        final TextDisplay textDisplay = new TextDisplay();
        textDisplay.pos().set(3 * TS, 26 * TS);
        textDisplay.data().setFont(GlobalFonts.ARCADE.font(TS));
        textDisplay.data().setText("PRESS START TO START GAME");
        textDisplay.data().setFillColor(NES_Palette.color(0x28));
        textDisplay.show();
        return textDisplay;
    }

    private RenderableMenuOption renderablePlayModeOption() {
        return new RenderableMenuOption(
            selectedOption() == OPTION_PLAY_MODE,
            "TYPE",
            "1 PLAYER",
            8,
            RenderingLayer.SCENE, 0,
            new Vector2f(0, 4.5f * TS)
        );
    }

    private RenderableMenuOption renderableBoosterModeOption() {
        final BoosterMode boosterMode = gameOptions(game().session()).boosterMode();
        return new RenderableMenuOption(
            selectedOption() == OPTION_PAC_BOOSTER,
            "PAC BOOSTER",
            switch (boosterMode) {
                case BOOSTER_OFF -> "OFF";
                case BOOSTER_ALWAYS_ON -> "ALWAYS ON";
                case ACTIVATE_WITH_A_OR_B -> "USE A OR B";
            },
            19,
            RenderingLayer.SCENE, 0,
            new Vector2f(0, 6 * TS)
        );
    }

    private RenderableMenuOption renderableGameDifficultyOption() {
        final Difficulty difficulty = gameOptions(game().session()).difficulty();
        return new RenderableMenuOption(
            selectedOption() == OPTION_DIFFICULTY,
            "GAME DIFFICULTY",
            difficulty.name(),
            19,
            RenderingLayer.SCENE, 0,
            new Vector2f(0, 7.5f * TS)
        );
    }

    private RenderableMenuOption renderableMapCategoryOption() {
        final MapCategory mapCategory = gameOptions(game().session()).mapCategory();
        return new RenderableMenuOption(
            selectedOption() == OPTION_MAP_CATEGORY,
            "MAZE SELECTION",
            mapCategory.name(),
            19,
            RenderingLayer.SCENE, 0,
            new Vector2f(0, 9f * TS)
        );
    }

    private RenderableMenuOption renderableStartingLevelOption() {
        final int startLevelNumber = gameOptions(game().session()).startLevelNumber();
        return new RenderableMenuOption(
            selectedOption() == OPTION_STARTING_LEVEL,
            "STARTING LEVEL",
            String.valueOf(startLevelNumber),
            19,
            RenderingLayer.SCENE, 0,
            new Vector2f(0, 10.5f * TS)
        );
    }

    private ImageDisplay createNumContinuesImageDisplay() {
        final int numContinues = gameOptions(game().session()).numContinues();
        final ImageDisplay imageDisplay = new ImageDisplay();
        imageDisplay.pos().set(24 * TS, 20 * TS);
        if (numContinues < 4) {
            final var spriteSheet = TengenMsPacMan_SpriteSheet.instance();
            final RectShort sprite = spriteSheet.findSprite(switch (numContinues) {
                case 0 -> SpriteID.CONTINUES_0;
                case 1 -> SpriteID.CONTINUES_1;
                case 2 -> SpriteID.CONTINUES_2;
                case 3 -> SpriteID.CONTINUES_3;
                default -> throw new IllegalArgumentException("Illegal number of continues: " + numContinues);
            });
            imageDisplay.image().setImage(spriteSheet.image(sprite));
            imageDisplay.show();
        }
        else {
            imageDisplay.hide();
        }
        return imageDisplay;
    }

    @Override
    public Stream<Renderable> renderables() {
        if (initialDelay > 0) return Stream.empty();

        return Ufx.streamOf(
            topBar,
            titleTextDisplay,
            renderablePlayModeOption(),
            renderableBoosterModeOption(),
            renderableGameDifficultyOption(),
            renderableMapCategoryOption(),
            renderableStartingLevelOption(), createNumContinuesImageDisplay(),
            moveArrowTextDisplay,
            chooseOptionsTextDisplay,
            pressStartTextDisplay,
            botBar
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

        selectedOption.set(OPTION_PAC_BOOSTER);
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

    private void optionValueChanged() {
        soundManager().play(TengenMsPacManSoundID.OPTION_VALUE_CHANGE);
        idleTicks = 0;
    }

    public int selectedOption() {
        return selectedOption.get();
    }

    @Override
    public void onInput() {
        final GameSession session = game().session();

        if (app().input().joypad().isButtonPressed(JoypadButton.DOWN)) {
            selectedOption.set(selectedOption() + 1 < NUM_OPTIONS ? selectedOption() + 1 : 0);
        }
        else if (app().input().joypad().isButtonPressed(JoypadButton.UP)) {
            selectedOption.set(selectedOption() == 0 ? NUM_OPTIONS - 1 : selectedOption() - 1);
        }
        // Button "A" on the joypad is located right of "B": select next value
        else if (app().input().joypad().isButtonPressed(JoypadButton.A) || app().input().keyboard().isKeyPressed(KeyCode.RIGHT)) {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setNextPacBoosterValue(session);
                case OPTION_DIFFICULTY     -> setNextDifficultyValue(session);
                case OPTION_MAP_CATEGORY -> setNextMapCategoryValue(session);
                case OPTION_STARTING_LEVEL -> setNextStartLevelValue();
            }
        }
        // Button "B" is left of "A": select previous value
        else if (app().input().joypad().isButtonPressed(JoypadButton.B) || app().input().keyboard().isKeyPressed(KeyCode.LEFT)) {
            switch (selectedOption()) {
                case OPTION_PAC_BOOSTER    -> setPrevPacBoosterValue(session);
                case OPTION_DIFFICULTY     -> setPrevDifficultyValue(session);
                case OPTION_MAP_CATEGORY -> setPrevMapCategoryValue(session);
                case OPTION_STARTING_LEVEL -> setPrevStartLevelValue();
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

    private void saveHighScore() {
        final ScoreSystem scoreSystem = game().playConfig().systems().scoreSystem();
        final Score highScore = game().session().hud().highScore();
        try {
            scoreSystem.save(highScore);
        } catch (IOException x) {
            game().eventManager().publishEvent(new HighScoreAccessErrorEvent(x));
        }
    }
}