# BPM Model Auto Deploy Sample

## Overview

The sample demonstrates how BPM process models can be automatically deployed on application startup.

Process models that must be deployed are stored in the directory defined by the _sample.processModelsDir_ application property. The property defined in the [app.properties](modules/core/src/com/company/sample/app.properties) file. In this sample the directory is [modules/core/web/WEB-INF/resources/processmodels](modules/core/web/WEB-INF/resources/processmodels). The [StartupInitialization.java](modules/core/src/com/company/sample/core/StartupInitialization.java) listens for the `AppContextStartedEvent` and when it occurs the `deployAllModels()` method of the [ProcModelDeployer.java](modules/core/src/com/company/sample/core/ProcModelDeployer.java) is invoked. 

`ProcModelDeployer` scans the directory with process models and for each file checks whether the file was already deployed. It saves file hash to the database and in case when there is no hash for the file yet or file hash is different, the new model version is deployed. 

The file hash is stored in the [FileHash.java](modules/global/src/com/company/sample/entity/FileHash.java) entity.

The service that works with these entities is [FileHashService.java](modules/global/src/com/company/sample/service/FileHashService.java) 