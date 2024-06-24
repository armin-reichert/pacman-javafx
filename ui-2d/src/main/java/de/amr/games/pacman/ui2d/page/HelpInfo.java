/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.ui2d.GameContext;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.ui2d.PacManGames2dUI.PY_AUTOPILOT;

/**
 * @author Armin Reichert
 */
public class HelpInfo extends PageInfo {

    public static HelpInfo currentHelpContent(GameContext context) {
        HelpInfo help = new HelpInfo(context);
        switch (context.gameState()) {
            case INTRO -> help.addInfoForIntroScene();
            case CREDIT -> help.addInfoForCreditScene();
            case READY, HUNTING, PACMAN_DYING, GHOST_DYING -> {
                if (context.game().isDemoLevel()) {
                    help.addInfoForDemoLevelPlayScene();
                } else {
                    help.addInfoForPlayScene();
                }
            }
            default -> help.addQuitEntry();
        }
        return help;
    }

    private final GameContext context;

    public HelpInfo(GameContext context) {
        this.context = context;
    }

    public void addLocalizedEntry(String lhsKey, String keyboardKey) {
        addRow(
            label(context.tt(lhsKey), Color.gray(0.9)),
            text("[" + keyboardKey + "]", Color.YELLOW)
        );
    }

    @Override
    public Pane createPane(Color backgroundColor, Font font) {
        var pane = super.createPane(backgroundColor, font);
        var grid = (GridPane) pane.getChildren().get(0); // TODO improve
        // add default entries:
        if (PY_AUTOPILOT.get()) {
            var autoPilotEntry = text(context.tt("help.autopilot_on"), Color.ORANGE);
            autoPilotEntry.setFont(font);
            GridPane.setColumnSpan(autoPilotEntry, 2);
            grid.add(autoPilotEntry, 0, grid.getRowCount());
        }
        if (context.gameController().isPacImmune()) {
            var immunityEntry = text(context.tt("help.immunity_on"), Color.ORANGE);
            immunityEntry.setFont(font);
            GridPane.setColumnSpan(immunityEntry, 2);
            grid.add(immunityEntry, 0, grid.getRowCount() + 1);
        }
        return pane;
    }

    private void addQuitEntry() {
        addLocalizedEntry("help.show_intro", "Q");
    }

    private void addInfoForIntroScene() {
        if (context.gameController().hasCredit()) {
            addLocalizedEntry("help.start_game", "1");
        }
        addLocalizedEntry("help.add_credit", "5");
        addQuitEntry();
    }

    private void addInfoForCreditScene() {
        if (context.gameController().hasCredit()) {
            addLocalizedEntry("help.start_game", "1");
        }
        addLocalizedEntry("help.add_credit", "5");
        addQuitEntry();
    }

    private void addInfoForPlayScene() {
        addLocalizedEntry("help.move_left", context.tt("help.cursor_left"));
        addLocalizedEntry("help.move_right", context.tt("help.cursor_right"));
        addLocalizedEntry("help.move_up", context.tt("help.cursor_up"));
        addLocalizedEntry("help.move_down", context.tt("help.cursor_down"));
        addQuitEntry();
    }

    private void addInfoForDemoLevelPlayScene() {
        addLocalizedEntry("help.add_credit", "5");
        addQuitEntry();
    }
}