package com.oneconfig.core.stores;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.oneconfig.core.Const;
import com.oneconfig.core.OneConfigException;
import com.oneconfig.utils.common.Json;
import com.oneconfig.utils.common.Str;

/**
 * JsonStore expects the following key in the configObject:
 *
 * -- storeContent -- a string containing the contents of the store json file
 */
public class JsonStore implements IStore {
    public static final String JSON_STORE_CONTENTSTR = "storeContent";

    private String name;
    private JsonNode root;

    public void init(String name, Map<String, String> configObject) {
        this.name = name;
        try {
            root = Json.parseJsonString(configObject.get(JSON_STORE_CONTENTSTR));
            // TODO: add processing for cacheTTL
        } catch (Exception ex) {
            throw new OneConfigException(String.format("Problem initializing store '%s': ", name, ex));
        }
    }

    public String getName() {
        return name;
    }

    public StoreResult resolvePath(String path) {
        return internalResolveKey(path, root, 0, path);
    }

    private StoreResult parseNodeForResult(JsonNode node, String nodeName, String fullPath) {
        if (node.isTextual()) {
            return new StoreResult(node.textValue());
        }
        return sensorNode(node, fullPath);
    }

    private StoreResult sensorNode(JsonNode node, String fullPath) {
        Map<String, String> sensorValues = new HashMap<String, String>();
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode fieldValue = node.get(fieldName);
            if (!fieldValue.isTextual()) {
                throw new OneConfigException("Sensor field '%s' at path '%s' is expected to be text", fieldName, fullPath);
            }
            sensorValues.put(fieldName, fieldValue.textValue());
        }
        if (sensorValues.get("?") == null) {
            throw new OneConfigException("Sensor at path '%s' must have the field \"?\": \"<SensorName>\"", fullPath);
        }
        return new StoreResult(sensorValues);
    }

    private StoreResult internalResolveKey(String path, JsonNode root, int nestingLevel, String origPath) {
        if (nestingLevel > Const.MAX_KEY_PARTS) {
            throw new OneConfigException("Too many nested levels in the path '%s'", origPath);
        }

        String nodeName = Str.head(path);
        if (nodeName.length() > Const.MAX_KEY_PART_LENGHT) {
            throw new OneConfigException(
                "The lenght of the path component '%s' of the key '%s' is more than allowed %d symbols.",
                nodeName,
                origPath,
                Const.MAX_KEY_PART_LENGHT
            );
        }
        String pathTail = Str.tail(path);

        JsonNode currNode = root.get(nodeName);
        if (currNode == null) {
            throw new OneConfigException("Key '%s' in the path '%s' can't be found in the config file", nodeName, origPath);
        }

        if (pathTail.length() > 0) {
            if (!currNode.isObject()) { // if there are more keys to process but current node does not have internal structure
                throw new OneConfigException("Part '%s' of the path '%s' cannot be reached", pathTail, origPath);
            } else { // drilling down inside the object
                return internalResolveKey(pathTail, currNode, nestingLevel + 1, origPath);
            }
        } else {
            return parseNodeForResult(currNode, nodeName, origPath);
        }
    }
}
