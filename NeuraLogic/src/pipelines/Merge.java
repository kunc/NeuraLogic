package pipelines;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Merge blocks are special as they need external execution (there is no single predecessor that could induce it by itself)
 *
 * @param <I1>
 * @param <I2>
 * @param <O>
 */
public abstract class Merge<I1, I2, O> implements Supplier<O>, Executable {
    private static final Logger LOG = Logger.getLogger(Merge.class.getName());

    Supplier<I1> input1;
    Supplier<I2> input2;
    Consumer<O> output;

    public String ID;
    /**
     * Storage of the (intermediate) result of calculation of this Pipe. It will only be not null once someone has
     * actually run (called accept) this Pipe.
     */
    O outputReady;

    public Merge(String id) {
        ID = id;
    }
    
    @Override
    public void run() {
        I1 i1 = input1.get();
        I2 i2 = input2.get();
        accept(i1, i2);
    }

    public O get() {
        if (outputReady == null) {
            LOG.severe("The result of this Merge " + ID + " is requested but not yet calculated");
            LOG.severe("Pipeline is broken");
            System.exit(3);
        }
        return outputReady;
    }

    public void accept(I1 input1, I2 input2) {
        O merging = merge(input1, input2);
        if (output != null)
            output.accept(merging);
        else {
            outputReady = merging;
        }
    }

    protected abstract O merge(I1 input1, I2 input2);

}