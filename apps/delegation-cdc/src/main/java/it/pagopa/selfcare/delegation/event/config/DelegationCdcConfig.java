package it.pagopa.selfcare.delegation.event.config;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DelegationCdcConfig {

    @ApplicationScoped
    public TelemetryClient telemetryClient(@ConfigProperty(name = "delegation-cdc.appinsights.connection-string") String appInsightsConnectionString) {
        TelemetryConfiguration telemetryConfiguration = TelemetryConfiguration.createDefault();
        telemetryConfiguration.setConnectionString(appInsightsConnectionString);
        return new TelemetryClient(telemetryConfiguration);
    }

    @ApplicationScoped
    public TableClient tableClient(@ConfigProperty(name = "delegation-cdc.storage.connection-string") String storageConnectionString,
                                   @ConfigProperty(name = "delegation-cdc.table.name") String tableName){
        return new TableClientBuilder()
                .connectionString(storageConnectionString)
                .tableName(tableName)
                .buildClient();
    }

}
