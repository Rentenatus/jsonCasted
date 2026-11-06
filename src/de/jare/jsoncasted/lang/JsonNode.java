/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import de.jare.jsoncasted.diff.DifferableNode;
import de.jare.jsoncasted.diff.MerkleHasher;
import de.jare.jsoncasted.diff.NodeKind;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.tools.SimpleStringSplitter;
import static de.jare.jsoncasted.lang.JsonNodeType.ARRAY;
import static de.jare.jsoncasted.lang.JsonNodeType.BOOLEAN;
import static de.jare.jsoncasted.lang.JsonNodeType.LONG;
import static de.jare.jsoncasted.lang.JsonNodeType.NULL;
import static de.jare.jsoncasted.lang.JsonNodeType.NUMBER;
import static de.jare.jsoncasted.lang.JsonNodeType.OBJECT;
import static de.jare.jsoncasted.lang.JsonNodeType.STRING;
import static de.jare.jsoncasted.lang.JsonTerms.COLONCOLON;
import static de.jare.jsoncasted.lang.JsonTerms.PREFIX_THIS;
import static de.jare.jsoncasted.lang.JsonTerms.PREFIX_SELF;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_CLASS;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import java.util.*;

/**
 * Simple JsonNode representation supporting objects, arrays, strings, numbers, booleans and null. This replaces the
 * previous generic HashMap-based class.
 *
 * Note: JsonClass may be attached to OBJECT nodes to aid editing/debugging.
 *
 * @author Janusch Renteantus
 */
public class JsonNode implements DifferableNode {

    private final JsonNodeType type;
    private final Map<String, JsonNode> objectValue;
    private final List<JsonNode> arrayValue;
    private final String textValue;
    private final Double numberValue;
    private final Long numberLongValue;
    private final Boolean boolValue;
    private JsonTypeDescriptor jsonDescriptor; // optional, set for OBJECT nodes
    private String woodObjectId; // identity for nodes that cannot carry members (arrays)
    private Integer sourceLine; // line number in the source JSON text (1-based)
    private Integer sourceColumn; // column position in the source JSON text (0-based)
    private String memberKey; // member key under which this node sits in its parent (set by put)
    private Long merkleHash; // memoized Merkle subtree hash (diff machinery, see doc/TREE_DIFF.md)

    private JsonNode(JsonNodeType type,
            Map<String, JsonNode> objectValue,
            List<JsonNode> arrayValue,
            String textValue,
            Double numberValue,
            Long numberLongValue,
            Boolean boolValue) {
        this.type = type;
        this.objectValue = objectValue;
        this.arrayValue = arrayValue;
        this.textValue = textValue;
        this.numberValue = numberValue;
        this.numberLongValue = numberLongValue;
        this.boolValue = boolValue;
        this.jsonDescriptor = null;
        this.sourceLine = null;
        this.sourceColumn = null;
    }

    public static JsonNode objectNode() {
        return new JsonNode(JsonNodeType.OBJECT, new LinkedHashMap<>(), null, null, null, null, null);
    }

    public static JsonNode arrayNode() {
        return new JsonNode(JsonNodeType.ARRAY, null, new ArrayList<>(), null, null, null, null);
    }

    public static JsonNode arrayNode(List<JsonNode> arrayValue) {
        return new JsonNode(JsonNodeType.ARRAY, null, arrayValue, null, null, null, null);
    }

    public static JsonNode stringNode(String str) {
        return new JsonNode(JsonNodeType.STRING, null, null, str, null, null, null);
    }

    public static JsonNode numberNode(double number) {
        return new JsonNode(JsonNodeType.NUMBER, null, null, null, number, null, null);
    }

    public static JsonNode longNode(long number) {
        return new JsonNode(JsonNodeType.LONG, null, null, null, null, number, null);
    }

    public static JsonNode booleanNode(boolean aBool) {
        return new JsonNode(JsonNodeType.BOOLEAN, null, null, null, null, null, aBool);
    }

