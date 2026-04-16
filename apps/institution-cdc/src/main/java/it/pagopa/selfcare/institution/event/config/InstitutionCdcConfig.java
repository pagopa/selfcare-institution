package it.pagopa.selfcare.institution.event.config;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class InstitutionCdcConfig {

    @ApplicationScoped
    public TableClient tableClient(@ConfigProperty(name = "institution-cdc.storage.connection-string") String storageConnectionString,
                                   @ConfigProperty(name = "institution-cdc.table.name") String tableName) {
        return new TableClientBuilder()
                .connectionString(storageConnectionString)
                .tableName(tableName)
                .buildClient();
    }

    @ApplicationScoped
    public ServiceBusSenderClient serviceBusSenderClient(@ConfigProperty(name = "institution-cdc.servicebus.connection-string") String senderBusConnectionString,
                                                         @ConfigProperty(name = "institution-cdc.servicebus.topic") String senderBusTopicName) {
        return new ServiceBusClientBuilder()
                .connectionString(senderBusConnectionString)
                .sender()
                .topicName(senderBusTopicName)
                .buildClient();
    }

}

