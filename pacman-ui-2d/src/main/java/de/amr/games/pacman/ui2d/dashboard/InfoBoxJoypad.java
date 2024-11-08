/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.input.JoypadKeyAdapter;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

public class InfoBoxJoypad extends InfoBox {

    public void init(GameContext context) {
        super.init(context);
        JoypadKeyAdapter joypad = context.joypad();
        setContentTextFont(Font.font("Monospace", 16));
        String indent = "  "; // Urgh
        labeledValue("[SELECT]   [START]", () -> "%s%s  %s".formatted(
            indent,
            joypad.mapControToKey(NES.Joypad.SELECT).getDisplayText(),
            joypad.mapControToKey(NES.Joypad.START).getDisplayText())
        );
        labeledValue("[B]  [A]", () -> "%s%s   %s".formatted(
            indent,
            joypad.mapControToKey(NES.Joypad.B).getDisplayText(),
            joypad.mapControToKey(NES.Joypad.A).getDisplayText())
        );
        labeledValue("UP/DOWN/LEFT/RIGHT", () -> "%s%s  %s  %s  %s".formatted(
            indent,
            joypad.mapControToKey(NES.Joypad.UP).getDisplayText(),
            joypad.mapControToKey(NES.Joypad.DOWN).getDisplayText(),
            joypad.mapControToKey(NES.Joypad.LEFT).getDisplayText(),
            joypad.mapControToKey(NES.Joypad.RIGHT).getDisplayText())
        );
        addRow(new ImageView(context.assets().image("tengen.image.nes-controller")));
    }
}