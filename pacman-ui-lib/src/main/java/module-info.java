// module is open to allow access to non-class resources
open module de.amr.pacmanfx.uilib {
    requires de.amr.basics;
    requires de.amr.basics.ui;
    requires de.amr.meshbuilder;
    requires de.amr.objparser;
    requires de.amr.pacmanfx.core;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires org.tinylog.api;

    exports de.amr.pacmanfx.uilib.controls;
    exports de.amr.pacmanfx.uilib.controls.skin;
    exports de.amr.pacmanfx.uilib.view2d;
    exports de.amr.pacmanfx.uilib.view3d;
    exports de.amr.pacmanfx.uilib.widgets;
    exports de.amr.pacmanfx.uilib.widgets.decorationpane;
    exports de.amr.pacmanfx.uilib.widgets.optionmenu;
}