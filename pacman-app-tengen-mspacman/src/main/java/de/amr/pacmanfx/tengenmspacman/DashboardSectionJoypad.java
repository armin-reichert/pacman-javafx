/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.input.Input;
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

        // NES controller image is located in "pacman-ui" module, use a class from that module to load it
        final ResourceManager resourceManager = () -> AppContext.class;

        final ImageView nesControllerImage = new ImageView(
            resourceManager.loadImage("/de/amr/pacmanfx/ui/graphics/nes-controller.jpg"));
        addRow(nesControllerImage);

        addDynamicLabeledValue("[SELECT]",
            () -> buttonKey(JoypadButton.SELECT));

        addDynamicLabeledValue("[START]",
            () -> buttonKey(JoypadButton.START));

        addDynamicLabeledValue("[B]  [A]",
            () -> "%s   %s".formatted(
                buttonKey(JoypadButton.B), buttonKey(JoypadButton.A)));

        addDynamicLabeledValue("UP/DOWN/LEFT/RIGHT",
            () -> "%s  %s  %s  %s".formatted(
                buttonKey(JoypadButton.UP), buttonKey(JoypadButton.DOWN), buttonKey(JoypadButton.LEFT), buttonKey(JoypadButton.RIGHT))
        );
    }

    private static String buttonKey(JoypadButton button) {
        return Input.instance().joypad.keyForButton(button).getDisplayText();
    }
}