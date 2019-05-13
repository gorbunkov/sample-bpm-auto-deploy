package com.company.sample.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Table(name = "SAMPLE_FILE_HASH")
@Entity(name = "sample$FileHash")
public class FileHash extends StandardEntity {
    @NotNull
    @Column(name = "FILE_PATH", nullable = false, length = 2000)
    protected String filePath;

    @NotNull
    @Column(name = "HASH", nullable = false)
    protected String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}