/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

public class InfoBoxJoypad extends InfoBox {

    public InfoBoxJoypad(GameUI ui) {
        super(ui);
    }

    public void init(GameUI ui) {
        ResourceManager rm = () -> GameUI_Implementation.class;
        var imageNesController = new ImageView(rm.loadImage("graphics/nes-controller.jpg"));

        setContentTextFont(Font.font("Monospace", 16));

        var joypadKeyBinding = ui.joypad().currentKeyBinding();
        String indent = "  "; // Urgh
        addDynamicLabeledValue("[SELECT]   [START]", () -> "%s%s  %s".formatted(
            indent,
            joypadKeyBinding.key(JoypadButton.SELECT).getDisplayText(),
            joypadKeyBinding.key(JoypadButton.START).getDisplayText())
        );
        addDynamicLabeledValue("[B]  [A]", () -> "%s%s   %s".formatted(
            indent,
            joypadKeyBinding.key(JoypadButton.B).getDisplayText(),
            joypadKeyBinding.key(JoypadButton.A).getDisplayText())
        );
        addDynamicLabeledValue("UP/DOWN/LEFT/RIGHT", () -> "%s%s  %s  %s  %s".formatted(
            indent,
            joypadKeyBinding.key(JoypadButton.UP).getDisplayText(),
            joypadKeyBinding.key(JoypadButton.DOWN).getDisplayText(),
            joypadKeyBinding.key(JoypadButton.LEFT).getDisplayText(),
            joypadKeyBinding.key(JoypadButton.RIGHT).getDisplayText())
        );
        addRow(imageNesController);
    }
}