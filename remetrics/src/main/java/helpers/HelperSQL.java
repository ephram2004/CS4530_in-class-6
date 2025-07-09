package helpers;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;

import io.micrometer.common.lang.Nullable;

public class HelperSQL {

    public static JsonNode generateSchema(Class<?> cls) {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON
        );

        configBuilder.forFields()
                .withRequiredCheck(field -> {
                    boolean hasNullable = field.getAnnotationConsideringFieldAndGetter(
                            Nullable.class) != null;
                    System.out.println(field.getName() + ": "
                            + (hasNullable ? "nullable" : "required"));
                    return !hasNullable;
                });

        SchemaGeneratorConfig config = configBuilder.with(Option.EXTRA_OPEN_API_FORMAT_VALUES)
                .without(Option.FLATTENED_ENUMS_FROM_TOSTRING)
                .build();

        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(cls);

        return jsonSchema;
    }

    public static void exportSchemaToFile(Class<?> cls) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode schema = generateSchema(cls);

        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("schema.json"), schema);
            System.out.println("Exported JSON schema to schema.json");
        } catch (IOException e) {
            System.err.println("Could not export JSON schema!! " + e);
        }
    }

    public static String camelToSnake(String camel) {
        return camel.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
