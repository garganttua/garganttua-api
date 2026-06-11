package com.garganttua.api.core.unit.context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.repository.Repository;
import com.garganttua.api.core.dto.DtoDefinition;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.IDtoContext;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.definition.IEntityDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.core.mapper.annotations.FieldMappingRule;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

@DisplayName("Repository Tests")
class RepositoryTest {

    @BeforeAll
    static void initReflection() {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build());
    }

    // --- Test POJOs ---

    public static class TestEntity {
        private String id;
        private String uuid; 
        private String tenantId;
        private String name;

        public TestEntity() {}

        public TestEntity(String id, String uuid, String tenantId, String name) {
            this.id = id;
            this.uuid = uuid;
            this.tenantId = tenantId;
            this.name = name;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class TestDto {
        @FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "tenantId")
        private String tenantId;
        @FieldMappingRule(sourceFieldAddress = "name")
        private String name;

        public TestDto() {}

        public TestDto(String id, String uuid, String tenantId, String name) {
            this.id = id;
            this.uuid = uuid;
            this.tenantId = tenantId;
            this.name = name;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // --- In-memory DAO stub ---

    public static class InMemoryDao implements IDao {
        private final List<Object> storage = new ArrayList<>();

        @Override
        public void registerDomain(IDomainDefinition domainDefinition) {}

        @Override
        public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
                throws ApiException {
            if (filter.isPresent()) {
                return filterStorage(filter.get());
            }
            return new ArrayList<>(storage);
        }

        @Override
        public Object save(Object object) throws ApiException {
            storage.add(object);
            return object;
        }

        @Override
        public void delete(Object object) throws ApiException {
            String deleteUuid = extractUuid(object);
            if (deleteUuid != null) {
                Iterator<Object> it = storage.iterator();
                while (it.hasNext()) {
                    Object stored = it.next();
                    String storedUuid = extractUuid(stored);
                    if (deleteUuid.equals(storedUuid)) {
                        it.remove();
                        return;
                    }
                }
            }
            storage.remove(object);
        }

        private String extractUuid(Object obj) {
            try {
                java.lang.reflect.Field field = obj.getClass().getDeclaredField("uuid");
                field.setAccessible(true);
                Object value = field.get(obj);
                return value != null ? value.toString() : null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public long count(IFilter filter) throws ApiException {
            return storage.size();
        }

        public List<Object> getStorage() {
            return storage;
        }

        private List<Object> filterStorage(IFilter filter) {
            // Simple implementation: handle $field filters with $eq
            if ("$field".equals(filter.getName()) && filter.getFilters() != null) {
                String fieldName = String.valueOf(filter.getValue());
                IFilter operator = filter.getFilters().get(0);
                if ("$eq".equals(operator.getName())) {
                    Object expectedValue = operator.getValue();
                    List<Object> result = new ArrayList<>();
                    for (Object obj : storage) {
                        try {
                            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
                            field.setAccessible(true);
                            Object actualValue = field.get(obj);
                            if (expectedValue != null && expectedValue.equals(actualValue)) {
                                result.add(obj);
                            }
                        } catch (Exception e) {
                            // skip
                        }
                    }
                    return result;
                }
            }
            return new ArrayList<>(storage);
        }
    }

    // --- Fields ---

    private InMemoryDao inMemoryDao;
    private IDtoContext<TestDto> dtoContext;
    private IDtoDefinition<TestDto> dtoDefinition;
    private Repository repository;

    private ObjectAddress uuidAddress;
    private ObjectAddress idAddress;
    private ObjectAddress tenantIdAddress;

    // --- Setup ---

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        uuidAddress = new ObjectAddress("uuid");
        idAddress = new ObjectAddress("id");
        tenantIdAddress = new ObjectAddress("tenantId");

        dtoDefinition = new DtoDefinition<>(IClass.getClass(TestDto.class), uuidAddress, idAddress, tenantIdAddress, java.util.List.of());

        inMemoryDao = new InMemoryDao();

        dtoContext = mock(IDtoContext.class);
        when(dtoContext.getDtoDefinition()).thenReturn(dtoDefinition);
        when(dtoContext.getDao()).thenReturn(inMemoryDao);

        // Delegate find/save/delete/count to InMemoryDao
        when(dtoContext.find(any(), any(), any())).thenAnswer(inv ->
                inMemoryDao.find(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2)));
        when(dtoContext.save(any())).thenAnswer(inv ->
                inMemoryDao.save(inv.getArgument(0)));
        doAnswer(inv -> {
            inMemoryDao.delete(inv.getArgument(0));
            return null;
        }).when(dtoContext).delete(any());
        when(dtoContext.count(any())).thenAnswer(inv ->
                inMemoryDao.count(inv.getArgument(0)));

        // getUuid extracts uuid from dto
        when(dtoContext.getUuid(any())).thenAnswer(inv -> {
            Object dto = inv.getArgument(0);
            if (dto instanceof TestDto testDto) {
                return testDto.getUuid();
            }
            return null;
        });

        repository = new Repository(List.of(dtoContext), IClass.getClass(TestEntity.class));
    }

    @SuppressWarnings("unchecked")
    private void setUpDomain() throws Exception {
        IDomain<TestEntity> domainContext = mock(IDomain.class);
        IEntityDefinition<TestEntity> entityDefinition = mock(IEntityDefinition.class);
        IDomainDefinition<TestEntity> domainDefinition = mock(IDomainDefinition.class);

        when(entityDefinition.uuid()).thenReturn(uuidAddress);
        when(entityDefinition.id()).thenReturn(idAddress);
        when(entityDefinition.tenantId()).thenReturn(tenantIdAddress);

        when(domainDefinition.entityDefinition()).thenReturn(entityDefinition);
        doReturn(List.of(dtoDefinition)).when(domainDefinition).dtoDefinitions();
        when(domainDefinition.domainName()).thenReturn("testentities");

        when(domainContext.getDomainDefinition()).thenReturn(domainDefinition);
        when(domainContext.getEntityDefinition()).thenReturn(entityDefinition);

        repository.setDomain(domainContext);
    }

    private TestDto createTestDto(String id, String uuid, String tenantId, String name) {
        return new TestDto(id, uuid, tenantId, name);
    }

    private TestEntity createTestEntity(String id, String uuid, String tenantId, String name) {
        return new TestEntity(id, uuid, tenantId, name);
    }

    // --- Test groups ---

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("rejects null dtoContexts")
        void rejectsNullDtoContexts() {
            assertThrows(NullPointerException.class,
                    () -> new Repository(null, IClass.getClass(TestEntity.class)));
        }

        @Test
        @DisplayName("rejects null entityClass")
        void rejectsNullEntityClass() {
            assertThrows(NullPointerException.class,
                    () -> new Repository(List.of(), null));
        }

        @Test
        @DisplayName("accepts empty dtoContexts list")
        void acceptsEmptyDtoContexts() {
            assertDoesNotThrow(() -> new Repository(List.of(), IClass.getClass(TestEntity.class)));
        }

        @Test
        @DisplayName("creates repository with valid arguments")
        void createsWithValidArgs() {
            assertDoesNotThrow(() -> new Repository(List.of(dtoContext), IClass.getClass(TestEntity.class)));
        }
    }

    @Nested
    @DisplayName("SetDomain")
    class SetDomainTests {

        @Test
        @DisplayName("rejects null domainContext")
        void rejectsNull() {
            assertThrows(NullPointerException.class,
                    () -> repository.setDomain(null));
        }

        @Test
        @DisplayName("accepts valid domainContext")
        @SuppressWarnings("unchecked")
        void acceptsValid() {
            IDomain<TestEntity> dc = mock(IDomain.class);
            assertDoesNotThrow(() -> repository.setDomain(dc));
        }
    }

    @Nested
    @DisplayName("Save")
    class SaveTests {

        @Test
        @DisplayName("saves entity and persists DTO in DAO")
        void savePersistsDto() throws ApiException {
            TestEntity entity = createTestEntity("1", "uuid-1", "tenant-1", "Alice");
            repository.save(entity);

            assertEquals(1, inMemoryDao.getStorage().size());
            Object saved = inMemoryDao.getStorage().get(0);
            assertTrue(saved instanceof TestDto);
            TestDto savedDto = (TestDto) saved;
            assertEquals("uuid-1", savedDto.getUuid());
            assertEquals("Alice", savedDto.getName());
        }

        @Test
        @DisplayName("rejects null entity")
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> repository.save(null));
        }

        @Test
        @DisplayName("saves multiple entities")
        void savesMultiple() throws ApiException {
            repository.save(createTestEntity("1", "uuid-1", "t1", "Alice"));
            repository.save(createTestEntity("2", "uuid-2", "t1", "Bob"));

            assertEquals(2, inMemoryDao.getStorage().size());
        }
    }

    @Nested
    @DisplayName("Delete")
    class DeleteTests {

        @Test
        @DisplayName("deletes entity from DAO")
        void deletesFromDao() throws ApiException {
            TestEntity entity = createTestEntity("1", "uuid-1", "t1", "Alice");
            repository.save(entity);
            assertEquals(1, inMemoryDao.getStorage().size());

            repository.delete(entity);
            assertEquals(0, inMemoryDao.getStorage().size());
        }

        @Test
        @DisplayName("rejects null entity")
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> repository.delete(null));
        }
    }

    @Nested
    @DisplayName("GetEntities")
    class GetEntitiesTests {

        @Test
        @DisplayName("returns empty list when nothing saved")
        void returnsEmptyWhenNothingSaved() throws ApiException {
            List<Object> entities = repository.getEntities(
                    Optional.empty(), Optional.empty(), Optional.empty());
            assertNotNull(entities);
            assertTrue(entities.isEmpty());
        }

        @Test
        @DisplayName("returns saved entities without filter")
        void returnsSavedEntities() throws ApiException {
            repository.save(createTestEntity("1", "uuid-1", "t1", "Alice"));
            repository.save(createTestEntity("2", "uuid-2", "t1", "Bob"));

            List<Object> entities = repository.getEntities(
                    Optional.empty(), Optional.empty(), Optional.empty());

            assertEquals(2, entities.size());
        }

        @Test
        @DisplayName("returned entities are of correct type")
        void returnedEntitiesAreCorrectType() throws ApiException {
            repository.save(createTestEntity("1", "uuid-1", "t1", "Alice"));

            List<Object> entities = repository.getEntities(
                    Optional.empty(), Optional.empty(), Optional.empty());

            assertEquals(1, entities.size());
            assertTrue(entities.get(0) instanceof TestEntity);
        }

        @Test
        @DisplayName("returned entities have correct field values")
        void returnedEntitiesHaveCorrectValues() throws ApiException {
            repository.save(createTestEntity("1", "uuid-1", "t1", "Alice"));

            List<Object> entities = repository.getEntities(
                    Optional.empty(), Optional.empty(), Optional.empty());

            TestEntity result = (TestEntity) entities.get(0);
            assertEquals("1", result.getId());
            assertEquals("uuid-1", result.getUuid());
            assertEquals("t1", result.getTenantId());
            assertEquals("Alice", result.getName());
        }
    }

    @Nested
    @DisplayName("DoesExist")
    class DoesExistTests {

        @Test
        @DisplayName("doesExist(uuid) returns true when entity exists")
        void existsByUuidTrue() throws ApiException {
            repository.save(createTestEntity("1", "uuid-1", "t1", "Alice"));

            assertTrue(repository.doesExist("uuid-1"));
        }

        @Test
        @DisplayName("doesExist(uuid) returns false when entity does not exist")
        void existsByUuidFalse() throws ApiException {
            assertFalse(repository.doesExist("uuid-nonexistent"));
        }

        @Test
        @DisplayName("doesExist(uuid) rejects null")
        void existsByUuidRejectsNull() {
            assertThrows(NullPointerException.class,
                    () -> repository.doesExist((String) null));
        }

        @Test
        @DisplayName("doesExist(entity) returns true when entity exists")
        void existsByEntityTrue() throws Exception {
            setUpDomain();
            TestEntity entity = createTestEntity("1", "uuid-1", "t1", "Alice");
            repository.save(entity);

            assertTrue(repository.doesExist(entity));
        }

        @Test
        @DisplayName("doesExist(entity) returns false when entity does not exist")
        void existsByEntityFalse() throws Exception {
            setUpDomain();
            TestEntity entity = createTestEntity("1", "uuid-nonexistent", "t1", "Ghost");

            assertFalse(repository.doesExist(entity));
        }

        @Test
        @DisplayName("doesExist(entity) rejects null")
        void existsByEntityRejectsNull() {
            assertThrows(NullPointerException.class,
                    () -> repository.doesExist((Object) null));
        }

        @Test
        @DisplayName("doesExist(entity) throws when domain context not set")
        void existsByEntityThrowsWithoutDomain() {
            TestEntity entity = createTestEntity("1", "uuid-1", "t1", "Alice");
            assertThrows(ApiException.class, () -> repository.doesExist(entity));
        }
    }

    @Nested
    @DisplayName("GetCount")
    class GetCountTests {

        @Test
        @DisplayName("returns count from first DtoContext")
        void returnsCount() throws ApiException {
            repository.save(createTestEntity("1", "uuid-1", "t1", "Alice"));
            repository.save(createTestEntity("2", "uuid-2", "t1", "Bob"));

            assertEquals(2, repository.getCount(null));
        }

        @Test
        @DisplayName("returns 0 when no DtoContexts")
        void returnsZeroWhenNoDtoContexts() throws ApiException {
            Repository emptyRepo = new Repository(List.of(), IClass.getClass(TestEntity.class));
            assertEquals(0, emptyRepo.getCount(null));
        }

        @Test
        @DisplayName("returns 0 when nothing saved")
        void returnsZeroWhenEmpty() throws ApiException {
            assertEquals(0, repository.getCount(null));
        }
    }

    @Nested
    @DisplayName("MergeMaps")
    class MergeMapsTests {

        @Test
        @DisplayName("merges maps correctly by key")
        void mergesCorrectly() throws ApiException {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("uuid-1", "dto1-from-map1");
            map1.put("uuid-2", "dto2-from-map1");

            Map<String, Object> map2 = new HashMap<>();
            map2.put("uuid-1", "dto1-from-map2");
            map2.put("uuid-3", "dto3-from-map2");

            Map<String, List<Object>> result = Repository.mergeMaps(List.of(map1, map2), false);

            assertEquals(3, result.size());
            assertEquals(2, result.get("uuid-1").size());
            assertEquals(1, result.get("uuid-2").size());
            assertEquals(1, result.get("uuid-3").size());
        }

        @Test
        @DisplayName("returns empty map for empty input")
        void returnsEmptyForEmptyInput() throws ApiException {
            Map<String, List<Object>> result = Repository.mergeMaps(List.of(), false);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("strict mode does not throw when all keys have same count")
        void strictModeNoThrowWhenConsistent() throws ApiException {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("uuid-1", "dto1");
            map1.put("uuid-2", "dto2");

            Map<String, Object> map2 = new HashMap<>();
            map2.put("uuid-1", "dto1b");
            map2.put("uuid-2", "dto2b");

            assertDoesNotThrow(() -> Repository.mergeMaps(List.of(map1, map2), true));
        }

        @Test
        @DisplayName("strict mode throws when keys have different counts")
        void strictModeThrowsWhenInconsistent() {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("uuid-1", "dto1");
            map1.put("uuid-2", "dto2");

            Map<String, Object> map2 = new HashMap<>();
            map2.put("uuid-1", "dto1b");
            // uuid-2 missing from map2 => inconsistent

            assertThrows(Exception.class,
                    () -> Repository.mergeMaps(List.of(map1, map2), true));
        }
    }

    // =========================================================================
    // Multi-DTO / Multi-DAO tests
    // =========================================================================

    // --- Multi-DTO POJOs ---

    public static class MultiEntity {
        private String id;
        private String uuid;
        private String tenantId;
        private String name;
        private String email;

        public MultiEntity() {}

        public MultiEntity(String id, String uuid, String tenantId, String name, String email) {
            this.id = id;
            this.uuid = uuid;
            this.tenantId = tenantId;
            this.name = name;
            this.email = email;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class DtoA {
        @FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "tenantId")
        private String tenantId;
        @FieldMappingRule(sourceFieldAddress = "name")
        private String name;

        public DtoA() {}

        public DtoA(String id, String uuid, String tenantId, String name) {
            this.id = id;
            this.uuid = uuid;
            this.tenantId = tenantId;
            this.name = name;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class DtoB {
        @FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "tenantId")
        private String tenantId;
        @FieldMappingRule(sourceFieldAddress = "email")
        private String email;

        public DtoB() {}

        public DtoB(String id, String uuid, String tenantId, String email) {
            this.id = id;
            this.uuid = uuid;
            this.tenantId = tenantId;
            this.email = email;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @Nested
    @DisplayName("Multi-DTO Repository")
    class MultiDtoRepository {

        private InMemoryDao daoA;
        private InMemoryDao daoB;
        private IDtoContext<DtoA> dtoContextA;
        private IDtoContext<DtoB> dtoContextB;
        private Repository multiRepo;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUp() {
            ObjectAddress uuidAddr = new ObjectAddress("uuid");
            ObjectAddress idAddr = new ObjectAddress("id");
            ObjectAddress tenantIdAddr = new ObjectAddress("tenantId");

            IDtoDefinition<DtoA> defA = new DtoDefinition<>(IClass.getClass(DtoA.class), uuidAddr, idAddr, tenantIdAddr, java.util.List.of());
            IDtoDefinition<DtoB> defB = new DtoDefinition<>(IClass.getClass(DtoB.class), uuidAddr, idAddr, tenantIdAddr, java.util.List.of());

            daoA = new InMemoryDao();
            daoB = new InMemoryDao();

            dtoContextA = mock(IDtoContext.class);
            when(dtoContextA.getDtoDefinition()).thenReturn(defA);
            try {
                when(dtoContextA.getDao()).thenReturn(daoA);
                when(dtoContextA.find(any(), any(), any())).thenAnswer(inv ->
                        daoA.find(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2)));
                when(dtoContextA.save(any())).thenAnswer(inv -> daoA.save(inv.getArgument(0)));
                doAnswer(inv -> { daoA.delete(inv.getArgument(0)); return null; }).when(dtoContextA).delete(any());
                when(dtoContextA.count(any())).thenAnswer(inv -> daoA.count(inv.getArgument(0)));
                when(dtoContextA.getUuid(any())).thenAnswer(inv -> {
                    Object dto = inv.getArgument(0);
                    if (dto instanceof DtoA a) return a.getUuid();
                    return null;
                });
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

            dtoContextB = mock(IDtoContext.class);
            when(dtoContextB.getDtoDefinition()).thenReturn(defB);
            try {
                when(dtoContextB.getDao()).thenReturn(daoB);
                when(dtoContextB.find(any(), any(), any())).thenAnswer(inv ->
                        daoB.find(inv.getArgument(0), inv.getArgument(1), inv.getArgument(2)));
                when(dtoContextB.save(any())).thenAnswer(inv -> daoB.save(inv.getArgument(0)));
                doAnswer(inv -> { daoB.delete(inv.getArgument(0)); return null; }).when(dtoContextB).delete(any());
                when(dtoContextB.count(any())).thenAnswer(inv -> daoB.count(inv.getArgument(0)));
                when(dtoContextB.getUuid(any())).thenAnswer(inv -> {
                    Object dto = inv.getArgument(0);
                    if (dto instanceof DtoB b) return b.getUuid();
                    return null;
                });
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

            multiRepo = new Repository(List.of(dtoContextA, dtoContextB), IClass.getClass(MultiEntity.class));
        }

        private MultiEntity createMultiEntity(String id, String uuid, String tenantId, String name, String email) {
            return new MultiEntity(id, uuid, tenantId, name, email);
        }

        @Nested
        @DisplayName("Save")
        class MultiSaveTests {

            @Test
            @DisplayName("save persists to both DAOs")
            void savePersistsToBothDaos() throws ApiException {
                multiRepo.save(createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com"));

                assertEquals(1, daoA.getStorage().size());
                assertEquals(1, daoB.getStorage().size());
            }

            @Test
            @DisplayName("DtoA receives name field")
            void dtoAReceivesName() throws ApiException {
                multiRepo.save(createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com"));

                DtoA savedA = (DtoA) daoA.getStorage().get(0);
                assertEquals("Alice", savedA.getName());
                assertEquals("uuid-1", savedA.getUuid());
            }

            @Test
            @DisplayName("DtoB receives email field")
            void dtoBReceivesEmail() throws ApiException {
                multiRepo.save(createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com"));

                DtoB savedB = (DtoB) daoB.getStorage().get(0);
                assertEquals("alice@test.com", savedB.getEmail());
                assertEquals("uuid-1", savedB.getUuid());
            }

            @Test
            @DisplayName("save multiple entities persists to both DAOs")
            void saveMultiplePersistsToBothDaos() throws ApiException {
                multiRepo.save(createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com"));
                multiRepo.save(createMultiEntity("2", "uuid-2", "t1", "Bob", "bob@test.com"));

                assertEquals(2, daoA.getStorage().size());
                assertEquals(2, daoB.getStorage().size());
            }
        }

        @Nested
        @DisplayName("Delete")
        class MultiDeleteTests {

            @Test
            @DisplayName("delete removes from both DAOs")
            void deleteRemovesFromBothDaos() throws ApiException {
                MultiEntity entity = createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com");
                multiRepo.save(entity);

                assertEquals(1, daoA.getStorage().size());
                assertEquals(1, daoB.getStorage().size());

                multiRepo.delete(entity);

                assertEquals(0, daoA.getStorage().size());
                assertEquals(0, daoB.getStorage().size());
            }
        }

        @Nested
        @DisplayName("GetEntities")
        class MultiGetEntitiesTests {

            @Test
            @DisplayName("returns entities merged from both DTOs")
            void returnsMergedEntities() throws ApiException {
                multiRepo.save(createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com"));

                List<Object> entities = multiRepo.getEntities(
                        Optional.empty(), Optional.empty(), Optional.empty());

                assertEquals(1, entities.size());
                assertTrue(entities.get(0) instanceof MultiEntity);
            }

            @Test
            @DisplayName("merged entity has fields from both DTOs")
            void mergedEntityHasFieldsFromBothDtos() throws ApiException {
                multiRepo.save(createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com"));

                List<Object> entities = multiRepo.getEntities(
                        Optional.empty(), Optional.empty(), Optional.empty());

                MultiEntity result = (MultiEntity) entities.get(0);
                assertEquals("Alice", result.getName());
                assertEquals("alice@test.com", result.getEmail());
                assertEquals("uuid-1", result.getUuid());
                assertEquals("t1", result.getTenantId());
            }

            @Test
            @DisplayName("returns multiple merged entities")
            void returnsMultipleMergedEntities() throws ApiException {
                multiRepo.save(createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com"));
                multiRepo.save(createMultiEntity("2", "uuid-2", "t1", "Bob", "bob@test.com"));

                List<Object> entities = multiRepo.getEntities(
                        Optional.empty(), Optional.empty(), Optional.empty());

                assertEquals(2, entities.size());
            }

            @Test
            @DisplayName("each merged entity has correct fields")
            void eachMergedEntityHasCorrectFields() throws ApiException {
                multiRepo.save(createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com"));
                multiRepo.save(createMultiEntity("2", "uuid-2", "t1", "Bob", "bob@test.com"));

                List<Object> entities = multiRepo.getEntities(
                        Optional.empty(), Optional.empty(), Optional.empty());

                List<MultiEntity> sorted = entities.stream()
                        .map(e -> (MultiEntity) e)
                        .sorted((a, b) -> a.getId().compareTo(b.getId()))
                        .toList();

                assertEquals("Alice", sorted.get(0).getName());
                assertEquals("alice@test.com", sorted.get(0).getEmail());
                assertEquals("Bob", sorted.get(1).getName());
                assertEquals("bob@test.com", sorted.get(1).getEmail());
            }

            @Test
            @DisplayName("returns empty list when no entities saved")
            void returnsEmptyWhenNothingSaved() throws ApiException {
                List<Object> entities = multiRepo.getEntities(
                        Optional.empty(), Optional.empty(), Optional.empty());

                assertTrue(entities.isEmpty());
            }
        }

        @Nested
        @DisplayName("GetCount")
        class MultiGetCountTests {

            @Test
            @DisplayName("count uses first DTO context only")
            void countUsesFirstDtoContext() throws ApiException {
                multiRepo.save(createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com"));
                multiRepo.save(createMultiEntity("2", "uuid-2", "t1", "Bob", "bob@test.com"));

                assertEquals(2, multiRepo.getCount(null));
            }
        }

        @Nested
        @DisplayName("DoesExist")
        class MultiDoesExistTests {

            @Test
            @DisplayName("doesExist returns true when entity exists")
            void returnsTrueWhenExists() throws ApiException {
                multiRepo.save(createMultiEntity("1", "uuid-1", "t1", "Alice", "alice@test.com"));

                assertTrue(multiRepo.doesExist("uuid-1"));
            }

            @Test
            @DisplayName("doesExist returns false when entity does not exist")
            void returnsFalseWhenNotExists() throws ApiException {
                assertFalse(multiRepo.doesExist("uuid-nonexistent"));
            }
        }
    }
}
