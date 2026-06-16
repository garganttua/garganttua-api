package com.garganttua.dao.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.garganttua.api.commons.definition.DtoComposition;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.core.mapper.annotations.FieldMappingRule;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * MongoDB-native field projection (layer b): a {@code find(..., projection)} pushes a
 * {@code Projections.include(...)} of the requested fields down to the query, always force-including
 * the uuid field and every composition (DBRef) field so mapping + reference resolution survive.
 * Field names are translated entity→DTO via {@code @FieldMappingRule}.
 */
@DisplayName("MongoDao — native field projection (.projection())")
class MongoDaoProjectionTest {

    /** name maps straight through; fullName (entity) maps to the DTO field "displayName". */
    public static class PersonDto {
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "name")
        private String name;
        @FieldMappingRule(sourceFieldAddress = "fullName")
        private String displayName;
        @FieldMappingRule(sourceFieldAddress = "email")
        private String email;
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @BeforeAll
    static void installReflection() {
        com.garganttua.core.bootstrap.dsl.Bootstrap.builder();
    }

    private MongoCollection<Document> collection;
    private FindIterable<Document> find;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private MongoDao daoWith(List<DtoComposition> compositions) {
        MongoDatabase database = mock(MongoDatabase.class);
        this.collection = mock(MongoCollection.class);
        when(database.getCollection("people")).thenReturn(this.collection);

        this.find = mock(FindIterable.class);
        when(this.collection.find(any(Bson.class))).thenReturn(this.find);
        MongoCursor<Document> cursor = mock(MongoCursor.class);
        when(cursor.hasNext()).thenReturn(false);
        when(this.find.iterator()).thenReturn(cursor);

        IDtoDefinition<Object> dtoDefinition = mock(IDtoDefinition.class);
        when(dtoDefinition.dtoClass()).thenReturn((IClass) IClass.getClass(PersonDto.class));
        when(dtoDefinition.uuid()).thenReturn(new ObjectAddress("uuid"));
        when(dtoDefinition.compositions()).thenReturn(compositions);
        IDomainDefinition domainDefinition = mock(IDomainDefinition.class);
        when(domainDefinition.dtoDefinitions()).thenReturn(List.of(dtoDefinition));

        MongoDao dao = new MongoDao(database, "people");
        dao.registerDomain(domainDefinition);
        return dao;
    }

    @BeforeEach
    void setUp() {
        // each test builds its own dao via daoWith(...)
    }

    private BsonDocument capturedProjection() {
        ArgumentCaptor<Bson> captor = ArgumentCaptor.forClass(Bson.class);
        verify(this.find).projection(captor.capture());
        return captor.getValue().toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
    }

    @Test
    @DisplayName("a projection includes the requested field AND force-includes uuid")
    void includesRequestedAndUuid() throws Exception {
        MongoDao dao = daoWith(List.of());

        dao.find(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(List.of("name")));

        BsonDocument proj = capturedProjection();
        assertTrue(proj.containsKey("name"), "the requested field must be included");
        assertEquals(1, proj.getInt32("name").getValue(), "include uses 1");
        assertTrue(proj.containsKey("uuid"), "uuid must be force-included so the entity maps + DBRefs resolve");
        assertTrue(!proj.containsKey("email") || proj.getInt32("email").getValue() == 0,
                "a non-requested field must not be included");
    }

    @Test
    @DisplayName("no projection → .projection() is never called (full document fetched)")
    void noProjectionNoPushdown() throws Exception {
        MongoDao dao = daoWith(List.of());

        dao.find(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        verify(this.find, never()).projection(any());
    }

    @Test
    @DisplayName("entity field names are translated to DTO field names via @FieldMappingRule")
    void translatesEntityToDtoFieldName() throws Exception {
        MongoDao dao = daoWith(List.of());

        // The entity field is "fullName"; the DTO/document field is "displayName".
        dao.find(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(List.of("fullName")));

        BsonDocument proj = capturedProjection();
        assertTrue(proj.containsKey("displayName"), "the entity field must be projected as its DTO field name");
        assertTrue(!proj.containsKey("fullName"), "the entity field name must not be sent to Mongo verbatim");
    }

    @Test
    @DisplayName("a composition (DBRef) field is force-included even when not requested")
    void forceIncludesCompositionFields() throws Exception {
        MongoDao dao = daoWith(List.of(new DtoComposition(new ObjectAddress("email"), "emails")));

        dao.find(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(List.of("name")));

        BsonDocument proj = capturedProjection();
        assertTrue(proj.containsKey("name"), "the requested field is included");
        assertTrue(proj.containsKey("email"), "the composition (DBRef) field must be force-included to resolve references");
        assertTrue(proj.containsKey("uuid"), "uuid is always force-included");
    }
}
