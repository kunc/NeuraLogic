package cz.cvut.fel.ida.logic.constructs.template.metadata;

import cz.cvut.fel.ida.algebra.functions.Activation;
import cz.cvut.fel.ida.algebra.functions.Aggregation;
import cz.cvut.fel.ida.algebra.utils.metadata.Metadata;
import cz.cvut.fel.ida.algebra.utils.metadata.Parameter;
import cz.cvut.fel.ida.algebra.utils.metadata.ParameterValue;
import cz.cvut.fel.ida.logic.constructs.template.components.WeightedRule;
import cz.cvut.fel.ida.setup.Settings;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by gusta on 1.3.18.
 */
public class RuleMetadata extends Metadata<WeightedRule> {
    private static final Logger LOG = Logger.getLogger(RuleMetadata.class.getName());

    public RuleMetadata(Settings settings, Map<String, Object> stringObjectMap) {
        super(settings, stringObjectMap);
    }

    @Override
    public boolean addValidateMetadatum(String parameterText, Object value) {
        Parameter parameter = new Parameter(parameterText);
        ParameterValue parameterValue = new ParameterValue(value);

        boolean valid = false;
        if (parameter.type == Parameter.Type.OFFSET && parameterValue.type == ParameterValue.Type.VALUE) {
            valid = true;
        } else if (parameter.type == Parameter.Type.LEARNABLE && parameterValue.type == ParameterValue.Type.BOOLEAN) {
            valid = true;
        } else if (parameter.type == Parameter.Type.ACTIVATION && parameterValue.type == ParameterValue.Type.STRING) {
            Aggregation aggregation = Activation.parseActivation(parameterValue.stringValue);
            if (aggregation != null) {
                valid = true;
                parameterValue.value = aggregation;
            }
        } else if (parameter.type == Parameter.Type.AGGREGATION && parameterValue.type == ParameterValue.Type.STRING) {
            Aggregation aggregation = Aggregation.parseFrom(parameterValue.stringValue);
            if (aggregation != null) {
                valid = true;
                parameterValue.value = aggregation;
            }
            //todo rest
        }

        if (valid)
            metadata.put(parameter, parameterValue);
        return true;
    }

    @Override
    public void applyTo(WeightedRule object) {
        metadata.forEach((param, value) -> apply(object, param, value));
    }

    private void apply(WeightedRule rule, Parameter param, ParameterValue value) {
        if (param.type == Parameter.Type.ACTIVATION) {
            rule.setActivationFcn((Activation) value.value);
        }
        if (param.type == Parameter.Type.AGGREGATION) {
            rule.setAggregationFcn((Aggregation) value.value);
        }
    }
}