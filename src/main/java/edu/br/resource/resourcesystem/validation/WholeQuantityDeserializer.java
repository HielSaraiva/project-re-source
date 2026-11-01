package edu.br.resource.resourcesystem.validation;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/** Preserve integer validation at the JSON boundary instead of truncating a decimal. */
public class WholeQuantityDeserializer extends StdDeserializer<Integer> {
    public WholeQuantityDeserializer() {
        super(Integer.class);
    }

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) {
        if (parser.isExpectedNumberIntToken()
                || parser.currentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
            try {
                return parser.getDecimalValue().intValueExact();
            } catch (ArithmeticException invalid) {
                /* Report a binding error below. */
            }
        }
        return context.reportInputMismatch(
                Integer.class, "Informe uma quantidade inteira entre 1 e 2.147.483.647.");
    }
}
