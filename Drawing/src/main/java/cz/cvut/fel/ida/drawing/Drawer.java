package cz.cvut.fel.ida.drawing;


import cz.cvut.fel.ida.setup.Settings;

import javax.swing.*;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by gusta on 8.3.17.
 */
public abstract class Drawer<S> {   //todo next replace hashcodes (which collide sometimes) with truly unique IDs

    private static final Logger LOG = Logger.getLogger(Drawer.class.getName());

    protected NumberFormat numberFormat;
    protected Settings.Detail drawingDetail;
    protected boolean storeNotShow;

    protected GraphViz graphviz;
    public Settings settings;

    public Drawer(Settings settings) {
        this.settings = settings;
        if (settings.drawing) {
            this.graphviz = new GraphViz(settings);

            Logger.getLogger("java.awt").setLevel(Level.WARNING);
            Logger.getLogger("sun.awt").setLevel(Level.WARNING);
            Logger.getLogger("javax.swing").setLevel(Level.WARNING);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            }
        }

        this.numberFormat = Settings.shortNumberFormat;
        this.drawingDetail = settings.drawingDetail;
        this.storeNotShow = settings.storeNotShow;
    }

    public void display(byte[] imageBytes, String name) {
        //now turn off annoying java awt logging messages here
        Logger.getLogger("java.awt").setLevel(Level.WARNING);
        Logger.getLogger("sun.awt").setLevel(Level.WARNING);
        Logger.getLogger("javax.swing").setLevel(Level.WARNING);

        Object lock = new Object();
        DrawWindow debugWindow = new DrawWindow(imageBytes, name, lock);
        debugWindow.pauseUntilWindowCloses(lock);
    }

    public void draw(S obj) {
        if (graphviz == null) {
            LOG.warning("Drawing is required but no graphviz exec found, could not draw!");
            return;
        }
        this.graphviz.clearGraph();
        loadGraph(obj);
        try {
            LOG.info("Paused for drawing with Graphviz...(this may take a while for bigger graphs)");
            if (storeNotShow) {
                graphviz.storeGraphSource(obj.toString());
                byte[] image = graphviz.getGraphUsingTemporaryFile(graphviz.getDotSource(), graphviz.imgtype, graphviz.algorithm);
                graphviz.writeImageToFile(image, obj.toString());
                LOG.info("Graph stored into file named: " + obj.toString());
            } else {
                display(graphviz.getGraphImage(obj.toString()), obj.toString());
            }
        } catch (IOException | InterruptedException e) {
            LOG.severe(e.getMessage());
        }
    }

    public abstract void loadGraph(S obj);  //todo add indentation into the dot file

}
