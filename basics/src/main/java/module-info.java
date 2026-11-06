/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
module de.amr.basics {
    requires com.google.gson;
    requires javafx.graphics;
    requires org.tinylog.api;

    exports de.amr.basics;
    exports de.amr.basics.ecs;
    exports de.amr.basics.ecs.comp;
    exports de.amr.basics.ecs.system;
    exports de.amr.basics.filesystem;
    exports de.amr.basics.fsm;
    exports de.amr.basics.graph;
    exports de.amr.basics.json;
    exports de.amr.basics.math;
    exports de.amr.basics.timer;
    exports de.amr.basics.util;
}