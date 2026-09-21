module de.amr.basics.ui {

    requires de.amr.basics;
    requires org.tinylog.api;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;

    exports de.amr.basics.ui.ecs;
    exports de.amr.basics.ui.ecs.comp;
    exports de.amr.basics.ui.ecs.systems;
    exports de.amr.basics.ui.spriteanim;
    exports de.amr.basics.ui.entities.props.textdisplay;
    exports de.amr.basics.ui.entities.props.marquee;
    exports de.amr.basics.ui.entities.props.imagedisplay;
    exports de.amr.basics.ui.entities.props;
    exports de.amr.basics.ui.entities.props.clapperboard;
    exports de.amr.basics.ui.assets;
    exports de.amr.basics.ui.entities.hud.score;
    exports de.amr.basics.ui.rendering;
    exports de.amr.basics.ui.entities.props.messageview;
    exports de.amr.basics.ui.entities.hud.levelCounter;
    exports de.amr.basics.ui.entities.hud.livescounter;
    exports de.amr.basics.ui.entities.props.ghostpoints;
    exports de.amr.basics.ui.entities.props.bonuspoints;
    exports de.amr.basics.ui.animation;
    exports de.amr.basics.ui.entities.hud;
    exports de.amr.basics.ui.entities.props.stork;
}