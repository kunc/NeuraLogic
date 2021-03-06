package cz.cvut.fel.ida.neural.networks.structure.metadata.inputMappings;

import cz.cvut.fel.ida.algebra.weights.Weight;
import cz.cvut.fel.ida.neural.networks.structure.components.neurons.Neurons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class WeightedNeuronMapping<T extends Neurons> extends NeuronMapping<T> implements LinkedMapping.WeightMapping {
    private static final Logger LOG = Logger.getLogger(WeightedNeuronMapping.class.getName());

    List<Weight> weights;
    public WeightedNeuronMapping<T> previous;

    public WeightedNeuronMapping(List<T> inputs, List<Weight> weights) {
        super(inputs);
        this.previous = new WeightedNeuronMapping<>();
        this.previous.weights = new ArrayList<>(weights);
        this.weights = new ArrayList<>();
    }

    public WeightedNeuronMapping(WeightedNeuronMapping<T> previous) {
        super(previous);
        this.previous = previous;
        this.weights = new ArrayList<>();
    }

    public WeightedNeuronMapping() {
        super();
        this.weights = new ArrayList<>();
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public Iterator<Weight> weightIterator() {
        return new WeightIterator(this);
    }

    public void addWeight(Weight weight) {
        weights.add(weight);
    }


    public class WeightIterator implements Iterator<Weight> {

        WeightedNeuronMapping<T> actual;
        int current;

        public WeightIterator(WeightedNeuronMapping<T> inputMapping) {
            this.actual = inputMapping;
            this.current = actual.inputs.size() - 1;
        }

        @Override
        public boolean hasNext() {
            return !(actual.previous == null && current <= 0);
        }

        @Override
        public Weight next() {
            if (current >= 0)
                return actual.weights.get(current--);
            else if (actual.previous != null) {
                actual = actual.previous;
                current = actual.inputs.size() - 1;
                return actual.weights.get(current--);
            } else {
                return null;
            }
        }

        public void replace(Weight weight) {
            actual.weights.set(current + 1, weight);    //todo test this
        }
    }

}