    public static JsonNode varNode(String str) {
        if (str == null) {
            return JsonNode.nullNode();
        }
        String trimmed = str.trim();
        if ("null".equals(trimmed)) {
            return JsonNode.nullNode();
        }
        if ("true".equals(trimmed)) {
            return JsonNode.booleanNode(true);
        }
        if ("false".equals(trimmed)) {
            return JsonNode.booleanNode(false);
        }
        // Prefer long when there's no decimal point or exponent
        if (trimmed.matches("-?\\d+")) {
            try {
                return JsonNode.longNode(Long.parseLong(trimmed));
            } catch (NumberFormatException e) {
                // fall through to double parsing as a fallback
            }
        }
        try {
            return JsonNode.numberNode(Double.parseDouble(trimmed));
        } catch (NumberFormatException e) {
            return JsonNode.stringNode(str);
        }
    }

    public static JsonNode nullNode() {
        return new JsonNode(JsonNodeType.NULL, null, null, null, null, null, null);
    }

    // Mutating helpers for building nodes (return this for chaining)
    public JsonNode put(String key, JsonNode value) {
        if (type != JsonNodeType.OBJECT) {
            throw new IllegalStateException("not an object node");
        }
        objectValue.put(key, value);
        value.memberKey = key; // the child carries its member key for the diff projection
        value.merkleHash = null; // the member key is part of the child's Merkle hash
        merkleHash = null;
        return this;
    }

    public JsonNode add(JsonNode value) {
        if (type != JsonNodeType.ARRAY) {
            throw new IllegalStateException("not an array node");
        }
        arrayValue.add(value);
        merkleHash = null;
        return this;
    }

    // JsonClass attachment for OBJECT nodes
    public void setJsonDescriptor(JsonTypeDescriptor jc) {
        this.jsonDescriptor = jc;
    }

    public JsonTypeDescriptor getJsonDescriptor() {
        return this.jsonDescriptor;
    }

    public JsonNodeType getType() {
        return type;
    }

    public boolean isObject() {
        return type == OBJECT;
    }

    public boolean isArray() {
        return type == ARRAY;
    }

    public boolean isNull() {
        return type == NULL;
    }

    public Map<String, JsonNode> asObjectValues() {
        return objectValue;
    }

    public List<JsonNode> asArray() {
        return arrayValue;
    }

    public String asText() {
        return textValue;
    }

    public Double asNumber() {
        return numberValue;
    }

    public Long asLong() {
        return numberLongValue;
    }

    public Boolean asBoolean() {
        return boolValue;
    }

    public Double toNumber() {
        if (numberLongValue != null) {
            return numberLongValue.doubleValue();
        }
        return numberValue;
    }

    public String toText() throws JsonParseException {
        switch (type) {
            case STRING:
                return asText();
            case NUMBER:
                return String.valueOf(asNumber());
            case LONG:
                return String.valueOf(asLong());
            case BOOLEAN:
                return String.valueOf(asBoolean());
            case NULL:
                return "null";
            default:
                throw new JsonParseException("Unsupported JsonNode type: " + type);
        }
    }

    @Override
    public String toString() {
        switch (type) {
            case OBJECT:
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                boolean first = true;
                for (Map.Entry<String, JsonNode> e : objectValue.entrySet()) {
                    if (!first) {
                        sb.append(',');
                    }
                    first = false;
                    sb.append('"').append(escape(e.getKey())).append('"').append(':').append(e.getValue().toString());
                }
                sb.append("}");
                return sb.toString();
            case ARRAY:
                StringBuilder sa = new StringBuilder();
                sa.append('[');
                for (int i = 0; i < arrayValue.size(); i++) {
                    if (i > 0) {
                        sa.append(',');
                    }
                    sa.append(arrayValue.get(i).toString());
                }
                sa.append(']');
                return sa.toString();
            case STRING:
                return '"' + escape(textValue) + '"';
            case NUMBER:
                return numberValue.toString();
            case LONG:
                return numberLongValue.toString();
            case BOOLEAN:
                return boolValue.toString();
            case NULL:
                return "null";
            default:
                return "null";
        }
    }

