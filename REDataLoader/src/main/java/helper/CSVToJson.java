package helper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CSVToJson {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonNode csvRecordToJson(CSVRecord rec) {
        Map<String, String> map = new HashMap<>();
        for (String header : rec.toMap().keySet()) {
            map.put(header, rec.get(header));
        }

        return mapper.valueToTree(map);
    }
}
