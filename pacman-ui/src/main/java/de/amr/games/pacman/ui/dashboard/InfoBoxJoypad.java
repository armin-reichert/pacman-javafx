/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.lib.nes.NES_JoypadButtonID;
import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import static de.amr.games.pacman.ui.Globals.THE_KEYBOARD;

public class InfoBoxJoypad extends InfoBox {

    public void init() {
        super.init();

        ResourceManager rm = () -> PacManGamesUI.class;
        var imageNesController = new ImageView(rm.loadImage("graphics/nes-controller.jpg"));

        setContentTextFont(Font.font("Monospace", 16));

        var joypad = THE_KEYBOARD.currentJoypadKeyBinding();
        String indent = "  "; // Urgh
        addLabeledValue("[SELECT]   [START]", () -> "%s%s  %s".formatted(
            indent,
            joypad.key(NES_JoypadButtonID.SELECT).getDisplayText(),
            joypad.key(NES_JoypadButtonID.START).getDisplayText())
        );
        addLabeledValue("[B]  [A]", () -> "%s%s   %s".formatted(
            indent,
            joypad.key(NES_JoypadButtonID.B).getDisplayText(),
            joypad.key(NES_JoypadButtonID.A).getDisplayText())
        );
        addLabeledValue("UP/DOWN/LEFT/RIGHT", () -> "%s%s  %s  %s  %s".formatted(
            indent,
            joypad.key(NES_JoypadButtonID.UP).getDisplayText(),
            joypad.key(NES_JoypadButtonID.DOWN).getDisplayText(),
            joypad.key(NES_JoypadButtonID.LEFT).getDisplayText(),
            joypad.key(NES_JoypadButtonID.RIGHT).getDisplayText())
        );
        addRow(imageNesController);
    }
}