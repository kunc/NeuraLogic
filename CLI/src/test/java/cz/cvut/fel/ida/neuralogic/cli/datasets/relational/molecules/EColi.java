package cz.cvut.fel.ida.neuralogic.cli.datasets.relational.molecules;

import cz.cvut.fel.ida.neuralogic.cli.Main;
import cz.cvut.fel.ida.pipelines.Pipeline;
import cz.cvut.fel.ida.setup.Settings;
import cz.cvut.fel.ida.utils.generic.Pair;
import cz.cvut.fel.ida.utils.generic.TestAnnotations;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.logging.Logger;

import static cz.cvut.fel.ida.utils.generic.Utilities.getDatasetArgs;

public class EColi {
    private static final Logger LOG = Logger.getLogger(EColi.class.getName());
    static String dataset = "relational/molecules/e_coli";

    @TestAnnotations.Slow
    public void defaultEcoliPerformance() throws Exception {
        Settings settings = Settings.forSlowTest();

        settings.seed = 1;

        settings.setOptimizer(Settings.OptimizerSet.SGD);
//        settings.initLearningRate = 0.3;

        settings.maxCumEpochCount = 5000;
        settings.plotProgress = 5;

        settings.trainValidationPercentage = 0.9;

//        settings.appLimitSamples = 200;
        settings.isoValueCompression = false;
        settings.chainPruning = false;
        settings.processMetadata = true;

        Pair<Pipeline, ?> results = Main.main(getDatasetArgs(dataset, "-t ./template.txt"), settings);
    }

    @TestAnnotations.Slow
    public void defaultEcoliPerformanceCross() throws Exception {
        Settings settings = Settings.forSlowTest();

        settings.seed = 1;

        settings.setOptimizer(Settings.OptimizerSet.ADAM);
        settings.maxCumEpochCount = 100;
        settings.plotProgress = 30;

        settings.appLimitSamples = 1000;
        settings.isoValueCompression = false;
        settings.chainPruning = false;

        settings.errorFunction = Settings.ErrorFcn.CROSSENTROPY;
        settings.ruleNeuronActivation = Settings.ActivationFcn.TANH;

        Pair<Pipeline, ?> results = Main.main(getDatasetArgs(dataset, "-t ./template_cross.txt"), settings);
    }



    @TestAnnotations.Slow
    public void defaultEcoliPerformanceGNN() throws Exception {
        Settings settings = Settings.forSlowTest();

        settings.seed = 1;

        settings.setOptimizer(Settings.OptimizerSet.ADAM);
        settings.maxCumEpochCount = 10000;

        settings.appLimitSamples = 10000;
        settings.isoValueCompression = false;
        settings.chainPruning = false;
        settings.processMetadata = true;

        Pair<Pipeline, ?> results = Main.main(getDatasetArgs(dataset, "-t ./template_gnn.txt"), settings);
    }


    @TestAnnotations.Slow
    public void defaultEcoliPerformanceCross_08AUC() throws Exception {
        Settings settings = Settings.forSlowTest();

        settings.seed = 1;

        settings.setOptimizer(Settings.OptimizerSet.ADAM);
        settings.maxCumEpochCount = 400;
        settings.plotProgress = 60;

        settings.appLimitSamples = -1;
        settings.isoValueCompression = false;
        settings.chainPruning = false;
        settings.processMetadata = true;

        settings.trainValidationPercentage = 0.9;
        settings.errorFunction = Settings.ErrorFcn.CROSSENTROPY;

        Pair<Pipeline, ?> results = Main.main(getDatasetArgs(dataset, "-t ./template_cross.txt"), settings);
    }

    @TestAnnotations.Parameterized
    @ValueSource(strings = {
            "./templates/template.txt",
            "./templates/template10.txt",
            "./templates/template100.txt",
            "./templates/template_cross.txt",
            "./templates/template_cross4.txt",
            "./templates/template_gnn.txt",
            "./templates/template_gnn_shallow.txt",
            "./templates/template_gnn_simple.txt",
            "./templates/template_gnnW.txt",
            "./templates/template_gnnW10.txt",
            "./templates/template_gnnW100.txt",
            "./templates/templateW.txt",
            "./templates/templateW10.txt",
            "./templates/templateW100.txt"
    })
    public void testTemplates(String template) throws Exception {
        Settings settings = Settings.forSlowTest();

        settings.maxCumEpochCount = 10;
        settings.appLimitSamples = 100;

        Pair<Pipeline, ?> results = Main.main(getDatasetArgs(dataset, "-t " + template), settings);
    }
}