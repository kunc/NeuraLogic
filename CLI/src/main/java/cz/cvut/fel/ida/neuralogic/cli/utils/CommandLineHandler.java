package cz.cvut.fel.ida.neuralogic.cli.utils;

import cz.cvut.fel.ida.setup.Settings;
import org.apache.commons.cli.*;
import org.apache.commons.cli.DefaultParser;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by gusta on 8.3.17.
 */
public class CommandLineHandler {
    private static final Logger LOG = Logger.getLogger(CommandLineHandler.class.getName());

    public CommandLine parseParams(String[] args, Settings settings) throws Exception {
        Options options = getOptions(settings);
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        LOG.info("with arguments: " + Arrays.toString(args) + " ...parsed into:");
        try {
            cmd = parser.parse(options, args);
            if (cmd.getOptions().length == 0) {
                printHelp(options);
                throw new Exception("No arguments provided. You need to specify input data/model for learning.");
            }
            for (Option option : cmd.getOptions()) {
                LOG.info(option.getLongOpt() + " : " + option.getValue());
            }
        } catch (ParseException ex) {
            printHelp(options);
            throw new Exception("Unable to parse arguments!");
        }
        return cmd;
    }

    public Options getOptions(Settings settings) {
        Options options = new Options();

        //-----------source files
        options.addOption("sf", "sourcesFile", true, "path to json sources specification file (" + settings.sourcesFile + ")");
        options.addOption("sd", "sourcesDir", true, "path to directory with all the standardly-named source files for learning (" + settings.sourcePath + ")");

        options.addOption("t", "template", true, "template file containing weighted rules for leaning (" + settings.templateFile + ")");

        options.addOption("q", "trainQueries", true, "trainQueries file containing labeled queries (" + settings.trainQueriesFile + ")");
        options.addOption("e", "trainExamples", true, "trainExamples file containing, possibly labeled, input facts (" + settings.trainExamplesFile + ")");

        options.addOption("vq", "valQueries", true, "valQueries file containing labeled queries (" + settings.valQueriesFile + ")");
        options.addOption("ve", "valExamples", true, "valExamples file containing, possibly labeled, input facts (" + settings.valExamplesFile + ")");

        //-----------settings

        options.addOption("set", "settingsFile", true, "path to json file with all the settings (" + settings.settingsFile + ")");

        options.addOption("out", "outputFolder", true, "output folder for logging and exporting (" + settings.outDir + ")");

        options.addOption("mode", "pipelineMode", true, "main mode of the program [complete, neuralization, debug] (" + settings.mainMode.toString().toLowerCase() + ")");

        options.addOption("debug", "debugMode", true, "debug some objects within the pipeline during the run [template, grounding, neuralization, samples, model] (none)");

        options.addOption("lim", "limitExamples", true, "limit examples to some smaller number (use e.g. for debugging) (" + settings.appLimitSamples + ")");

        options.addOption("seed", "randomSeed", true, "int seed for random generator (" + settings.seed + ")");

        //-----------selection of one of evaluation modes
        OptionGroup evalGroup = new OptionGroup();
        // with test files given
        evalGroup.addOption(new Option("tq", "testQueries", true, "file with test queries (" + settings.testQueriesFile + ")"));
        options.addOption("te", "testExamples", true, "file with test examples (" + settings.testExamplesFile + ")");
        // with crossvalidation folds given
        evalGroup.addOption(Option.builder("fp").optionalArg(true).longOpt("foldPrefix").numberOfArgs(1).desc("folds folder names prefix (" + settings.foldsPrefix + ")").build());
        // with single file to xval split given
        evalGroup.addOption(new Option("xval", "crossvalidation", true, "number of folds to split for crossvalidation (" + settings.foldsCount + ")"));
        options.addOptionGroup(evalGroup);

        //grounding
        options.addOption(new Option("gm", "groundingMode", true, "groundings mode [normal, sequential, global] (" + settings.groundingMode.name() + ")"));
        options.addOption(new Option("ga", "groundingAlgorithm", true, "groundings algorithm [BUp, TDown, Gringo] (" + settings.grounding.name() + ")"));

        //training
        options.addOption(new Option("dist", "distribution", true, "distribution for weight initialization [uniform, normal, longtail, constant] (" + settings.initDistribution.toString().toLowerCase() + ")"));
        options.addOption(new Option("init", "initialization", true, "algorithm for weight initialization [simple, glorot, he] (" + settings.initializer.toString().toLowerCase() + ")"));

        options.addOption(new Option("opt", "optimizer", true, "optimization algorithm (" + settings.getOptimizer() + ")"));
        options.addOption(new Option("lr", "learningRate", true, "initial learning rate (" + settings.initLearningRate + ")"));
        options.addOption(new Option("ts", "trainingSteps", true, "cumulative number of epochae in neural training (" + settings.maxCumEpochCount + ")"));

        options.addOption(new Option("decay", "learnRateDecay", true, "learning rate decay geometric coefficient (-1=off) (" + settings.learnRateDecay + ")"));
        options.addOption(new Option("decays", "decaySteps", true, "learning rate decays every N steps (" + settings.decaySteps + ")"));

        options.addOption(new Option("preft", "preferTraining", true, "turn on to force best training model selection as opposed to (default) selecting best validation error model (" + settings.preferBestTrainingNotvalidation + ")"));

        //functions
        options.addOption(new Option("atomf", "atomFunction", true, "activation function for atom neurons (" + settings.atomNeuronActivation.name().toLowerCase() + ")"));
        options.addOption(new Option("rulef", "ruleFunction", true, "activation function for rule neurons (" + settings.ruleNeuronActivation.name().toLowerCase() + ")"));
        options.addOption(new Option("aggf", "aggFunction", true, "aggregation function for aggregation neurons (" + settings.aggNeuronActivation.name().toLowerCase() + ")"));


        //evaluation
        options.addOption(new Option("em", "evaluationMode", true, "evaluation is either [regression, classification] (" + "classification" + ")"));
        options.addOption(new Option("ef", "errorFunction", true, "type of error function [MSE, XEnt] (" + settings.errorAggregationFcn.name() + settings.errorFunction.name() + ")"));

        //compression
        options.addOption(new Option("iso", "isoCompression", true, "iso-value network compression (lifting), number of decimal digits (" + settings.isoDecimals + ")"));
        options.addOption(new Option("isoinits", "isoInitializations", true, "number of iso-value initializations for network compression (lifting) (" + settings.isoValueInits + ")"));
        options.addOption(new Option("isocheck", "losslessCompression", true, "lossless compression isomorphism extra check? (" + settings.losslessIsoCompression + ")"));
        options.addOption(new Option("prune", "chainPruning", true, "linear chain network pruning (" + (settings.chainPruning ? 1 : 0) + ")"));

        //todo rest of the commandline options that might be useful

        return options;
    }

    public void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(200);
        formatter.setOptionComparator(null);
        String header = "perform learning of a Lifted Relational Neural Network model";
        String footer = "please refer to README or project page (https://github.com/GustikS/NeuraLogic) for further details.";
        formatter.printHelp("java -jar NeuraLogic.jar", header, options, footer, true);
    }

}
