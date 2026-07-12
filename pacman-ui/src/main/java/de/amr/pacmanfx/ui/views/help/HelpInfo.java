/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.help;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.state.TimedGameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
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

    public static HelpInfo build(GameActionContext actionContext) {
        final GameContext context = actionContext.gameContext();
        final TimedGameState state = context.state();
        final boolean demoLevel = context.gamePlay().isDemoLevelRunning(context.model());

        final HelpInfo helpInfo = new HelpInfo(actionContext);
        if (GameStateID.GAME_INTRO.identifies(state)) {
            helpInfo.addInfoForIntroScene();
        }
        else if (GameStateID.GAME_PREPARATION.identifies(state)) {
            helpInfo.addInfoForCreditScene();
        }
        else if (state.isOneOf(GameStateID.GAME_OR_LEVEL_STARTING, GameStateID.GAME_LEVEL_PLAYING,
            GameStateID.GAME_LEVEL_PACMAN_DYING, GameStateID.GAME_LEVEL_EATING_GHOST)) {
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

    private final GameActionContext actionContext;

    private final List<Label> column0 = new ArrayList<>();
    private final List<Text>  column1 = new ArrayList<>();

    public HelpInfo(GameActionContext actionContext) {
        this.actionContext = requireNonNull(actionContext);
    }

    private String translate(String key, Object... args) {
        return actionContext.ui().translations().translate(key, args);
    }

    public Pane createPane(GameActionContext actionContext, Color backgroundColor, Font font) {
        final var grid = new GridPane();
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
        final var pane = new BorderPane(grid);
        pane.setPadding(new Insets(10));
        pane.setBackground(Ufx.roundedBackground(backgroundColor, 10));

        final GameContext gameContext = actionContext.gameContext();
        final GameCheats cheats = gameContext.cheats();

        // add default entries:
        if (cheats.isPacUsingAutopilot()) {
            final Text autoPilotEntry = text(translate("help.autopilot_on"), Color.ORANGE);
            autoPilotEntry.setFont(font);
            GridPane.setColumnSpan(autoPilotEntry, 2);
            grid.add(autoPilotEntry, 0, grid.getRowCount());
        }
        if (cheats.isPacImmune()) {
            final Text immunityEntry = text(translate("help.immunity_on"), Color.ORANGE);
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
        final var label = new Label(s);
        label.setTextFill(color);
        return label;
    }

    private Text text(String s, Color color) {
        final var text = new Text(s);
        text.setFill(color);
        return text;
    }

    private void addTranslatedEntry(String lhsKey, String keyCode) {
        final Label label = label(translate(lhsKey), Color.gray(0.9));
        final Text keyCodeText = text("[%s]".formatted(keyCode), Color.YELLOW);
        addRow(label, keyCodeText);
    }

    private void addQuitEntry() {
        addTranslatedEntry("help.show_intro", "Q");
    }

    private void addInfoForIntroScene() {
        //TODO make context-sensitive
        addTranslatedEntry("help.start_game", "1");
        addTranslatedEntry("help.add_credit", "5");
        addQuitEntry();
    }

    private void addInfoForCreditScene() {
        addTranslatedEntry("help.start_game", "1");
        addTranslatedEntry("help.add_credit", "5");
        addQuitEntry();
    }

    private void addInfoForPlayScene() {
        addTranslatedEntry("help.move_left",  translate("help.cursor_left"));
        addTranslatedEntry("help.move_right", translate("help.cursor_right"));
        addTranslatedEntry("help.move_up",    translate("help.cursor_up"));
        addTranslatedEntry("help.move_down",  translate("help.cursor_down"));
        addQuitEntry();
    }

    private void addInfoForDemoLevelPlayScene() {
        addTranslatedEntry("help.add_credit", "5");
        addQuitEntry();
    }
}