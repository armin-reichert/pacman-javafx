/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.input.JoypadKeyBinding;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

public class InfoBoxJoypad extends InfoBox {

    private JoypadKeyBinding joypad;
    public void init(GameContext context) {
        super.init(context);
        setContentTextFont(Font.font("Monospace", 16));
        String indent = "  "; // Urgh
        joypad = context.currentJoypadKeyBinding();
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

    @Override
    public void update() {
        joypad = context.currentJoypadKeyBinding();
        super.update();
    }
}