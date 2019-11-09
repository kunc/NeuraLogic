package networks.computation.training.debugging;

import org.junit.Test;
import settings.Settings;
import utils.logging.Logging;

import java.util.logging.Level;

public class TrainingDebuggerTest {

    @Test
    public void family() {
        Logging logging = Logging.initLogging(Level.FINE);
        String[] args = "-path ./resources/datasets/simple/family".split(" ");
        Settings settings = new Settings();
        settings.maxCumEpochCount = 2;
        settings.intermediateDebug = true;
        settings.undoWeightTrainingChanges = true;
        TrainingDebugger trainingDebugger = new TrainingDebugger(args, settings);
        trainingDebugger.executeDebug();
    }

    @Test
    public void mutaMini() {
        Logging logging = Logging.initLogging(Level.FINER);
        String[] args = "-e ./resources/datasets/relational/molecules/muta_mini/examples -t ./resources/datasets/relational/molecules/muta_mini/template_old_init".split(" ");

        Settings settings = new Settings();
        settings.maxCumEpochCount = 1000;
        settings.oneQueryPerExample = true;
        settings.neuralNetsPostProcessing = false;
        settings.pruneNetworks = true;
        settings.isoValueCompression = true;
        settings.intermediateDebug = false;
        settings.debugPipeline = true;
        settings.storeNotShow = true;
        settings.debugTemplateTraining = false; //too big
        settings.debugTemplate = false;
        settings.optimizer = Settings.OptimizerSet.SGD;
        TrainingDebugger trainingDebugger = new TrainingDebugger(args, settings);
        trainingDebugger.executeDebug();
    }

    @Test
    public void mutagen_zero_init() {
        Logging logging = Logging.initLogging(Level.FINER);
        String[] args = ("-e ./resources/datasets/relational/molecules/mutagenesis/trainExamples.txt " +
                "-q ./resources/datasets/relational/molecules/mutagenesis/trainQueries.txt " +
                "-t ./resources/datasets/relational/molecules/mutagenesis/template_old.txt").split(" ");

        Settings settings = new Settings();
        settings.seed = 0;
        settings.initLearningRate = 0.1;
        settings.maxCumEpochCount = 100;
        settings.resultsRecalculationEpochae = 20;
        settings.appLimitSamples = 10;
        settings.stratification = true;
        settings.oneQueryPerExample = true;
        settings.neuralNetsPostProcessing = true;
        settings.pruneNetworks = true;
        settings.isoValueCompression = false;
        settings.intermediateDebug = false;
        settings.debugPipeline = false;
        settings.storeNotShow = true;
        settings.debugTemplateTraining = false; //too big
        settings.debugTemplate = false;
        settings.optimizer = Settings.OptimizerSet.ADAM;
        TrainingDebugger trainingDebugger = new TrainingDebugger(args, settings);
        trainingDebugger.executeDebug();
    }

    /**
     * This setting works successfuly on template with no offsets with SGD after 10000 steps
     */
    @Test
    public final void mutagen_sgd_very_slow() {
        Logging logging = Logging.initLogging(Level.FINER);
        String[] args = ("-e ./resources/datasets/relational/molecules/mutagenesis/trainExamples.txt " +
                "-q ./resources/datasets/relational/molecules/mutagenesis/trainQueries.txt " +
                "-t ./resources/datasets/relational/molecules/mutagenesis/template_new_fast.txt").split(" ");

        Settings settings = new Settings();

        settings.seed = 0;
        settings.initLearningRate = 0.05;
        settings.maxCumEpochCount = 10000;
        settings.resultsRecalculationEpochae = 100;
        settings.debugSampleOutputs = true;
//        settings.appLimitSamples = 100;
        settings.calculateBestThreshold = true;
        settings.stratification = true;
        settings.oneQueryPerExample = true;
        settings.neuralNetsPostProcessing = true;
        settings.pruneNetworks = true;
        settings.isoValueCompression = false;
        settings.intermediateDebug = false;
        settings.debugPipeline = false;
        settings.storeNotShow = true;
        settings.debugTemplateTraining = false; //too big
        settings.debugTemplate = false;
        settings.initializer = Settings.InitSet.UNIFORM;
        settings.optimizer = Settings.OptimizerSet.SGD;
        TrainingDebugger trainingDebugger = new TrainingDebugger(args, settings);
        trainingDebugger.executeDebug();
    }

    @Test
    public void mutagen_fast() {
        Logging logging = Logging.initLogging(Level.FINER);
        String[] args = ("-e ./resources/datasets/relational/molecules/mutagenesis/trainExamples.txt " +
                "-q ./resources/datasets/relational/molecules/mutagenesis/trainQueries.txt " +
                "-t ./resources/datasets/relational/molecules/mutagenesis/template_fastest.txt").split(" ");

        Settings settings = new Settings();

        settings.seed = 0;
        settings.initLearningRate = 0.05;
        settings.maxCumEpochCount = 10000;
        settings.resultsRecalculationEpochae = 10;
        settings.shuffleEachEpoch = false;
        settings.debugSampleOutputs = false;
//        settings.appLimitSamples = 100;
        settings.calculateBestThreshold = false;
        settings.initializer = Settings.InitSet.UNIFORM;
        settings.optimizer = Settings.OptimizerSet.SGD;


        settings.stratification = false;
        settings.oneQueryPerExample = true;
        settings.neuralNetsPostProcessing = true;
        settings.pruneNetworks = true;
        settings.isoValueCompression = false;
        settings.intermediateDebug = false;
        settings.debugPipeline = false;
        settings.storeNotShow = true;
        settings.debugTemplateTraining = false; //too big
        settings.debugTemplate = false;
        TrainingDebugger trainingDebugger = new TrainingDebugger(args, settings);
        trainingDebugger.executeDebug();
    }
}