    public String getObjectId(String providerName) throws JsonParseException {
        final String id = readRawObjectId();
        if (id == null) {
            return null;
        }
        if (id.contains(COLONCOLON)) {
            throw new JsonParseException("IDs must not contain a provider name or a :: sign (" + id + ").");
        }
        return providerName + COLONCOLON + id;
    }

    /**
     * Returns the raw identity of this node: the {@code _woodObjectId} member wins on objects, the array identity
     * field serves nodes that cannot carry members.
     *
     * @return the raw id text, or {@code null} when the node has no identity.
     * @throws JsonParseException if the id member does not hold readable text.
     */
    private String readRawObjectId() throws JsonParseException {
        if (isObject() && objectValue != null) {
            final JsonNode idNode = objectValue.get(JsonTerms.TERM_WOOD_OBJECT_ID);
            if (idNode != null) {
                return idNode.toText();
            }
        }
        return woodObjectId;
    }

    /**
     * Sets the wood identity for nodes that cannot carry members (arrays). Objects use the {@code _woodObjectId}
     * member instead; when both exist, the member wins.
     *
     * @param woodObjectId the raw id text, or {@code null} to remove the identity.
     */
    public void setWoodObjectId(String woodObjectId) {
        this.woodObjectId = woodObjectId;
        this.merkleHash = null;
    }

    /**
     * Returns the raw wood identity of this node without provider normalization.
     *
     * @return the raw id text, or {@code null} when the node has no identity.
     */
    public String getWoodObjectId() {
        return woodObjectId;
    }

    /**
     * Returns the 1-based line number in the source JSON text where this node was parsed.
     *
     * @return the source line number, or {@code null} if not set.
     */
    public Integer getSourceLine() {
        return sourceLine;
    }

    /**
     * Sets the 1-based line number in the source JSON text where this node was parsed.
     *
     * @param sourceLine the line number, or {@code null} to clear.
     */
    public void setSourceLine(Integer sourceLine) {
        this.sourceLine = sourceLine;
    }

    /**
     * Returns the 0-based column position in the source JSON text where this node was parsed.
     *
     * @return the source column, or {@code null} if not set.
     */
    public Integer getSourceColumn() {
        return sourceColumn;
    }

