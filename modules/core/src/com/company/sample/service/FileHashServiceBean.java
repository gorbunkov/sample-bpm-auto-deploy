package com.company.sample.service;

import com.company.sample.entity.FileHash;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Metadata;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;

@Service(FileHashService.NAME)
public class FileHashServiceBean implements FileHashService {
    @Inject
    protected DataManager dataManager;

    @Inject
    protected Metadata metadata;

    @Override
    @Nullable
    public String loadHash(String filePath) {
        FileHash fileHash = findFileHashEntityByFilePath(filePath);
        return fileHash != null ? fileHash.getHash() : null;
    }

    @Override
    public void saveHash(String filePath, String hash) {
        FileHash fileHashEntity = findFileHashEntityByFilePath(filePath);
        if (fileHashEntity == null) {
            fileHashEntity = metadata.create(FileHash.class);
            fileHashEntity.setFilePath(filePath);
        }
        fileHashEntity.setHash(hash);
        dataManager.commit(fileHashEntity);
    }

    private FileHash findFileHashEntityByFilePath(String filePath) {
        return dataManager.load(FileHash.class)
                .query("select e from sample$FileHash e where e.filePath = :filePath")
                .parameter("filePath", filePath)
                .optional()
                .orElse(null);
    }
}