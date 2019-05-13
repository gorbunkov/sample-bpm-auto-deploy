-- begin SAMPLE_FILE_HASH
create table SAMPLE_FILE_HASH (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    FILE_PATH varchar(2000) not null,
    HASH varchar(255) not null,
    --
    primary key (ID)
)^
-- end SAMPLE_FILE_HASH
