/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl.StateName;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.Ufx;
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

    public static HelpInfo build(GameUI ui) {
        final Game game = ui.gameContext().currentGame();
        final boolean demoLevel = game.optGameLevel().isPresent() && game.level().isDemoLevel();
        final StateMachine.State<?> state = game.control().state();

        HelpInfo helpInfo = new HelpInfo(ui);
        if (state.matches(StateName.INTRO)) {
            helpInfo.addInfoForIntroScene(game);
        }
        else if (state.matches(StateName.SETTING_OPTIONS_FOR_START)) {
            helpInfo.addInfoForCreditScene(game);
        }
        else if (state.matches(StateName.STARTING_GAME_OR_LEVEL, StateName.HUNTING, StateName.PACMAN_DYING, StateName.EATING_GHOST)) {
            if (demoLevel) {
                helpInfo.addInfoForDemoLevelPlayScene();
            } else {
                helpInfo.addInfoForPlayScene();
            }
        }
        else {
            helpInfo.addQuitEntry();
        }
        return helpInfo;
    }

    private final GameUI ui;
    private final List<Label> column0 = new ArrayList<>();
    private final List<Text> column1 = new ArrayList<>();

    public HelpInfo(GameUI ui) {
        this.ui = requireNonNull(ui);
    }

    public Pane createPane(GameUI ui, Color backgroundColor, Font font) {
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
        pane.setBackground(Ufx.roundedBackground(backgroundColor, 10));

        // add default entries:
        if (ui.gameContext().currentGame().isUsingAutopilot()) {
            var autoPilotEntry = text(ui.translate("help.autopilot_on"), Color.ORANGE);
            autoPilotEntry.setFont(font);
            GridPane.setColumnSpan(autoPilotEntry, 2);
            grid.add(autoPilotEntry, 0, grid.getRowCount());
        }
        if (ui.gameContext().currentGame().isImmune()) {
            var immunityEntry = text(ui.translate("help.immunity_on"), Color.ORANGE);
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
        addRow(label(ui.translate(lhsKey), Color.gray(0.9)), text("[" + keyboardKey + "]", Color.YELLOW));
    }

    private void addQuitEntry() {
        addRow("help.show_intro", "Q");
    }

    private void addInfoForIntroScene(Game game) {
        if (game.canStartNewGame()) {
            addRow("help.start_game", "1");
        }
        addRow("help.add_credit", "5");
        addQuitEntry();
    }

    private void addInfoForCreditScene(Game game) {
        if (game.canStartNewGame()) {
            addRow("help.start_game", "1");
        }
        addRow("help.add_credit", "5");
        addQuitEntry();
    }

    private void addInfoForPlayScene() {
        addRow("help.move_left", ui.translate("help.cursor_left"));
        addRow("help.move_right", ui.translate("help.cursor_right"));
        addRow("help.move_up", ui.translate("help.cursor_up"));
        addRow("help.move_down", ui.translate("help.cursor_down"));
        addQuitEntry();
    }

    private void addInfoForDemoLevelPlayScene() {
        addRow("help.add_credit", "5");
        addQuitEntry();
    }
}