package com.garganttua.api.core.runtime.executors;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.garganttua.api.core.context.execution.ExecutionVariable;
import com.garganttua.api.core.old.entity.tools.EntityHelper;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.context.IApplicationContext;
import com.garganttua.api.spec.context.IExecutionContext;
import com.garganttua.api.spec.context.StandardParameter;
import com.garganttua.api.spec.service.ReadOutputMode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProduceReadAllResult extends RuntimeExecutor {

    @Override
    protected void execute(IApplicationContext aCtxt, IExecutionContext eCtxt) throws CoreException {

        Optional<?> repoReturnedOpt = eCtxt.getExecutionVariable(ExecutionVariable.REPOSITORY_RETURN.toString());
        Optional<ReadOutputMode> modeOpt = eCtxt.getExecutionVariable(StandardParameter.MODE.toString(),
                ReadOutputMode.class);

        ReadOutputMode mode = ReadOutputMode.full;
        if (modeOpt.isPresent())
            mode = modeOpt.get();

        if (repoReturnedOpt.isPresent()) {
            Object temp = repoReturnedOpt.get();
            if (Collection.class.isAssignableFrom(temp.getClass())) {
                List<Object> entities = (List<Object>) temp;
                List<Object> finalEntityList;

                switch (mode) {
                    case id:
                        finalEntityList = entities.stream().map(entity -> {
                            try {
                                return EntityHelper.getId(entity);
                            } catch (CoreException e) {
                                if (log.isDebugEnabled()) {
                                    log.warn("Error : ", e);
                                }
                            }
                            return null;
                        }).collect(Collectors.toList());
                        break;
                    case uuid:
                        finalEntityList = entities.stream().map(entity -> {
                            try {
                                return EntityHelper.getUuid(entity);
                            } catch (CoreException e) {
                                if (log.isDebugEnabled()) {
                                    log.warn("Error : ", e);
                                }
                            }
                            return null;
                        }).collect(Collectors.toList());
                        break;
                    default:
                    case full:
                        finalEntityList = entities;
                        break;
                }

                eCtxt.setExecutionVariable(ExecutionVariable.OUTPUT_DATA.toString(), finalEntityList);
            }
        }

        /*
         * if (pageable != null && pageable.getPageSize() != 0) {
         * long totalCount = 0;// this.factory.countEntities(caller, filter,
         * customParameters);
         * Page page = new Page(totalCount, ((List<Object>) finalEntityList));
         * event.setOut(page);
         * } else {
         * event.setOut(finalEntityList);
         * }
         */
    }

}
