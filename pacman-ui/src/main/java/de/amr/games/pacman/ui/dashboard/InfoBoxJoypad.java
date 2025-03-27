/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import static de.amr.games.pacman.ui.Globals.THE_UI;

public class InfoBoxJoypad extends InfoBox {

    public void init() {
        super.init();

        ResourceManager rm = () -> PacManGamesUI.class;
        var imageNesController = new ImageView(rm.loadImage("graphics/nes-controller.jpg"));

        setContentTextFont(Font.font("Monospace", 16));

        var joypad = THE_UI.keyboard().selectedJoypad();
        String indent = "  "; // Urgh
        addLabeledValue("[SELECT]   [START]", () -> "%s%s  %s".formatted(
            indent,
            joypad.key(NES_JoypadButton.BUTTON_SELECT).getDisplayText(),
            joypad.key(NES_JoypadButton.BUTTON_START).getDisplayText())
        );
        addLabeledValue("[B]  [A]", () -> "%s%s   %s".formatted(
            indent,
            joypad.key(NES_JoypadButton.BUTTON_B).getDisplayText(),
            joypad.key(NES_JoypadButton.BUTTON_A).getDisplayText())
        );
        addLabeledValue("UP/DOWN/LEFT/RIGHT", () -> "%s%s  %s  %s  %s".formatted(
            indent,
            joypad.key(NES_JoypadButton.BUTTON_UP).getDisplayText(),
            joypad.key(NES_JoypadButton.BUTTON_DOWN).getDisplayText(),
            joypad.key(NES_JoypadButton.BUTTON_LEFT).getDisplayText(),
            joypad.key(NES_JoypadButton.BUTTON_RIGHT).getDisplayText())
        );
        addRow(imageNesController);
    }
}