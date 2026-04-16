/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardSection;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.ImageView;

public class DashboardSectionJoypad extends DashboardSection {

    private static final ResourceManager LOCAL_RESOURCES = () -> GameUI_Implementation.class;

    public DashboardSectionJoypad(Dashboard dashboard) {
        super(dashboard);
    }

    public void init(GameUI ui) {
        final var imageNesController = new ImageView(LOCAL_RESOURCES.loadImage("graphics/nes-controller.jpg"));

        addRow(imageNesController);
        addDynamicLabeledValue("[SELECT]", () -> "%s".formatted(
            Input.instance().joypad.keyForButton(JoypadButton.SELECT).getDisplayText())
        );
        addDynamicLabeledValue("[START]", () -> "%s".formatted(
            Input.instance().joypad.keyForButton(JoypadButton.START).getDisplayText())
        );
        addDynamicLabeledValue("[B]  [A]", () -> "%s   %s".formatted(
            Input.instance().joypad.keyForButton(JoypadButton.B).getDisplayText(),
            Input.instance().joypad.keyForButton(JoypadButton.A).getDisplayText())
        );
        addDynamicLabeledValue("UP/DOWN/LEFT/RIGHT", () -> "%s  %s  %s  %s".formatted(
            Input.instance().joypad.keyForButton(JoypadButton.UP).getDisplayText(),
            Input.instance().joypad.keyForButton(JoypadButton.DOWN).getDisplayText(),
            Input.instance().joypad.keyForButton(JoypadButton.LEFT).getDisplayText(),
            Input.instance().joypad.keyForButton(JoypadButton.RIGHT).getDisplayText())
        );
    }
}