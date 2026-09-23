/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import de.jare.jsoncasted.model.JsonCollectionType;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.validation.reflectdefault.CollectionTypeMatchValidator;
import de.jare.jsoncasted.model.validation.reflectdefault.ConstructorArityValidator;
import de.jare.jsoncasted.model.validation.reflectdefault.GetterSetterValidator;
import de.jare.jsonconfig.def.JsonConfigDefinition;
import de.jare.jsoncasted.model.descriptor.def.JsonModelDescriptorDefinition;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Tests for the reflection-based validators registered by the ReflectionValidatorContributor. The positive test
 * asserts that the Seed config model produces no reflection diagnostics. The meta model test documents the known
 * defects of the descriptor classes. The negative test asserts that deliberately broken declarations are detected.
 *
 * @author Janusch Rentenatus
 */
public class ReflectionValidatorNGTest {

    /**
     * The Seed config model must produce no reflection diagnostics: every declared getter, setter and constructor
     * must exist on the real config classes.
     */
    @Test
    public void testSeedModelHasNoReflectionDiagnostics() {
        List<ValidationDiagnostic> found = reflectionDiagnostics(
                JsonConfigDefinition.getInstance().getModel());
        assertTrue(found.isEmpty(), "Expected no reflection diagnostics, but found: " + found);
    }

    /**
     * The descriptor meta model currently has one known defect that the reflection validators expose: the inherited
     * constructor parameters of JsonFieldDescriptor (collectionType, typeName from JsonFieldTypeNote) precede the
     * own ones in build order, but the public seven-arg constructor of JsonFieldDescriptor declares
     * (fieldName, typeName, collectionType, ...) - so loading a description fails with 'constructor not found'.
     * When the constructor parameter order is fixed, this test must be tightened to expect no diagnostics at all.
     */
    @Test
    public void testSelfDescriptionKnownDefects() {
        List<ValidationDiagnostic> found = reflectionDiagnostics(
                JsonModelDescriptorDefinition.getInstance().getModel());
        assertEquals(found.size(), 1, "Expected exactly the known defect: " + found);
        assertEquals(found.get(0).getCode(), ConstructorArityValidator.CTOR_SIGNATURE_CODE,
                "Expected the JsonFieldDescriptor constructor signature defect: " + found);
    }

    /**
     * A deliberately broken model must be detected: missing setter, missing getter, constructor arity mismatch,
     * missing no-arg constructor and collection type mismatches.
     */
    @Test
    public void testBrokenModelIsDetected() {
        final JsonModel model = new JsonModel("BrokenModel");
        model.addBasicModel();
        final JsonClass asString = model.getJsonClass("String");

        final JsonClass broken = model.newJsonReflect(BrokenBean.class);
        broken.addField("ok", asString, "getOk", "setOk");
        broken.addField("noSetter", asString, "getNoSetter", "setNoSetter");
        broken.addField("noGetter", asString, "getDoesNotExist", "setNoGetter");
        broken.addCParam("alpha", asString);

        final JsonClass onlyArgs = model.newJsonReflect(OnlyArgsBean.class);
        onlyArgs.addField("value", asString);

        final JsonClass wrongSignature = model.newJsonReflect(WrongSignatureBean.class);
        wrongSignature.addCParam("number", asString);

        final JsonClass collections = model.newJsonReflect(CollectionBean.class);
        collections.addField("names", asString, JsonCollectionType.LIST);
        collections.addField("tags", asString, JsonCollectionType.ARRAY);

        final ValidationResult result = new ValidationRunner().validate(model);
        final List<ValidationDiagnostic> found = result.getDiagnostics();

        assertHasCode(found, GetterSetterValidator.SETTER_MISSING_CODE, "noSetter");
        assertHasCode(found, GetterSetterValidator.GETTER_MISSING_CODE, "noGetter");
        assertHasCode(found, ConstructorArityValidator.CTOR_ARITY_CODE, null);
        assertHasCode(found, ConstructorArityValidator.CTOR_SIGNATURE_CODE, null);
        assertHasCode(found, ConstructorArityValidator.CTOR_DEFAULT_CODE, null);
        assertHasCode(found, CollectionTypeMatchValidator.LIST_MISMATCH_CODE, "names");
        assertHasCode(found, CollectionTypeMatchValidator.ARRAY_MISMATCH_CODE, "tags");
    }

    private static List<ValidationDiagnostic> reflectionDiagnostics(JsonModel model) {
        final ValidationResult result = new ValidationRunner().validate(model);
        return result.getDiagnostics().stream()
                .filter(d -> d.getCode() != null && d.getCode().startsWith("reflection."))
                .toList();
    }

    private static void assertHasCode(List<ValidationDiagnostic> found, String code, String fieldName) {
        for (ValidationDiagnostic d : found) {
            if (code.equals(d.getCode())) {
                if (fieldName == null) {
                    return;
                }
                final Object source = d.getSource();
                if (source instanceof JsonField field && fieldName.equals(field.getfName())) {
                    return;
                }
            }
        }
        fail("Expected diagnostic with code '" + code + "'"
                + (fieldName == null ? "" : " for field '" + fieldName + "'")
                + ", but found: " + found);
    }

    /**
     * Bean with a valid field, a field without setter, a field without getter and one declared constructor
     * parameter although only a no-arg constructor exists.
     */
    public static class BrokenBean {

        public BrokenBean() {
        }

        public String getOk() {
            return null;
        }

        public void setOk(String ok) {
        }

        public String getNoSetter() {
            return null;
        }

        public void setNoGetter(String value) {
        }
    }

    /**
     * Bean with a two-arg constructor only, so no declared no-arg constructor exists.
     */
    public static class OnlyArgsBean {

        public OnlyArgsBean(String name, int number) {
        }

        public String getValue() {
            return null;
        }

        public void setValue(String value) {
        }
    }

    /**
     * Bean with a constructor of the right arity but the wrong parameter type for the declared constructor
     * parameter.
     */
    public static class WrongSignatureBean {

        public WrongSignatureBean(int number) {
        }
    }

    /**
     * Bean whose getters return the wrong collection kind for the declared collection types.
     */
    public static class CollectionBean {

        public String[] getNames() {
            return null;
        }

        public void setNames(String[] names) {
        }

        public java.util.List<String> getTags() {
            return null;
        }

        public void setTags(java.util.List<String> tags) {
        }
    }
}
