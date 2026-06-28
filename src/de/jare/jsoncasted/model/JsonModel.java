/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.builder.*;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.item.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The JsonModel class serves as a registry for JSON classes and types. It provides methods for managing JSON class
 * definitions, including retrieval, creation, and deletion.
 *
 * <p>
 * This class is the central type registry in the jsonCasted system, maintaining mappings for:
 * </p>
 * <ul>
 * <li>Concrete classes ({@link JsonClass})</li>
 * <li>Interfaces ({@link JsonInter}) with their implementations</li>
 * <li>Enum types (stored as JsonClass in the enums map)</li>
 * <li>Repository models ({@link JsonRepoModel}) for external resources</li>
 * </ul>
 *
 * <p>
 * Note: This class does NOT automatically register field types. If a class has fields with types that need to be
 * deserialized, those types must be explicitly registered in the model before use.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public class JsonModel {

    final HashMap<String, JsonClass> classes;
    final HashMap<String, JsonInter> interfaces;
    final HashMap<String, JsonClass> enums;
    final JsonDefinitions definitionsRoot;
    private final HashMap<String, JsonRepoModel> repoModels;
    private final String mName;
    private JsonModelDescriptor descriptor;

    /**
     * Constructs a JsonModel instance with a specified model name.
     *
     * @param mName The name of the JSON model.
     */
    public JsonModel(String mName) {
        this.mName = mName;
        this.descriptor = null;
        this.classes = new HashMap<>();
        this.interfaces = new HashMap<>();
        this.enums = new HashMap<>();
        this.repoModels = new HashMap<>();
        this.definitionsRoot = new JsonDefinitions(mName + JsonTerms.DEFINITIONS_SUFFIX);
    }

    /**
     * Retrieves the model name.
     *
     * @return The name of the JSON model.
     */
    public String getmName() {
        return mName;
    }

    public JsonDefinitions getDefinitionsRoot() {
        return definitionsRoot;
    }

    /**
     * Returns whether the root definitions contain any local types.
     *
     * @return {@code true} if root definitions contain types.
     */
    public boolean hasDefinitions() {
        return definitionsRoot.hasTypes();
    }

    /**
     * Returns whether the root definitions contain child definition scopes.
     *
     * @return {@code true} if child definition scopes exist.
     */
    public boolean hasDefinitionChildren() {
        return definitionsRoot.hasChildren();
    }

    /**
     * Returns whether the root definitions are empty.
     *
     * @return {@code true} if no types and no child definition scopes exist.
     */
    public boolean isDefinitionsEmpty() {
        return definitionsRoot.isEmpty();
    }

    /**
     * Adds a type to the root definitions.
     *
     * @param type The type to add.
     */
    public void addDefinitionType(JsonType type) {
        definitionsRoot.addType(type);
    }

    /**
     * Adds a type to the root definitions if absent.
     *
     * @param type The type to add.
     * @return {@code true} if the type was added.
     */
    public boolean addDefinitionTypeIfAbsent(JsonType type) {
        return definitionsRoot.addTypeIfAbsent(type);
    }

    /**
     * Adds a child definitions scope to the root definitions.
     *
     * @param child The child definitions scope.
     */
    public void addDefinitionsChild(JsonDefinitions child) {
        definitionsRoot.addChild(child);
    }

    /**
     * Returns whether the root definitions contain the given type locally.
     *
     * @param cName The canonical type name.
     * @return {@code true} if found locally.
     */
    public boolean containsLocalDefinitionType(String cName) {
        return definitionsRoot.containsLocalType(cName);
    }

    /**
     * Returns the local type from the root definitions.
     *
     * @param cName The canonical type name.
     * @return The type, or {@code null} if not found.
     */
    public JsonType getLocalDefinitionType(String cName) {
        return definitionsRoot.getLocalType(cName);
    }

    /**
     * Finds a type in the definitions tree.
     *
     * @param cName The canonical type name.
     * @return The type, or {@code null} if not found.
     */
    public JsonType findDefinitionType(String cName) {
        return definitionsRoot.findType(cName);
    }

    /**
     * Finds a definitions scope in the definitions tree.
     *
     * @param name The definitions scope name.
     * @return The definitions scope, or {@code null} if not found.
     */
    public JsonDefinitions findDefinitions(String name) {
        return definitionsRoot.findDefinitions(name);
    }

    /**
     * Returns an iterator over the root-local definition types.
     *
     * @return Iterator over local definition types.
     */
    public Iterator<JsonType> definitionTypesIterator() {
        return definitionsRoot.typesIterator();
    }

    /**
     * Returns an iterator over all definition types in the tree.
     *
     * @return Iterator over all definition types.
     */
    public Iterator<JsonType> definitionTypesDeepIterator() {
        return definitionsRoot.typesDeepIterator();
    }

    /**
     * Returns an iterator over the direct child definitions of the root.
     *
     * @return Iterator over child definitions.
     */
    public Iterator<JsonDefinitions> definitionChildrenIterator() {
        return definitionsRoot.childrenIterator();
    }

    /**
     * Returns an iterator over all child definitions in the tree.
     *
     * @return Iterator over all nested child definitions.
     */
    public Iterator<JsonDefinitions> definitionChildrenDeepIterator() {
        return definitionsRoot.childrenDeepIterator();
    }

    /**
     * Removes a local type from the root definitions by name.
     *
     * @param cName The canonical type name.
     * @return The removed type, or {@code null} if not found.
     */
    public JsonType removeLocalDefinitionType(String cName) {
        return definitionsRoot.removeType(cName);
    }

    /**
     * Clears the complete definitions tree.
     */
    public void clearDefinitions() {
        definitionsRoot.clear();
    }

    /**
     * Adds a JSON class to the model registry.
     * <p>
     * Note: This method only registers the class itself. Field types must be registered separately. Unlike previous
     * versions, there is no automatic recursive registration of field types.
     * </p>
     *
     * @param jClass The JSON class to register.
     */
    public void addClass(JsonClass jClass) {
        classes.put(jClass.getcName(), jClass);
    }

    /**
     * Adds a JSON interface to the model registry along with all its implementations. The interface and all its
     * concrete implementations are registered in the model.
     *
     * @param inter The JSON interface to register.
     */
    public void addInterface(JsonInter inter) {
        for (JsonClass jc : inter) {
            if (classes.containsKey(jc.getcName())) {
                continue;
            }
            addClass(jc);
        }
        interfaces.put(inter.getcName(), inter);

    }

    /**
     * Retrieves a JSON class by its name.
     *
     * @param key The name of the JSON class.
     * @return The corresponding JsonClass, or null if not found.
     */
    public JsonClass getJsonClass(String key) {
        return classes.get(key.trim());
    }

    /**
     * Finds a JSON class whose name ends with the specified string. Useful for finding classes by simple name when the
     * full qualified name is unknown.
     *
     * @param cNameEnding The suffix to match.
     * @return The matching JsonClass, or null if none found.
     */
    public JsonClass getEndsWith(String cNameEnding) {
        for (JsonClass jc : classes.values()) {
            if (jc.getcName().endsWith(cNameEnding)) {
                return jc;
            }
        }
        return null;
    }

    /**
     * Retrieves a JSON class by its corresponding Java class.
     *
     * @param clazz The Java class.
     * @return The matching JsonClass, or null if not found.
     */
    public JsonClass getJsonClass(Class<?> clazz) {
        return classes.get(clazz.getTypeName());
    }

    /**
     * Retrieves a JSON interface by its name.
     *
     * @param key The name of the JSON interface.
     * @return The corresponding JsonInter, or null if not found.
     */
    public JsonInter getJsonInter(String key) {
        return interfaces.get(key.trim());
    }

    /**
     * Retrieves a JSON enum by its name.
     *
     * @param key The name of the JSON enum.
     * @return The corresponding JsonClass, or null if not found.
     */
    public JsonClass getJsonEnum(String key) {
        return enums.get(key.trim());
    }

    /**
     * Removes a JSON class from the model registry.
     *
     * @param jClass The JSON class to remove.
     * @return The removed JsonClass, or null if not found.
     */
    public JsonClass remove(JsonClass jClass) {
        return classes.remove(jClass.getcName());
    }

    /**
     * Returns an iterator over the registered JSON classes.
     *
     * @return An iterator over JsonClass objects.
     */
    public Iterator<JsonClass> classesIterator() {
        return classes.values().iterator();
    }

    /**
     * Retrieves the set of registered class names.
     *
     * @return A set of class names.
     */
    public Set<String> classesKeySet() {
        return classes.keySet();
    }

    /**
     * Adds a repository model with a given synonym. Repository models allow referencing types from external JSON
     * resources.
     *
     * @param synonym The synonym/identifier for the repository model.
     * @param providerModel The JsonRepoModel instance representing the repository.
     */
    public void addRepoModel(String synonym, JsonRepoModel providerModel) {
        repoModels.put(synonym, providerModel);
    }

    /**
     * Retrieves a repository model by its synonym.
     *
     * @param synonym The synonym/identifier of the repository model.
     * @return The JsonRepoModel instance, or null if not found.
     */
    public JsonRepoModel getRepoModel(String synonym) {
        return repoModels.get(synonym);
    }

    /**
     * Returns an iterator over the registered repository models.
     *
     * @return An iterator over JsonRepoModel objects.
     */
    public Iterator<JsonRepoModel> repoModelsIterator() {
        return repoModels.values().iterator();
    }

    /**
     * Returns the appropriate model for a given provider synonym. If a repository model is registered for the synonym,
     * it is returned. Otherwise, this main model is returned as fallback.
     *
     * @param synonym The provider synonym to look up.
     * @return The JsonModel for the synonym, or this main model if not found.
     */
    public JsonModel getModelForSynonym(String synonym) {
        JsonRepoModel repoModel = repoModels.get(synonym);
        return repoModel != null ? repoModel : this;
    }

    /**
     * Populates the model with basic data types used in JSON processing. Registers primitive wrapper types (String,
     * Integer, Long, Float, Double, Boolean) and their primitive counterparts (int, long, float, double, boolean).
     */
    public void addBasicModel() {
        addClass(new JsonClass("String", JsonNodeType.STRING, new JsonStringBuilder()));
        addClass(new JsonClass("Integer", JsonNodeType.LONG, new JsonIntegerObjBuilder()));
        addClass(new JsonClass("Long", JsonNodeType.LONG, new JsonLongObjBuilder()));
        addClass(new JsonClass("Float", JsonNodeType.NUMBER, new JsonFloatObjBuilder()));
        addClass(new JsonClass("Double", JsonNodeType.NUMBER, new JsonDoubleObjBuilder()));
        addClass(new JsonClass("Boolean", JsonNodeType.BOOLEAN, new JsonBooleanObjBuilder()));
        addClass(new JsonClass("int", JsonNodeType.LONG, new JsonIntBuilder()));
        addClass(new JsonClass("long", JsonNodeType.LONG, new JsonLongBuilder()));
        addClass(new JsonClass("float", JsonNodeType.NUMBER, new JsonFloatBuilder()));
        addClass(new JsonClass("double", JsonNodeType.NUMBER, new JsonDoubleBuilder()));
        addClass(new JsonClass("boolean", JsonNodeType.BOOLEAN, new JsonBooleanBuilder()));
    }

    // Methods for dynamically creating JSON class definitions
    /**
     * Creates a new JSON class with the specified Java class and builder. The class is automatically registered in this
     * model.
     *
     * @param clazz The Java class to model.
     * @param builder The builder for creating instances.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonClass(Class<?> clazz, JsonModellClassBuilder builder) {

        return newJsonClassIndividually(clazz, clazz.getTypeName(), builder);
    }

    /**
     * Creates a new JSON class with the specified Java class and builder.The class is automatically registered in this
     * model.
     *
     * @param clazz The Java class to model.
     * @param cNameOrNull
     * @param builder The builder for creating instances.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonClassIndividually(Class<?> clazz, String cNameOrNull, JsonModellClassBuilder builder) {
        if (cNameOrNull == null) {
            cNameOrNull = clazz.getTypeName();
        }
        JsonClass ret = new JsonClass(cNameOrNull, builder);
        addClass(ret);
        return ret;
    }

    /**
     * Creates a new JSON class with the specified Java class, node type, and builder. The class is automatically
     * registered in this model.
     *
     * @param clazz The Java class to model.
     * @param nodeType The JSON node type for this class.
     * @param builder The builder for creating instances.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonClass(Class<?> clazz, JsonNodeType nodeType, JsonModellClassBuilder builder) {
        return newJsonClassIndividually(clazz, clazz.getTypeName(), nodeType, builder);
    }

    /**
     * Creates a new JSON class with the specified Java class, node type, and builder.The class is automatically
     * registered in this model.
     *
     * @param clazz The Java class to model.
     * @param cNameOrNull
     * @param nodeType The JSON node type for this class.
     * @param builder The builder for creating instances.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonClassIndividually(Class<?> clazz, String cNameOrNull, JsonNodeType nodeType,
            JsonModellClassBuilder builder) {
        if (cNameOrNull == null) {
            cNameOrNull = clazz.getTypeName();
        }
        JsonClass ret = new JsonClass(cNameOrNull, nodeType, builder);
        addClass(ret);
        return ret;
    }

    /**
     * Creates a new JSON class using reflection for the specified Java class. Uses the default JsonReflectBuilder for
     * instantiation. The class is automatically registered in this model.
     *
     * @param clazz The Java class to model.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonReflect(Class<?> clazz) {
        return newJsonReflectIndividually(clazz, clazz.getTypeName());
    }

    /**
     * Creates a new JSON class using reflection for the specified Java class.Uses the default JsonReflectBuilder for
     * instantiation.Uses theThe class is automatically registered in this model.* @param clazz The Java class to model.
     *
     *
     * @param clazz
     * @param cNameOrNull@return The newly created JsonClass, already registered in this model.
     * @return
     */
    public JsonClass newJsonReflectIndividually(Class<?> clazz, String cNameOrNull) {
        if (cNameOrNull == null) {
            cNameOrNull = clazz.getTypeName();
        }
        JsonClass ret = new JsonClass(cNameOrNull, new JsonReflectBuilder(clazz));
        addClass(ret);
        return ret;
    }

    /**
     * Creates a new JSON class using reflection for the specified Java class. Uses the default JsonReflectBuilder for
     * instantiation. The class is automatically registered in this model.
     *
     * @param clazz The Java class to model.
     * @param skippingNulls Whether to skip null values when building.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonReflect(Class<?> clazz, boolean skippingNulls) {
        return newJsonReflectIndividually(clazz, clazz.getTypeName(), skippingNulls);
    }

    /**
     * Creates a new JSON class using reflection for the specified Java claUses the default JsonReflectBuilder for
     * instantiation.ss. The class is automatically registered in this model.el.
     *
     * @param clazz The Java class to model
     * @param cNameOrNull
     * @param skippingNulls Whether to skip null values when building.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonReflectIndividually(Class<?> clazz, String cNameOrNull, boolean skippingNulls) {
        if (cNameOrNull == null) {
            cNameOrNull = clazz.getTypeName();
        }
        JsonClass ret = new JsonClass(cNameOrNull, skippingNulls, new JsonReflectBuilder(clazz));
        addClass(ret);
        return ret;
    }

    /**
     * Creates a new JSON class using reflection for the specified Java class, inheriting fields from the specified
     * parent class. The class is automatically registered in this model.
     *
     * @param clazz The Java class to model.
     * @param parent The parent JsonClass to inherit fields from.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonReflect(Class<?> clazz, JsonClass parent) {
        return newJsonReflectIndividually(clazz, clazz.getTypeName(), parent);
    }

    /**
     * Creates a new JSON class using reflection for the specified Java class, inheriting fields from the specified
     * parent class.The class is automatically registered in this model.
     *
     * @param clazz The Java class to model.
     * @param cNameOrNull
     * @param parent The parent JsonClass to inherit fields from.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonReflectIndividually(Class<?> clazz, String cNameOrNull, JsonClass parent) {
        JsonClass ret = newJsonReflectIndividually(clazz, cNameOrNull);
        ret.addFromSuperclass(parent);
        return ret;
    }

    /**
     * Creates a new JSON class using reflection for the specified Java class, inheriting fields from the specified
     * parent class. The class is automatically registered in this model.
     *
     * @param clazz The Java class to model.
     * @param parent The parent JsonClass to inherit fields from.
     * @param skippingNulls Whether to skip null values when building.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonReflect(Class<?> clazz, JsonClass parent, boolean skippingNulls) {
        return newJsonReflectIndividually(clazz, clazz.getTypeName(), parent, skippingNulls);
    }

    /**
     * Creates a new JSON class using reflection for the specified Java class, inheriting fields from the specified
     * parent class.The class is automatically registered in this model.
     *
     * @param clazz The Java class to model.
     * @param cNameOrNull
     * @param parent The parent JsonClass to inherit fields from.
     * @param skippingNulls Whether to skip null values when building.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonReflectIndividually(Class<?> clazz, String cNameOrNull, JsonClass parent,
            boolean skippingNulls) {
        JsonClass ret = newJsonReflectIndividually(clazz, cNameOrNull, skippingNulls);
        ret.addFromSuperclass(parent);
        return ret;
    }

    /**
     * Creates a new JSON class for an enum type using name-based resolution. Enum values are resolved by calling the
     * static getByName method on the enum. The class is automatically registered in this model's enum registry.
     *
     * @param clazz The Java enum class to model.
     * @param valuesArray The array of enum templates for name-based lookup.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonEnumByName(Class<?> clazz, JsonEnumTemplate... valuesArray) {
        return newJsonEnumByNameIndividually(clazz, clazz.getTypeName(), valuesArray);
    }

    /**
     * Creates a new JSON class for an enum type using name-based resolution.Enum values are resolved by calling the
     * static getByName method on the enum. The class is automatically registered in this model's enum registry.
     *
     * @param clazz The Java enum class to model.
     * @param cNameOrNull
     * @param valuesArray The array of enum templates for name-based lookup.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonEnumByNameIndividually(Class<?> clazz, String cNameOrNull,
            JsonEnumTemplate... valuesArray) {
        if (cNameOrNull == null) {
            cNameOrNull = clazz.getTypeName();
        }
        final JsonClass ret = new JsonClass(cNameOrNull, JsonNodeType.STRING, new JsonEnumByNameBuilder(clazz));
        ret.setValuesArray(valuesArray);
        enums.put(ret.getcName(), ret);
        return ret;
    }

    /**
     * Creates a new JSON class for an enum type using name-based resolution. Enum values are resolved by calling the
     * static getByName method on the enum. The class is automatically registered in this model's enum registry.
     *
     * @param clazz The Java enum class to model.
     * @param valuesList The list of enum templates for name-based lookup.
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonEnumByName(Class<?> clazz, List<? extends JsonEnumTemplate> valuesList) {
        return newJsonEnumByNameIndividually(clazz, clazz.getTypeName(), valuesList);
    }

    /**
     * Creates a new JSON class for an enum type using name-based resolution.Enum values are resolved by calling the
     * static getByName method on the enum. * EnuThe class is automatically registered in this model's enum registry.
     *
     *
     * @param clazz The Java enum class to model.
     * @param cNameOrNull * @param valuesList The list of enum templates for name-based lookup.
     * @param valuesList
     * @return The newly created JsonClass, already registered in this model.
     */
    public JsonClass newJsonEnumByNameIndividually(Class<?> clazz, String cNameOrNull,
            List<? extends JsonEnumTemplate> valuesList) {
        if (cNameOrNull == null) {
            cNameOrNull = clazz.getTypeName();
        }
        final JsonClass ret = new JsonClass(cNameOrNull, JsonNodeType.STRING, new JsonEnumByNameBuilder(clazz));
        ret.setValuesArray(valuesList.toArray(JsonEnumTemplate[]::new));
        enums.put(ret.getcName(), ret);
        return ret;
    }

    /**
     * Creates a new JSON interface with the specified Java interface type and its allowed implementations. The
     * interface is automatically registered in this model.
     *
     * @param clazz The Java interface class to model.
     * @param jClass The allowed implementations of this interface.
     * @return The newly created JsonInter, already registered in this model.
     */
    public JsonInter newJsonInterface(Class<?> clazz, JsonClass... jClass) {
        return newJsonInterfaceIndividually(clazz, clazz.getTypeName(), jClass);
    }

    /**
     * Creates a new JSON interface with the specified Java interface type and its allowed implementations.The interface
     * is automatically registered in this model.
     *
     * @param clazz The Java interface class to model.
     * @param cNameOrNull
     * @param jClass The allowed implementations of this interface.
     * @return The newly created JsonInter, already registered in this model.
     */
    public JsonInter newJsonInterfaceIndividually(Class<?> clazz, String cNameOrNull, JsonClass... jClass) {
        if (cNameOrNull == null) {
            cNameOrNull = clazz.getTypeName();
        }
        final JsonInter ret = new JsonInter(cNameOrNull, new JsonReflectBuilder(clazz), jClass);
        interfaces.put(ret.getcName(), ret);
        return ret;
    }

    /**
     * Creates a new JSON map class for the specified Map implementation. Map types represent JSON objects with dynamic
     * keys mapping to values of a specific type. The map is automatically registered in this model.
     *
     * @param clazz The Java Map class to model.
     * @param itemClass The JsonClass for the map values.
     * @param colType The collection type (NONE, LIST, ARRAY).
     * @return The newly created JsonMap, already registered in this model.
     */
    public JsonMap newJsonMap(Class<? extends JsonInstance<?>> clazz, JsonClass itemClass, JsonCollectionType colType) {
        return newJsonMapIndividually(clazz, clazz.getTypeName(), itemClass, colType);
    }

    /**
     * Creates a new JSON map class for the specified Map implementation.Map types represent JSON objects with dynamic
     * keys mapping to values of a specific type.MThe map is automatically registered in this model.
     *
     *
     * @param clazz The Java Map class to model.
     * @param cNameOrNull * @param itemClass The JsonClass for the map values.
     * @param itemClass
     * @param colType The collection type (NONE, LIST, ARRAY).
     * @return The newly created JsonMap, already registered in this model.
     */
    public JsonMap newJsonMapIndividually(Class<? extends JsonInstance<?>> clazz, String cNameOrNull,
            JsonClass itemClass, JsonCollectionType colType) {
        if (cNameOrNull == null) {
            cNameOrNull = clazz.getTypeName() + "<" + itemClass.getcName() + ">"
                    + (colType == JsonCollectionType.NONE ? "" : "[]");
        }
        JsonMap ret = new JsonMap(cNameOrNull, clazz, itemClass, colType);
        addClass(ret);
        return ret;
    }

    /**
     * Creates a new JSON map class for the specified Map implementation with skipping nulls configuration. Map types
     * represent JSON objects with dynamic keys mapping to values of a specific type. The map is automatically
     * registered in this model.
     *
     * @param clazz The Java Map class to model.
     * @param skippingNulls Whether to skip null values when building.
     * @param itemClass The JsonClass for the map values.
     * @param colType The collection type (NONE, LIST, ARRAY).
     * @return The newly created JsonMap, already registered in this model.
     */
    public JsonMap newJsonMap(Class<? extends JsonInstance<?>> clazz, boolean skippingNulls, JsonClass itemClass,
            JsonCollectionType colType) {
        return newJsonMapIndividually(clazz, clazz.getTypeName(), skippingNulls, itemClass, colType);
    }

    /**
     * Creates a new JSON map class for the specified Map implementation with skipping nulls configuration.Map types
     * represent JSON objects with dynamic keys mapping to values of a specific type.MThe map is automatically
     * registered in this model.
     *
     *
     * @param clazz The Java Map class to model.
     * @param cNameOrNull * @param skippingNulls Whether to skip null values when building.
     * @param skippingNulls
     * @param itemClass The JsonClass for the map values.
     * @param colType The collection type (NONE, LIST, ARRAY).
     * @return The newly created JsonMap, already registered in this model.
     */
    public JsonMap newJsonMapIndividually(Class<? extends JsonInstance<?>> clazz, String cNameOrNull,
            boolean skippingNulls, JsonClass itemClass, JsonCollectionType colType) {
        if (cNameOrNull == null) {
            cNameOrNull = clazz.getTypeName() + "<" + itemClass.getcName() + ">"
                    + (colType == JsonCollectionType.NONE ? "" : "[]");
        }
        JsonMap ret = new JsonMap(cNameOrNull, skippingNulls, clazz, itemClass, colType);
        addClass(ret);
        return ret;
    }

    /**
     * Creates a new raw JSON map class (without collection type suffix) using the raw JsonInstance type.The map is
     * automatically registered in this model.
     *
     * @param clazz The raw JsonInstance class to model.
     * @param cNameOrNull
     * @param itemClass The JsonClass for the map values.
     * @return The newly created JsonMap, already registered in this model.
     */
    public JsonMap newRawJsonMapIndividually(Class<? extends JsonInstance> clazz, String cNameOrNull,
            JsonClass itemClass) {
        return newJsonMapIndividually((Class<? extends JsonInstance<?>>) clazz, cNameOrNull, itemClass,
                JsonCollectionType.NONE);
    }

    /**
     * Creates a new raw JSON map class (without collection type suffix) using the raw JsonInstance type.The map is
     * automatically registered in this model.
     *
     * @param clazz The raw JsonInstance class to model.
     * @param cNameOrNull
     * @param skippingNulls Whether to skip null values when building.
     * @param itemClass The JsonClass for the map values.
     * @return The newly created JsonMap, already registered in this model.
     */
    public JsonMap newRawJsonMapIndividually(Class<? extends JsonInstance> clazz, String cNameOrNull,
            boolean skippingNulls, JsonClass itemClass) {
        return newJsonMapIndividually((Class<? extends JsonInstance<?>>) clazz, cNameOrNull, skippingNulls, itemClass,
                JsonCollectionType.NONE);
    }

    /**
     * Creates a new raw JSON map class with the specified collection type using the raw JsonInstance type.The map is
     * automatically registered in this model.
     *
     * @param clazz The raw JsonInstance class to model.
     * @param cNameOrNull
     * @param itemClass The JsonClass for the map values.
     * @param type The collection type.
     * @return The newly created JsonMap, already registered in this model.
     */
    public JsonMap newRawJsonMapIndividually(Class<? extends JsonInstance> clazz, String cNameOrNull,
            JsonClass itemClass, JsonCollectionType type) {
        return newJsonMapIndividually((Class<? extends JsonInstance<?>>) clazz, cNameOrNull, itemClass, type);
    }

    /**
     * Creates a new raw JSON map class with the specified collection type using the raw JsonInstance type.The map is
     * automatically registered in this model.
     *
     * @param clazz The raw JsonInstance class to model.
     * @param cNameOrNull
     * @param skippingNulls Whether to skip null values when building.
     * @param itemClass The JsonClass for the map values.
     * @param type The collection type.
     * @return The newly created JsonMap, already registered in this model.
     */
    public JsonMap newRawJsonMapIndividually(Class<? extends JsonInstance> clazz, String cNameOrNull,
            boolean skippingNulls, JsonClass itemClass, JsonCollectionType type) {
        return newJsonMapIndividually((Class<? extends JsonInstance<?>>) clazz, cNameOrNull, skippingNulls, itemClass,
                type);
    }

    /**
     * Returns a list of all registered classes, with simple-named classes first, followed by fully-qualified class
     * names. This ordering prioritizes more commonly used simple names.
     *
     * @return An ordered list of JsonClass instances.
     */
    private List<JsonClass> getClassesList() {
        List<JsonClass> ordered = new ArrayList<>(classes.size());
        for (JsonClass jc : classes.values()) {
            if (!jc.getcName().contains(".")) {
                ordered.add(jc);
            }
        }
        for (JsonClass jc : classes.values()) {
            if (jc.getcName().contains(".")) {
                ordered.add(jc);
            }
        }
        return ordered;
    }

    /**
     * Returns a sorted list of all registered interfaces. Interfaces are sorted alphabetically by their class name.
     *
     * @return An ordered list of JsonInter instances, sorted by class name.
     */
    private List<JsonInter> getOrderedInterfacesList() {
        List<JsonInter> ordered = new ArrayList<>(interfaces.values());
        ordered.sort(Comparator.comparing(JsonInter::getcName));
        return ordered;
    }

    /**
     * Creates a complete descriptor of this model for introspection purposes. The descriptor includes all types, their
     * fields, dependencies, and relationships. This is useful for editor tools and debugging.
     *
     * @return A JsonModelDescriptor containing the complete model structure.
     */
    public JsonModelDescriptor describe() {
        JsonModelDescriptor context = new JsonModelDescriptor(mName);
        List<JsonClass> orderedClasses = getClassesList();
        List<JsonInter> orderedInterfaces = getOrderedInterfacesList();

        for (JsonClass jsonClass : orderedClasses) {
            context.addType(jsonClass.describeHead(context));
        }
        for (JsonClass jsonClass : enums.values()) {
            context.addType(jsonClass.describeHead(context));
        }

        for (JsonInter jsonInter : orderedInterfaces) {
            for (JsonClass next : jsonInter) {
                if (!context.containsType(next.getcName())) {
                    context.addType(next.describeHead(context));
                    orderedClasses.add(next);
                }
            }
            context.addType(jsonInter.describeHeadInterface(context));
        }

        for (JsonClass jsonClass : orderedClasses) {
            jsonClass.describeDependencies(context);
        }

        repoModels.forEach((synonym, repoModel) -> {
            JsonModelDescriptor repoDescriptor = repoModel.getOrCreateDescriptor();
            context.addRepoDescriptor(synonym, repoDescriptor);
        });

        if (definitionsRoot != null && !definitionsRoot.isEmpty()) {
            context.setDefinitionsRoot(definitionsRoot.describe());
        }

        descriptor = context;
        return context;
    }

    /**
     * Returns the cached model descriptor, or creates one if not yet generated. The descriptor is cached for
     * performance, so subsequent calls return the same instance.
     *
     * @return The JsonModelDescriptor for this model.
     */
    public JsonModelDescriptor getOrCreateDescriptor() {
        if (descriptor != null) {
            return descriptor;
        }
        return describe();
    }

}
