/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views;

import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.ui.EngineUIContext;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.util.List;

import static de.amr.pacmanfx.engine.action.ActionExecutor.runAction;

public final class MenuBuildingSupport {

    private MenuBuildingSupport() {}
    
    public static MenuItem addTitleItem(List<MenuItem> menu, String itemText) {
        final var item = new CustomMenuItem(new Text(itemText), false);
        item.getStyleClass().add("custom-menu-title-item");
        menu.add(item);
        return item;
    }

    public static MenuItem addLocalizedActionItem(
        List<MenuItem> menu,
        TranslationManager translator,
        Runnable action,
        String assetKey,
        Object... args)
    {
        var item = new MenuItem(translator.translate(assetKey, args));
        item.setOnAction(_ -> action.run());
        menu.add(item);
        return item;
    }

    public static MenuItem addLocalizedActionItem(
        EngineUIContext engine,
        List<MenuItem> menu,
        TranslationManager translator,
        GameAction<EngineUIContext> action,
        String assetKey,
        Object... args)
    {
        var item = new MenuItem(translator.translate(assetKey, args));
        item.setOnAction(_ -> runAction(action, engine));
        menu.add(item);
        return item;
    }

    public static MenuItem addLocalizedTitleItem(
        List<MenuItem> menu,
        TranslationManager translator,
        String assetKey,
        Object... args)
    {
        return addTitleItem(menu, translator.translate(assetKey, args));
    }

    public static CheckMenuItem addLocalizedCheckBox(
        List<MenuItem> menu,
        TranslationManager translator,
        BooleanProperty selectionProperty,
        String assetKey,
        Object... args)
    {
        var item = new CheckMenuItem(translator.translate(assetKey, args));
        item.selectedProperty().bindBidirectional(selectionProperty);
        menu.add(item);
        return item;
    }

    public static RadioMenuItem addLocalizedRadioButton(
        List<MenuItem> menu,
        TranslationManager translator,
        String assetKey,
        Object... args)
    {
        final var item = new RadioMenuItem(translator.translate(assetKey, args));
        menu.add(item);
        return item;
    }

    public static void addSeparator(List<MenuItem> menu) {
        menu.add(new SeparatorMenuItem());
    }
}