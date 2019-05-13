package com.company.sample.core;

import com.company.sample.service.FileHashService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.haulmont.bpm.core.ProcessRepositoryManager;
import com.haulmont.bpm.entity.ProcDefinition;
import com.haulmont.bpm.entity.ProcModel;
import com.haulmont.bpm.service.ModelService;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.encryption.Sha1EncryptionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * The class automatically deploys processes for models located under the "web/WEB-INF/resources/processmodels" directory. The
 * class is invoked on application startup.
 */
@Component("sample_ProcModelDeployer")
public class ProcModelDeployer {

    @Inject
    protected DataManager dataManager;

    @Inject
    protected ModelService modelService;

    @Inject
    protected Metadata metadata;

    @Inject
    protected ProcessRepositoryManager processRepositoryManager;

    @Inject
    protected FileHashService fileHashService;

    @Inject
    private Sha1EncryptionModule encryptionModule;

    private static final Logger log = LoggerFactory.getLogger(ProcModelDeployer.class);

    public void deployAllModels() {
        String modelsDir = AppContext.getProperty("sample.processModelsDir");

        if (Strings.isNullOrEmpty(modelsDir)) {
            log.error("Models directory specified by the sample.processModelsDir property not found");
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(modelsDir))) {
            for (Path file : stream) {
                String modelJson = new String(Files.readAllBytes(file));
                String currentHash = encryptionModule.getPlainHash(modelJson);
                String filePath = file.toString();
                String prevHash = fileHashService.loadHash(filePath);
                if (currentHash.equals(prevHash)) continue;
                String modelName = getModelName(modelJson);
                if (modelName == null) {
                    log.error("Cannot find the modelName in the {}", file.getFileName().toString());
                    continue;
                }
                ProcModel existingProcModel = findProcModelByName(modelName);
                if (existingProcModel == null) {
                    ProcModel newModel = createModel(modelJson, modelName);
                    deployModel(newModel);
                } else {
                    updateModel(modelJson, existingProcModel);
                    deployModel(existingProcModel);
                }
                fileHashService.saveHash(filePath, currentHash);
                log.info("Process model {} deployed", modelName);
            }
        } catch (IOException | DirectoryIteratorException e) {
            log.error("Error on deploying the model", e);
        }
    }

    @Nullable
    private String getModelName(String modelJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode modelJsonNode = objectMapper.readTree(modelJson);
        JsonNode propertiesNode = modelJsonNode.get("properties");
        if (propertiesNode == null) {
            log.error("Invalid JSON document");
            return null;
        }
        JsonNode nameNode = propertiesNode.get("name");
        if (nameNode == null) {
            log.error("Invalid JSON document");
            return null;
        }
        return nameNode.asText();
    }

    @Nullable
    private ProcModel findProcModelByName(String modelName) {
        View view = new View(ProcModel.class)
                .addProperty("name")
                .addProperty("actModelId");
        LoadContext ctx = LoadContext.create(ProcModel.class).setView(view);
        ctx.setQueryString("select m from bpm$ProcModel m where m.name = :name")
                .setParameter("name", modelName);
        return dataManager.<ProcModel>load(ctx);
    }

    private ProcModel createModel(String modelJson, String modelName) {
        String actModelId = modelService.createModel(modelName);
        modelService.updateModel(actModelId, modelName, "", modelJson, "");
        ProcModel procModel = metadata.create(ProcModel.class);
        procModel.setName(modelName);
        procModel.setActModelId(actModelId);
        return dataManager.commit(procModel);
    }

    private void updateModel(String modelJson, ProcModel model) {
        modelService.updateModel(model.getActModelId(), model.getName(), "", modelJson, "");
    }

    private List<ProcDefinition> findProcDefinitionsByModel(ProcModel model) {
        LoadContext<ProcDefinition> ctx = LoadContext.create(ProcDefinition.class);
        ctx.setQueryString("select pd from bpm$ProcDefinition pd where pd.model.id = :model order by pd.name, pd.deploymentDate desc")
                .setParameter("model", model);
        return dataManager.loadList(ctx);
    }

    private void deployModel(ProcModel procModel) {
        List<ProcDefinition> procDefinitionsByModel = findProcDefinitionsByModel(procModel);
        ProcDefinition procDefinition = procDefinitionsByModel.isEmpty() ? null : procDefinitionsByModel.get(0);
        String processXml = processRepositoryManager.convertModelToProcessXml(procModel.getActModelId());
        processRepositoryManager.deployProcessFromXml(processXml, procDefinition, procModel);
    }
}
