package com.garganttua.api.core;

import com.garganttua.objects.mapper.annotations.GGFieldMappingRule;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DummyDto {

    public DummyDto(String uuid, String tenantId, String id, String infos) {
        this.uuid = uuid;
        this.tenantId = tenantId;
        this.id = id;
        this.infos = infos;
    }

    @GGFieldMappingRule(sourceFieldAddress = "tenantId")
    private String tenantId;

    @GGFieldMappingRule(sourceFieldAddress = "uuid")
    private String uuid;

    @GGFieldMappingRule(sourceFieldAddress = "id")
    private String id;

    @GGFieldMappingRule(sourceFieldAddress = "infoFromDto1")
    private String infos;

}
