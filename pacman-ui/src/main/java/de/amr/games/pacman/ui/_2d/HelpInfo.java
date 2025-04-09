/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.ui.Globals;
import de.amr.games.pacman.uilib.Ufx;
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

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.Globals.THE_ASSETS;


/**
 * @author Armin Reichert
 */
public class HelpInfo {

    public static HelpInfo build() {
        HelpInfo help = new HelpInfo();
        switch (THE_GAME_CONTROLLER.state()) {
            case INTRO -> help.addInfoForIntroScene();
            case SETTING_OPTIONS -> help.addInfoForCreditScene();
            case STARTING_GAME, HUNTING, PACMAN_DYING, GHOST_DYING -> {
                if (THE_GAME_CONTROLLER.game().isDemoLevel()) {
                    help.addInfoForDemoLevelPlayScene();
                } else {
                    help.addInfoForPlayScene();
                }
            }
            default -> help.addQuitEntry();
        }
        return help;
    }

    private final List<Label> column0 = new ArrayList<>();
    private final List<Text> column1 = new ArrayList<>();

    public HelpInfo() {
    }

    public Pane createPane(Color backgroundColor, Font font) {
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
        pane.setBackground(Ufx.coloredRoundedBackground(backgroundColor, 10));

        // add default entries:
        if (Globals.PY_AUTOPILOT.get()) {
            var autoPilotEntry = text(THE_ASSETS.text("help.autopilot_on"), Color.ORANGE);
            autoPilotEntry.setFont(font);
            GridPane.setColumnSpan(autoPilotEntry, 2);
            grid.add(autoPilotEntry, 0, grid.getRowCount());
        }
        if (Globals.PY_IMMUNITY.get()) {
            var immunityEntry = text(THE_ASSETS.text("help.immunity_on"), Color.ORANGE);
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
        addRow(label(THE_ASSETS.text(lhsKey), Color.gray(0.9)), text("[" + keyboardKey + "]", Color.YELLOW));
    }

    private void addQuitEntry() {
        addRow("help.show_intro", "Q");
    }

    private void addInfoForIntroScene() {
        if (THE_GAME_CONTROLLER.game().canStartNewGame()) {
            addRow("help.start_game", "1");
        }
        addRow("help.add_credit", "5");
        addQuitEntry();
    }

    private void addInfoForCreditScene() {
        if (THE_GAME_CONTROLLER.game().canStartNewGame()) {
            addRow("help.start_game", "1");
        }
        addRow("help.add_credit", "5");
        addQuitEntry();
    }

    private void addInfoForPlayScene() {
        addRow("help.move_left", THE_ASSETS.text("help.cursor_left"));
        addRow("help.move_right", THE_ASSETS.text("help.cursor_right"));
        addRow("help.move_up", THE_ASSETS.text("help.cursor_up"));
        addRow("help.move_down", THE_ASSETS.text("help.cursor_down"));
        addQuitEntry();
    }

    private void addInfoForDemoLevelPlayScene() {
        addRow("help.add_credit", "5");
        addQuitEntry();
    }
}