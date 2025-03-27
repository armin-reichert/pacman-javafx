/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import static de.amr.games.pacman.ui.Globals.THE_UI;

public class InfoBoxJoypad extends InfoBox {

    private JoypadKeyBinding joypad;

    public void init() {
        super.init();

        ResourceManager rm = () -> PacManGamesUI.class;
        var imageNesController = new ImageView(rm.loadImage("graphics/nes-controller.jpg"));

        setContentTextFont(Font.font("Monospace", 16));

        joypad = THE_UI.keyboard().joypadKeyBinding();
        String indent = "  "; // Urgh
        addLabeledValue("[SELECT]   [START]", () -> "%s%s  %s".formatted(
            indent,
            joypad.key(NES_JoypadButton.BTN_SELECT).getDisplayText(),
            joypad.key(NES_JoypadButton.BTN_START).getDisplayText())
        );
        addLabeledValue("[B]  [A]", () -> "%s%s   %s".formatted(
            indent,
            joypad.key(NES_JoypadButton.BTN_B).getDisplayText(),
            joypad.key(NES_JoypadButton.BTN_A).getDisplayText())
        );
        addLabeledValue("UP/DOWN/LEFT/RIGHT", () -> "%s%s  %s  %s  %s".formatted(
            indent,
            joypad.key(NES_JoypadButton.BTN_UP).getDisplayText(),
            joypad.key(NES_JoypadButton.BTN_DOWN).getDisplayText(),
            joypad.key(NES_JoypadButton.BTN_LEFT).getDisplayText(),
            joypad.key(NES_JoypadButton.BTN_RIGHT).getDisplayText())
        );
        addRow(imageNesController);
    }

    @Override
    public void update() {
        joypad = THE_UI.keyboard().joypadKeyBinding();
        super.update();
    }
}