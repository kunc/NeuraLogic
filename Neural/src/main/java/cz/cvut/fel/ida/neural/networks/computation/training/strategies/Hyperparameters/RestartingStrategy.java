package cz.cvut.fel.ida.neural.networks.computation.training.strategies.Hyperparameters;

import cz.cvut.fel.ida.learning.results.Progress;
import cz.cvut.fel.ida.setup.Settings;
import cz.cvut.fel.ida.utils.exporting.Exportable;

import java.util.logging.Logger;

public abstract class RestartingStrategy implements Exportable {
    private static final Logger LOG = Logger.getLogger(RestartingStrategy.class.getName());

    public RestartingStrategy(){

    }

    public static RestartingStrategy getFrom(Settings settings) {
        return new StaticRestartingStrategy(settings.maxCumEpochCount);
    }

    public abstract boolean continueRestart(Progress progress);

    public abstract void nextRestart();
}