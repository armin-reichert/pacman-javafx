/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.model.world.WorldMapSelectionMode;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.uilib.widgets.OptionMenu;
import de.amr.pacmanfx.uilib.widgets.OptionMenuEntry;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_PACMAN_XXL;
import static java.util.Objects.requireNonNull;

public class PacManXXL_OptionMenu extends OptionMenu {

    public static final int NUM_TILES_X = 42;
    public static final int NUM_TILES_Y = 34;

    public static final int TEXT_COLUMN = 6;
    public static final int VALUE_COLUMN = 20;

    public static final float CHASE_ANIMATION_Y = (NUM_TILES_Y - 12) * TS;

    private OptionMenuEntry<StandardGameVariant> entryGameVariant;
    private OptionMenuEntry<Boolean> entryPlay3D;
    private OptionMenuEntry<Boolean> entryCutScenesEnabled;
    private OptionMenuEntry<WorldMapSelectionMode> entryMapOrder;

    private final ChaseAnimation chaseAnimation = new ChaseAnimation(NUM_TILES_X);
    private GameUI ui;

    public PacManXXL_OptionMenu() {
        super(NUM_TILES_X, NUM_TILES_Y, TEXT_COLUMN, VALUE_COLUMN);

        setTitle("Pac-Man XXL");
        // Default key code RIGHT is already used to navigate through start pages carousel
        setNextValueKeyCode(KeyCode.SPACE);
        defineAction(1, KeyCode.E, "OPEN EDITOR");
        defineAction(2, KeyCode.ENTER, "START");
        createEntries();

        chaseAnimation.setY(CHASE_ANIMATION_Y);
        chaseAnimation.scalingProperty().bind(scalingProperty());

        entryGameVariant.valueProperty().addListener((_, _, newVariant) -> {
            if (ui != null) {
                final GameUI_Config uiConfig = ui.config(newVariant.name());
                chaseAnimation.init(uiConfig, canvas);
            }
        });

        canvas.focusedProperty().addListener((_, _, newFocused) -> {
            if (newFocused) {
                startDrawLoop();
            }
        });
    }

    private void createEntries() {
        entryGameVariant = new OptionMenuEntry<>(
            "GAME VARIANT",
            List.of(ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN_XXL),
            ARCADE_PACMAN_XXL
        );
        entryGameVariant.setValueFormatter(variant -> switch (variant) {
            case ARCADE_PACMAN_XXL    -> "PAC-MAN XXL";
            case ARCADE_MS_PACMAN_XXL -> "MS.PAC-MAN XXL";
            default -> "???";
        });
        addEntry(entryGameVariant);

        entryPlay3D = new OptionMenuEntry<>(
            "SCENE DISPLAY",
            List.of(true, false),
            false
        );
        entryPlay3D.setValueFormatter(play3D -> play3D ? "3D" : "2D");
        addEntry(entryPlay3D);

        entryCutScenesEnabled = new OptionMenuEntry<>(
            "CUTSCENES",
            List.of(true, false),
                true
        );
        entryCutScenesEnabled.setValueFormatter(cutScenesEnabled -> cutScenesEnabled ? "YES" : "NO");
        addEntry(entryCutScenesEnabled);

        entryMapOrder = new OptionMenuEntry<>(
            "MAP ORDER",
            List.of(
                WorldMapSelectionMode.CUSTOM_MAPS_FIRST,
                WorldMapSelectionMode.ALL_RANDOM,
                WorldMapSelectionMode.NO_CUSTOM_MAPS),
            WorldMapSelectionMode.CUSTOM_MAPS_FIRST
        );
        entryMapOrder.setValueFormatter(mode -> {
            if (!entryMapOrder.enabled()) {
                return "NO CUSTOM MAPS!";
            }
            return switch (mode) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM -> "RANDOM ORDER";
                case NO_CUSTOM_MAPS -> "NO CUSTOM MAPS";
            };
        });
        addEntry(entryMapOrder);
    }

    @Override
    public void draw() {
        super.draw();
        chaseAnimation.draw();
    }

    public void init(GameUI ui) {
        this.ui = requireNonNull(ui);

        final Game game = ui.context().currentGame();
        final StandardGameVariant gameVariant = StandardGameVariant.valueOf(ui.context().gameVariantName());

        if (!(game.mapSelector() instanceof PacManXXL_MapSelector mapSelector)) {
            Logger.error("Expected XXL map selector but found {}", game.mapSelector().getClass().getSimpleName());
            throw new IllegalStateException();
        }
        mapSelector.loadMapPrototypes();

        // init entries
        entryGameVariant.selectValue(gameVariant);
        entryPlay3D.selectValue(GameUI.PROPERTY_3D_ENABLED.get());
        entryCutScenesEnabled.selectValue(game.cutScenesEnabled());
        entryMapOrder.selectValue(mapSelector.selectionMode());
        entryMapOrder.setEnabled(!mapSelector.customMapPrototypes().isEmpty());
        logEntryState();

        soundEnabledProperty().bind(ui.currentConfig().soundManager().muteProperty().not());
        chaseAnimation.init(ui.currentConfig(), canvas);

        requestFocus();
    }

    public void logEntryState() {
        Logger.info("{} {} {} mapOrder: {}",
            entryGameVariant.value(),
            entryPlay3D.value() ? "3D" : "2D",
            entryCutScenesEnabled.value() ? "cutscenes-on" : "cutscenes-off",
            entryMapOrder.value());
    }

    public void startSelectedGame() {
        ui.showPlayView();
        ui.restart();
    }

    public void startDrawLoop() {
        super.startDrawLoop();
        chaseAnimation.start();
    }

    public void stopDrawLoop() {
        super.stopDrawLoop();
        chaseAnimation.stop();
    }

    public OptionMenuEntry<StandardGameVariant> entryGameVariant() {
        return entryGameVariant;
    }

    public OptionMenuEntry<Boolean> entryPlay3D() {
        return entryPlay3D;
    }

    public OptionMenuEntry<Boolean> entryCutScenesEnabled() {
        return entryCutScenesEnabled;
    }

}