    /**
     * Sets the 0-based column position in the source JSON text where this node was parsed.
     *
     * @param sourceColumn the column position, or {@code null} to clear.
     */
    public void setSourceColumn(Integer sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    /**
     * Sets both source position values at once and returns this node for chaining.
     *
     * @param line the 1-based line number.
     * @param column the 0-based column position.
     * @return this node.
     */
    public JsonNode withSourcePosition(int line, int column) {
        this.sourceLine = line;
        this.sourceColumn = column;
        return this;
    }

    public String getCast() {
        if (!isObject()) {
            return null;
        }
        Map<String, JsonNode> values = asObjectValues();
        if (values == null) {
            return null;
        }
        JsonNode cast = values.get(TERM_CLASS);
        if (cast == null) {
            return null;
        }
        return cast.asText();
    }

    public String getLink(String providerName) throws JsonParseException {
        JsonNode idNode = objectValue.get(JsonTerms.TERM_WOOD_LINK);
        if (idNode == null) {
            return null;
        }
        return normalizeLinkKey(idNode.toText(), providerName);
    }

    private static String normalizeLinkKey(String linkKey, String providerName) {
        if (linkKey == null) {
            return null;
        }
        if (linkKey.startsWith(PREFIX_THIS)) {
            return providerName + COLONCOLON + linkKey.substring(PREFIX_THIS.length());
        }
        if (linkKey.startsWith(PREFIX_SELF)) {
            return providerName + COLONCOLON + linkKey.substring(PREFIX_SELF.length());
        }
        return linkKey;
    }

    // --- DifferableNode: read-only semantic projection for the diff engine (see doc/TREE_DIFF.md) ---
    @Override
    public NodeKind getKind() {
        switch (type) {
            case OBJECT:
                return NodeKind.OBJECT;
            case ARRAY:
                return NodeKind.ARRAY;
            default:
                return NodeKind.VALUE;
        }
    }

    @Override
    public String getObjektId() {
        if (isObject() && objectValue != null) {
            final JsonNode idNode = objectValue.get(JsonTerms.TERM_WOOD_OBJECT_ID);
            if (idNode != null) {
                try {
                    return idNode.toText();
                } catch (JsonParseException e) {
                    return null; // unreadable id member: no identity for the diff
                }
            }
        }
        return woodObjectId;
    }

    @Override
    public String getLinkTarget() {
        if (isObject() && objectValue != null) {
            final JsonNode linkNode = objectValue.get(JsonTerms.TERM_WOOD_LINK);
            if (linkNode != null) {
                return linkNode.asText();
            }
        }
        return null;
    }

    @Override
    public String getMemberKey() {
        return memberKey;
    }

    @Override
    public String getName() {
        // parse layer has no object-value name; the member name is the position (see getMemberKey) — never
        // duplicated here, otherwise a subtree under another key could not hash/validate as the same content
        return null;
    }

    @Override
    public String getValue() {
        // raw scalar text; the type tag is exposed by getValueTag()
        switch (type) {
            case STRING:
                return textValue;
            case NUMBER:
                return numberValue == null ? null : numberValue.toString();
            case LONG:
                return numberLongValue == null ? null : numberLongValue.toString();
            case BOOLEAN:
                return boolValue == null ? null : boolValue.toString();
            default:
                return null;
        }
    }

    @Override
    public String getValueTag() {
        switch (type) {
            case STRING:
                return "string";
            case NUMBER:
                return "number";
            case LONG:
                return "long";
            case BOOLEAN:
                return "boolean";
            case NULL:
                return "null";
            default:
                return null;
        }
    }

    @Override
    public String getCastName() {
        return getCast();
    }

    /**
     * Returns the memoized Merkle subtree hash, computing it bottom-up on first query. Memoized hashes are a
     * building-phase assumption: {@link #put(String, JsonNode)}, {@link #add(JsonNode)} and
     * {@link #setWoodObjectId(String)} invalidate this node (and the inserted child); ancestors of a node mutated
     * <em>after</em> hashing keep their stale hash and must be re-diffed from a freshly built or recomputed tree.
     *
     * @return the 64-bit Merkle hash of the subtree rooted at this node.
     */
    @Override
    public long getMerkleHash() {
        if (merkleHash == null) {
            merkleHash = MerkleHasher.nodeHash(this); // children hash lazily through this call
        }
        return merkleHash;
    }

    @Override
    public List<DifferableNode> children() {
        if (isObject() && objectValue != null) {
            // semantic projection: identity, link and cast members are exposed as projection fields
            // (getObjektId/getLinkTarget/getCastName), never duplicated as children — otherwise one change
            // would produce two deltas
            final List<DifferableNode> result = new ArrayList<>(objectValue.size());
            for (Map.Entry<String, JsonNode> entry : objectValue.entrySet()) {
                if (JsonTerms.TERM_WOOD_OBJECT_ID.equals(entry.getKey())
                        || JsonTerms.TERM_WOOD_LINK.equals(entry.getKey())
                        || JsonTerms.TERM_CLASS.equals(entry.getKey())) {
                    continue;
                }
                result.add(entry.getValue());
            }
            return result; // LinkedHashMap: insertion order preserved
        }
        if (isArray() && arrayValue != null) {
            final List<DifferableNode> result = new ArrayList<>(arrayValue.size());
            result.addAll(arrayValue);
            return result;
        }
        return Collections.emptyList();
    }

    private static String escape(String s) {
        return SimpleStringSplitter.mask(s);
    }

}
