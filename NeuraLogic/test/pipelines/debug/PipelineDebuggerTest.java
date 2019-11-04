package pipelines.debug;

import org.junit.Test;
import pipelines.Pipeline;
import pipelines.building.AbstractPipelineBuilder;
import settings.Settings;
import settings.Sources;
import utils.logging.Logging;

import java.util.logging.Level;

public class PipelineDebuggerTest {

    @Test
    public void family() {
        Logging logging = Logging.initLogging(Level.FINEST);
        String[] args = new String("-path ./resources/datasets/family").split(" ");

        Settings settings = new Settings();

        Sources sources = Sources.getSources(args, settings);
        PipelineDebugger pipelineDebugger = new PipelineDebugger(settings);

        AbstractPipelineBuilder<Sources, ?> builder = AbstractPipelineBuilder.getBuilder(sources, settings);
        Pipeline<Sources, ?> sourcesPipeline = builder.buildPipeline();
        pipelineDebugger.debug(sourcesPipeline);
    }

}