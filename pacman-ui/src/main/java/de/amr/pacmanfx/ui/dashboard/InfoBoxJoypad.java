/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui.PacManGamesUI;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.ui.PacManGamesEnvironment.THE_JOYPAD;

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
            joypadKeyBinding.key(JoypadButton.SELECT).getDisplayText(),
            joypadKeyBinding.key(JoypadButton.START).getDisplayText())
        );
        addLabeledValue("[B]  [A]", () -> "%s%s   %s".formatted(
            indent,
            joypadKeyBinding.key(JoypadButton.B).getDisplayText(),
            joypadKeyBinding.key(JoypadButton.A).getDisplayText())
        );
        addLabeledValue("UP/DOWN/LEFT/RIGHT", () -> "%s%s  %s  %s  %s".formatted(
            indent,
            joypadKeyBinding.key(JoypadButton.UP).getDisplayText(),
            joypadKeyBinding.key(JoypadButton.DOWN).getDisplayText(),
            joypadKeyBinding.key(JoypadButton.LEFT).getDisplayText(),
            joypadKeyBinding.key(JoypadButton.RIGHT).getDisplayText())
        );
        addRow(imageNesController);
    }
}