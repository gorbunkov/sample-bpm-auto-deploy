package com.company.sample.service;

import javax.annotation.Nullable;

/**
 * Service is used for storing values of file hashes. It is required by the data initialization that is performed on application start. Based on the
 * hash value we determine whether some file with a data has been changed and decide whether this file should be processed for import.
 */
public interface FileHashService {
    String NAME = "sample_FileHashService";

    @Nullable
    String loadHash(String filePath);

    void saveHash(String filePath, String hash);
}