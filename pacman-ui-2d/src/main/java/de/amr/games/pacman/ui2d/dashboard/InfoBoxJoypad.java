/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui2d.GameContext;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

public class InfoBoxJoypad extends InfoBox {

    public void init(GameContext context) {
        super.init(context);
        setContentTextFont(Font.font("Sans", 16));
        String indent = "         "; // Urgh
        labeledValue("[SELECT]   [START]", () -> "%s%s   %s".formatted(
            indent,
            context.joypad().select().getDisplayText(),
             context.joypad().start().getDisplayText())
        );
        labeledValue("[B]  [A]", () -> "%s%s   %s".formatted(
            indent,
            context.joypad().b().getDisplayText(),
            context.joypad().a().getDisplayText())
        );
        labeledValue("UP/DOWN/LEFT/RIGHT", () -> "%s%s  %s  %s  %s".formatted(
            indent,
            context.joypad().up().getDisplayText(),
            context.joypad().down().getDisplayText(),
            context.joypad().left().getDisplayText(),
            context.joypad().right().getDisplayText())
        );
        addRow(new ImageView(context.assets().image("tengen.image.nes-controller")));
    }
}