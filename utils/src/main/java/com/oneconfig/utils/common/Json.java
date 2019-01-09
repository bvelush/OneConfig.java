package com.oneconfig.utils.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Json {

    public static JsonNode parseJsonString(String strJson) {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.readTree(strJson);
        } catch (Exception ex) {
            throw new JsonUtilException(String.format("Can't convert string '%s...' to JsonNode", strJson.substring(0, 30)), ex);
        }
    }

    public static String getMandatoryString(JsonNode node, String fieldName) {
        try {
            JsonNode subnode = node.get(fieldName);
            if (subnode == null) {
                throw new JsonUtilException(String.format("Field '%s' doesn't exist", fieldName));
            }
            return subnode.textValue();
        } catch (Exception ex) {
            throw new JsonUtilException(String.format("Field '%s' is not a text field", fieldName), ex);
        }
    }

    public static JsonNode getMandatoryNode(JsonNode node, String fieldName) {
        JsonNode subnode = node.get(fieldName);
        if (subnode == null) {
            throw new JsonUtilException(String.format("Field '%s' doesn't exist", fieldName));
        }
        return subnode;
    }
}
