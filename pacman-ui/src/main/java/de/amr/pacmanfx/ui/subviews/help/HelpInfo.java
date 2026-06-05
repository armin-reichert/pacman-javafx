/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.help;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.uilib.UfxBackgrounds;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class HelpInfo {

    public static HelpInfo build(AppContext context) {
        final GameModel game = context.currentGame();
        final State<GameContext> state = context.currentGameState();
        final boolean demoLevel = game.isDemoLevelRunning();

        final HelpInfo helpInfo = new HelpInfo(context);
        if (state.nameIsOneOf(GameStateID.GAME_INTRO.name())) {
            helpInfo.addInfoForIntroScene(game);
        }
        else if (state.nameIsOneOf(GameStateID.GAME_PREPARATION.name())) {
            helpInfo.addInfoForCreditScene(game);
        }
        else if (state.nameIsOneOf(
            GameStateID.GAME_OR_LEVEL_STARTING.name(),
            GameStateID.GAME_LEVEL_PLAYING.name(),
            GameStateID.GAME_LEVEL_PACMAN_DYING.name(),
            GameStateID.GAME_LEVEL_EATING_GHOST.name())) {
            if (demoLevel) {
                helpInfo.addInfoForDemoLevelPlayScene();
            } else {
                helpInfo.addInfoForPlayScene(context.ui().translations());
            }
        }
        else {
            helpInfo.addQuitEntry();
        }
        return helpInfo;
    }

    private final AppContext context;
    private final List<Label> column0 = new ArrayList<>();
    private final List<Text> column1 = new ArrayList<>();

    public HelpInfo(AppContext context) {
        this.context = requireNonNull(context);
    }

    public Pane createPane(AppContext context, Color backgroundColor, Font font) {
        var grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        for (int row = 0; row < column0.size(); ++row) {
            grid.add(column0.get(row), 0, row);
            grid.add(column1.get(row), 1, row);
        }
        for (int row = 0; row < column0.size(); ++row) {
            column0.get(row).setFont(font);
            column1.get(row).setFont(font);
        }
        var pane = new BorderPane(grid);
        pane.setPadding(new Insets(10));
        pane.setBackground(UfxBackgrounds.roundedBackground(backgroundColor, 10));

        // add default entries:
        if (context.currentGame().cheats().isPacUsingAutopilot()) {
            var autoPilotEntry = text(context.ui().translations().translate("help.autopilot_on"), Color.ORANGE);
            autoPilotEntry.setFont(font);
            GridPane.setColumnSpan(autoPilotEntry, 2);
            grid.add(autoPilotEntry, 0, grid.getRowCount());
        }
        if (context.currentGame().cheats().isPacImmune()) {
            var immunityEntry = text(context.ui().translations().translate("help.immunity_on"), Color.ORANGE);
            immunityEntry.setFont(font);
            GridPane.setColumnSpan(immunityEntry, 2);
            grid.add(immunityEntry, 0, grid.getRowCount() + 1);
        }
        return pane;
    }

    private void addRow(Label label, Text text) {
        column0.add(label);
        column1.add(text);
    }

    private Label label(String s, Color color) {
        var label = new Label(s);
        label.setTextFill(color);
        return label;
    }

    private Text text(String s, Color color) {
        var text = new Text(s);
        text.setFill(color);
        return text;
    }

    private void addRow(String lhsKey, String keyboardKey) {
        addRow(label(context.ui().translations().translate(lhsKey), Color.gray(0.9)),
            text("[" + keyboardKey + "]", Color.YELLOW));
    }

    private void addQuitEntry() {
        addRow("help.show_intro", "Q");
    }

    private void addInfoForIntroScene(GameModel game) {
        if (game.canStartNewGame()) {
            addRow("help.start_game", "1");
        }
        addRow("help.add_credit", "5");
        addQuitEntry();
    }

    private void addInfoForCreditScene(GameModel game) {
        if (game.canStartNewGame()) {
            addRow("help.start_game", "1");
        }
        addRow("help.add_credit", "5");
        addQuitEntry();
    }

    private void addInfoForPlayScene(TranslationManager translationManager) {
        addRow("help.move_left", translationManager.translate("help.cursor_left"));
        addRow("help.move_right", translationManager.translate("help.cursor_right"));
        addRow("help.move_up",    translationManager.translate("help.cursor_up"));
        addRow("help.move_down",  translationManager.translate("help.cursor_down"));
        addQuitEntry();
    }

    private void addInfoForDemoLevelPlayScene() {
        addRow("help.add_credit", "5");
        addQuitEntry();
    }
}