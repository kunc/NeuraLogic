package cz.cvut.fel.ida.algebra.functions;

import cz.cvut.fel.ida.algebra.functions.specific.*;
import cz.cvut.fel.ida.algebra.values.Value;
import cz.cvut.fel.ida.setup.Settings;

import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Class representing activation functions, i.e. those that firstly accumulate input values via summation and then apply some non-linearity on top.
 * The non-linearity is provided via FunctionalInterface Function separately for evaluation and derivative/gradient.
 * <p>
 * Created by gusta on 8.3.17.
 */
public abstract class Activation extends Aggregation {
    private static final Logger LOG = Logger.getLogger(Activation.class.getName());

    /**
     * Forward-pass function
     */
    transient Function<Double, Double> evaluation;
    /**
     * Backward-pass / derivative of the evaluation function
     */
    transient Function<Double, Double> gradient;

    protected Activation(Function<Double, Double> evaluation, Function<Double, Double> gradient) {
        this.evaluation = evaluation;
        this.gradient = gradient;
    }

    @Override
    public boolean isInputSymmetric() {
        return true;
    }

    public Value evaluate(Value summedInputs) {
        return summedInputs.apply(evaluation);
    }

    public Value differentiate(Value summedInputs) {
        return summedInputs.apply(gradient);
    }

    @Override
    public Value evaluate(List<Value> inputs) {
        Value sum = inputs.get(0).clone();
        for (int i = 1, len = inputs.size(); i < len; i++) {
            sum.plus(inputs.get(i));
        }
        return evaluate(sum);
    }

    @Override
    public Value differentiate(List<Value> inputs) {
        Value sum = inputs.get(0).clone();
        for (int i = 1, len = inputs.size(); i < len; i++) {
            sum.plus(inputs.get(i));
        }
        return differentiate(sum);
    }

    public static Activation getActivationFunction(Settings.ActivationFcn activationFcn) {
        switch (activationFcn) {
            case SIGMOID:
                return Singletons.sigmoid;
            case TANH:
                return Singletons.tanh;
            case SIGNUM:
                return Singletons.signum;
            case IDENTITY:
                return Singletons.identity;
            case RELU:
                return Singletons.relu;
            case LUKASIEWICZ:
                return Singletons.lukasiewiczSigmoid;
            default:
                LOG.severe("Unimplemented activation function");
                return null;
        }
    }

    public static Aggregation parseActivation(String agg) {
        switch (agg) {
            case "sigmoid":
                return Activation.Singletons.sigmoid;
            case "tanh":
                return Activation.Singletons.tanh;
            case "signum":
                return Activation.Singletons.signum;
            case "relu":
                return Activation.Singletons.relu;
            case "identity":
                return Activation.Singletons.identity;
            case "lukasiewicz":
                return Activation.Singletons.lukasiewiczSigmoid;
            default:
                if (agg.startsWith("crosssum-")) {
                    String inner = agg.substring(agg.indexOf("-") + 1);
                    Aggregation innerActivation = parseActivation(inner);
                    return new CrossSum((Activation) innerActivation);
                } else if (agg.startsWith("elementproduct-")) {
                    String inner = agg.substring(agg.indexOf("-") + 1);
                    Aggregation innerActivation = parseActivation(inner);
                    return new ElementProduct((Activation) innerActivation);
                } else if (agg.startsWith("product-")) {
                    String inner = agg.substring(agg.indexOf("-") + 1);
                    Aggregation innerActivation = parseActivation(inner);
                    return new Product((Activation) innerActivation);
                }
                throw new RuntimeException("Unable to parse activation function: " + agg);
        }
    }

    public static class Singletons {
        public static LukasiewiczSigmoid lukasiewiczSigmoid = new LukasiewiczSigmoid();
        public static Sigmoid sigmoid = new Sigmoid();
        public static Signum signum = new Signum();
        public static ReLu relu = new ReLu();
        public static Identity identity = new Identity();
        public static Tanh tanh = new Tanh();
    }
}