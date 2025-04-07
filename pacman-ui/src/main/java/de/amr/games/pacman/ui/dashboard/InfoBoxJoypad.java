/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.lib.nes.JoypadButtonID;
import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import static de.amr.games.pacman.ui.Globals.THE_JOYPAD;

public class InfoBoxJoypad extends InfoBox {

    public void init() {
        super.init();

        ResourceManager rm = () -> PacManGamesUI.class;
        var imageNesController = new ImageView(rm.loadImage("graphics/nes-controller.jpg"));

        setContentTextFont(Font.font("Monospace", 16));

        var joypadKeyBinding = THE_JOYPAD.currentKeyBinding();
        String indent = "  "; // Urgh
        addLabeledValue("[SELECT]   [START]", () -> "%s%s  %s".formatted(
            indent,
            joypadKeyBinding.key(JoypadButtonID.SELECT).getDisplayText(),
            joypadKeyBinding.key(JoypadButtonID.START).getDisplayText())
        );
        addLabeledValue("[B]  [A]", () -> "%s%s   %s".formatted(
            indent,
            joypadKeyBinding.key(JoypadButtonID.B).getDisplayText(),
            joypadKeyBinding.key(JoypadButtonID.A).getDisplayText())
        );
        addLabeledValue("UP/DOWN/LEFT/RIGHT", () -> "%s%s  %s  %s  %s".formatted(
            indent,
            joypadKeyBinding.key(JoypadButtonID.UP).getDisplayText(),
            joypadKeyBinding.key(JoypadButtonID.DOWN).getDisplayText(),
            joypadKeyBinding.key(JoypadButtonID.LEFT).getDisplayText(),
            joypadKeyBinding.key(JoypadButtonID.RIGHT).getDisplayText())
        );
        addRow(imageNesController);
    }
}