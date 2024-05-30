package com.garganttua.api.core.dto.checker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.dto.exceptions.GGAPIDtoException;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.spec.dto.annotations.GGAPIDto;
import com.garganttua.api.spec.dto.annotations.GGAPIDtoTenantId;


public class GGAPIDtoCheckerTest {

    @Test
    void testCheckDtos() {
        List<Class<?>> dtoClasses = Arrays.asList(DtoClass1.class, DtoClass2.class);
        assertDoesNotThrow(() -> GGAPIDtoChecker.checkDtos(dtoClasses));
    }

    @Test
    void testCheckDtoWithInvalidAnnotation() {
        List<Class<?>> dtoClasses = Arrays.asList(DtoClassWithoutAnnotation.class);
        assertThrows(GGAPIDtoException.class, () -> GGAPIDtoChecker.checkDtos(dtoClasses));
    }

    @Test
    void testCheckDtoWithNoTenantIdField() {
        List<Class<?>> dtoClasses = Arrays.asList(DtoClassWithoutTenantIdField.class);
        assertThrows(GGAPIDtoException.class, () -> GGAPIDtoChecker.checkDtos(dtoClasses));
    }

    @Test
    void testCheckDtoWithInvalidFieldType() {
        List<Class<?>> dtoClasses = Arrays.asList(DtoClassWithInvalidFieldType.class);
        assertThrows(GGAPIDtoException.class, () -> GGAPIDtoChecker.checkDtos(dtoClasses));
    }
}

// Classes de test pour les cas d'exemple

@GGAPIDto(db = "database1", entityClass = GenericGGAPIEntity.class)
class DtoClass1 {
    @GGAPIDtoTenantId
    String tenantId;
}

@GGAPIDto(db = "database2", entityClass = GenericGGAPIEntity.class)
class DtoClass2 {
    @GGAPIDtoTenantId
    String tenantId;
    // Ajouter d'autres champs selon les besoins des tests
}

class DtoClassWithoutAnnotation {
    // Pas d'annotation GGAPIDto ici
}

@GGAPIDto(db = "database3", entityClass = GenericGGAPIEntity.class)
class DtoClassWithoutTenantIdField {
    String someField;
}

@GGAPIDto(db = "database4", entityClass = GenericGGAPIEntity.class)
class DtoClassWithInvalidFieldType {
    @GGAPIDtoTenantId
    Integer tenantId; // Le champ doit être de type String
}