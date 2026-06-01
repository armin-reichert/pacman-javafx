/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.subviews.dashboard.Dashboard;
import de.amr.pacmanfx.ui.subviews.dashboard.DashboardSection;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.ImageView;

public class DashboardSectionJoypad extends DashboardSection {

    public DashboardSectionJoypad(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void connect(AppContext context) {
        final Joypad joypad = context.input().joypad;

        // NES controller image is located in "pacman-ui" module, use a class from that module to load it
        final ResourceManager resourceManager = () -> AppContext.class;

        final ImageView nesControllerImage = new ImageView(
            resourceManager.loadImage("/de/amr/pacmanfx/ui/graphics/nes-controller.jpg"));
        addRow(nesControllerImage);

        addDynamicLabeledValue("[SELECT]",
            () -> buttonKey(joypad, JoypadButton.SELECT));

        addDynamicLabeledValue("[START]",
            () -> buttonKey(joypad, JoypadButton.START));

        addDynamicLabeledValue("[B]  [A]",
            () -> "%s   %s".formatted(
                buttonKey(joypad, JoypadButton.B), buttonKey(joypad, JoypadButton.A)));

        addDynamicLabeledValue("UP/DOWN/LEFT/RIGHT",
            () -> "%s  %s  %s  %s".formatted(
                buttonKey(joypad, JoypadButton.UP),
                buttonKey(joypad, JoypadButton.DOWN),
                buttonKey(joypad, JoypadButton.LEFT),
                buttonKey(joypad, JoypadButton.RIGHT))
        );
    }

    private static String buttonKey(Joypad joypad, JoypadButton button) {
        return joypad.keyForButton(button).getDisplayText();
    }
}