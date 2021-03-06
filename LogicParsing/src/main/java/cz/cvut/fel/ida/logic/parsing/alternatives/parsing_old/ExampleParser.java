package cz.cvut.fel.ida.logic.parsing.alternatives.parsing_old;

import cz.cvut.fel.ida.learning.Example;

import java.io.Reader;
import java.util.stream.Stream;

/**
 * Created by gusta on 14.3.17.
 */
public interface ExampleParser {

    boolean isValid(String input);

    Stream<Example> parseExamples(Reader reader);
}