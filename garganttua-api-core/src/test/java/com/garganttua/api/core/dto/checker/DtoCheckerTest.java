package com.garganttua.api.core.dto.checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.dto.exceptions.DtoException;
import com.garganttua.api.core.entity.GenericEntity;
import com.garganttua.api.spec.dto.annotations.Dto;
import com.garganttua.api.spec.dto.annotations.DtoTenantId;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;


public class DtoCheckerTest {

	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
    @Test
    void testCheckDtos() {
        List<Class<?>> dtoClasses = Arrays.asList(DtoClass1.class, DtoClass2.class);
        assertDoesNotThrow(() -> DtoChecker.checkDtosClasses(dtoClasses));
    }

    @Test
    void testCheckDtoWithInvalidAnnotation() {
        List<Class<?>> dtoClasses = Arrays.asList(DtoClassWithoutAnnotation.class);
        assertThrows(DtoException.class, () -> DtoChecker.checkDtosClasses(dtoClasses));
    }

    @Test
    void testCheckDtoWithNoTenantIdField() {
        List<Class<?>> dtoClasses = Arrays.asList(DtoClassWithoutTenantIdField.class);
        assertThrows(DtoException.class, () -> DtoChecker.checkDtosClasses(dtoClasses)); 
    }

    @Test
    void testCheckDtoWithInvalidFieldType() {
        List<Class<?>> dtoClasses = Arrays.asList(DtoClassWithInvalidFieldType.class);
        assertThrows(DtoException.class, () -> DtoChecker.checkDtosClasses(dtoClasses));
    }
}

// Classes de test pour les cas d'exemple

@Dto(db = "database1", entityClass = GenericEntity.class)
class DtoClass1 {
    @DtoTenantId
    String tenantId;
}

@Dto(db = "database2", entityClass = GenericEntity.class)
class DtoClass2 {
    @DtoTenantId
    String tenantId;
    // Ajouter d'autres champs selon les besoins des tests
}

class DtoClassWithoutAnnotation {
    // Pas d'annotation Dto ici
}

@Dto(db = "database3", entityClass = GenericEntity.class)
class DtoClassWithoutTenantIdField {
    String someField;
}

@Dto(db = "database4", entityClass = GenericEntity.class)
class DtoClassWithInvalidFieldType {
    @DtoTenantId
    Integer tenantId; // Le champ doit être de type String
}