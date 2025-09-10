// module is open to allow access to non-class resources
open module de.amr.pacmanfx.uilib {
    requires java.prefs;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires org.tinylog.api;
    requires de.amr.pacmanfx.core;
    requires javafx.base;

    exports de.amr.pacmanfx.uilib;
    exports de.amr.pacmanfx.uilib.objimport;
    exports de.amr.pacmanfx.uilib.model3D;
    exports de.amr.pacmanfx.uilib.assets;
    exports de.amr.pacmanfx.uilib.rendering;
    exports de.amr.pacmanfx.uilib.widgets;
    exports de.amr.pacmanfx.uilib.animation;
}