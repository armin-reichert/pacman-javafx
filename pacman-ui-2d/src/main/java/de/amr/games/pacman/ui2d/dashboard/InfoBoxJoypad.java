/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.input.JoypadKeyBinding;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

public class InfoBoxJoypad extends InfoBox {

    public void init(GameContext context) {
        super.init(context);
        JoypadKeyBinding joypad = context.joypadKeys();
        setContentTextFont(Font.font("Monospace", 16));
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
        addRow(new ImageView(DashboardAssets.IT.image("image.nes-controller")));
    }
}