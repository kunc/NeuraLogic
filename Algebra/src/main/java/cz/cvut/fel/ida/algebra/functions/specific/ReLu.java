package cz.cvut.fel.ida.algebra.functions.specific;

import cz.cvut.fel.ida.algebra.functions.Activation;
import cz.cvut.fel.ida.utils.generic.Pair;

import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by gusta on 8.3.17.
 */
public class ReLu extends Activation {
    private static final Logger LOG = Logger.getLogger(ReLu.class.getName());

    @Override
    public String getName() {
        return ReLu.class.getSimpleName();
    }

    private static final Function<Double, Double> signum = in -> in > 0 ? in : 0.0;

    private static final Function<Double, Double> zerograd = in -> in > 0 ? 1.0 : 0.0;

    public ReLu() {
        super(signum, zerograd);
    }

    @Override
    public ReLu replaceWithSingleton() {
        return Singletons.relu;
    }

    @Override
    public Pair<Double, Double> getSaturationRange() {
        return new Pair<>(0.01, Double.MAX_VALUE);
    }

}