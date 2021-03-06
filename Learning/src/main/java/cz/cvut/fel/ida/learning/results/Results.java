package cz.cvut.fel.ida.learning.results;

import org.jetbrains.annotations.NotNull;
import cz.cvut.fel.ida.algebra.functions.Aggregation;
import cz.cvut.fel.ida.algebra.functions.specific.Average;
import cz.cvut.fel.ida.algebra.values.Value;
import cz.cvut.fel.ida.setup.Settings;
import cz.cvut.fel.ida.utils.exporting.Exportable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Object carrying target values with outputs, and corresponding evaluation computations.
 * <p>
 * Created by gusta on 8.3.17.
 */
public abstract class Results implements Exportable<Results> {
    private static final Logger LOG = Logger.getLogger(Results.class.getName());

    transient Settings settings;

    @Deprecated
    public boolean evaluatedOnline = true;

    public transient List<Result> evaluations;

    /**
     * How to aggregate individual errors of samples. E.g. mean for MSE, or sum for SSE.
     */
    Aggregation aggregationFcn;

    /**
     * The error fcn value as measured by the respective aggregationFcn over individual sample errorFcns
     */
    public Value error;

    public Results(@NotNull List<Result> evaluations, Settings settings) {
        this.settings = settings;
        this.evaluations = evaluations;
        this.aggregationFcn = getAggregation(settings);
        if (!evaluations.isEmpty())
            this.recalculate();
    }

    public void addResult(Result result) {
        evaluations.add(result);
    }

    @Override
    public String toString() {
        return aggregationFcn.toString() + "-error= " + error.toString();
    }

    public abstract boolean recalculate();

    public abstract boolean betterThan(Results other, Settings.ModelSelection criterion);

    protected Results(Value meanError) {
        this.error = meanError;
    }

    public StringBuilder printOutputs(boolean sortByIndex) {
        if (sortByIndex) {
            evaluations.sort(new Comparator<Result>() {
                @Override
                public int compare(Result o1, Result o2) {
                    return Integer.compare(o1.position, o2.position);
                }
            });
        } else {
            Collections.sort(evaluations);  //sort by output (default comparator)
        }

        StringBuilder sb = new StringBuilder();
        for (Result evaluation : evaluations) {
            sb.append(evaluation.sampleId);
            sb.append(" , output: " + evaluation.getOutput().toDetailedString());
            if (evaluation.getTarget() != null) {
                sb.append(" , target: " + evaluation.getTarget());
            }
            sb.append("\n");
        }
        return sb;
    }

    private static Aggregation getAggregation(Settings settings) {
        if (settings.errorAggregationFcn == Settings.AggregationFcn.AVG) {
            return new Average();
        } else {
            LOG.severe("Unsupported errorAggregationFcn.");
        }
        return null;
    }

    public abstract String toString(Settings settings);

    public boolean isEmpty() {
        return evaluations.isEmpty();
    }

    public static abstract class Factory {

        Settings settings;

        public Factory(Settings settings) {
            this.settings = settings;
        }

        public static Factory getFrom(Settings settings) {
            if (settings.regression) {
                return new RegressionFactory(settings);
            } else {
                if (settings.detailedResults)
                    return new DetailedClassificationFactory(settings);
                else
                    return new ClassificationFactory(settings);
            }
        }

        public abstract Results createFrom(List<Result> outputs);
    }

    private static class VoidFactory extends Factory {

        public VoidFactory(Settings settings) {
            super(settings);
        }

        @Override
        public Results createFrom(List<Result> outputs) {
            VoidResults regressionResults = new VoidResults(outputs, settings);
            return regressionResults;
        }
    }

    private static class RegressionFactory extends Factory {

        public RegressionFactory(Settings aggregation) {
            super(aggregation);
        }

        @Override
        public Results createFrom(List<Result> outputs) {
            RegressionResults regressionResults = new RegressionResults(outputs, settings);
            return regressionResults;
        }
    }

    private static class DetailedClassificationFactory extends Factory {

        public DetailedClassificationFactory(Settings aggregation) {
            super(aggregation);
        }

        @Override
        public Results createFrom(List<Result> outputs) {
            DetailedClassificationResults detailedClassificationResults = new DetailedClassificationResults(outputs, settings);
            return detailedClassificationResults;
        }
    }

    private static class ClassificationFactory extends Factory {

        public ClassificationFactory(Settings aggregation) {
            super(aggregation);
        }

        @Override
        public Results createFrom(List<Result> outputs) {
            ClassificationResults classificationResults = new ClassificationResults(outputs, settings);
            return classificationResults;
        }
    }
}