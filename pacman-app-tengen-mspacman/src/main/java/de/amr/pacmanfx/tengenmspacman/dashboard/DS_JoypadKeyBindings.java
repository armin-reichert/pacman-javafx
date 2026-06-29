/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.dashboard;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameVariant;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.views.dashboard.GameDashboardSection;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.ImageView;

public class DS_JoypadKeyBindings extends GameDashboardSection {

    public DS_JoypadKeyBindings() {
        super(TengenMsPacMan_DashboardID.JOYPAD);
    }

    @Override
    public void connect(Game game) {
        final Joypad joypad = game.input().joypad();

        final ResourceManager resourceManager = this::getClass;

        final ImageView nesControllerImage = new ImageView(
            resourceManager.loadImage("/de/amr/pacmanfx/tengenmspacman/graphics/nes-controller.jpg"));

        addRow(nesControllerImage);

        addDynamicInfo("[SELECT]", () -> buttonKey(joypad, JoypadButton.SELECT));

        addDynamicInfo("[START]", () -> buttonKey(joypad, JoypadButton.START));

        addDynamicInfo("[B]  [A]",
            () -> "%s   %s".formatted(
                buttonKey(joypad, JoypadButton.B),
                buttonKey(joypad, JoypadButton.A)));

        addDynamicInfo("UP/DOWN/LEFT/RIGHT",
            () -> "%s  %s  %s  %s".formatted(
                buttonKey(joypad, JoypadButton.UP),
                buttonKey(joypad, JoypadButton.DOWN),
                buttonKey(joypad, JoypadButton.LEFT),
                buttonKey(joypad, JoypadButton.RIGHT))
        );

        // Take dashboard title from Tengen Ms. Pac-Man text bundle
        final GameVariant tengenGameVariant = game.gameVariant(GameVariantID.TENGEN_MS_PACMAN.name());
        setText(tengenGameVariant.config().translations().translate("infobox.joypad.title"));
    }

    private static String buttonKey(Joypad joypad, JoypadButton button) {
        return joypad.keyForButton(button).getDisplayText();
    }
}