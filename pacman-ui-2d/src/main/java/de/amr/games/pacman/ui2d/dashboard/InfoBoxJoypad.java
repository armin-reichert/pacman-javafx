/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.ui2d.GameContext;
import javafx.scene.text.Font;

public class InfoBoxJoypad extends InfoBox {

    public void init(GameContext context) {
        super.init(context);
        setContentTextFont(Font.font("Sans", 24));
        labeledValue("SELECT",   () -> context.joypad().select().getDisplayText());
        labeledValue("START",    () -> context.joypad().start().getDisplayText());
        labeledValue("B",        () -> context.joypad().b().getDisplayText());
        labeledValue("A",        () -> context.joypad().a().getDisplayText());
        labeledValue("UP",       () -> context.joypad().up().getDisplayText());
        labeledValue("DOWN",     () -> context.joypad().down().getDisplayText());
        labeledValue("LEFT",     () -> context.joypad().left().getDisplayText());
        labeledValue("RIGHT",    () -> context.joypad().right().getDisplayText());
    }
}