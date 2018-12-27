package com.huawei.cloud.oneconfig.stores;

import com.fasterxml.jackson.databind.JsonNode;
import com.huawei.cloud.oneconfig.Const;
import com.huawei.cloud.oneconfig.OneConfigException;

public class JsonStore implements IStore {
    private String name;
    private JsonNode root;

    public void init(String name, Object configObject) {
        this.name = name;
        try {
            root = (JsonNode) configObject; // this is a shortcut, needs to be redesigned
        } catch (Exception ex) {
            throw new OneConfigException(String.format("Problem initializing store '%s': ", name, ex));
        }
    }

    public String getName() {
        return name;
    }

    public String resolveKey(String key) {
        return internalResolveKey(key, root, 0, key);
    }

    private String internalResolveKey(String key, JsonNode root, int nestLevel, String origKey) {
        if (nestLevel > Const.MAX_LEVELS) {
            throw new OneConfigException(String.format("Too many nested levels in the key '%s'", origKey));
        }

        int dotPos = key.indexOf('.');
        String subKey = "";
        if (dotPos == -1) {
            subKey = key;
        } else {
            subKey = key.substring(0, dotPos);
        }
        JsonNode subNode = root.get(subKey);
        if (subNode.isContainerNode()) {
            return internalResolveKey(key.substring(dotPos + 1), subNode, nestLevel + 1, key);
        }

        return subNode.textValue();
    }


}
