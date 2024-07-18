package it.pagopa.selfcare.institution;

import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.institution.service.InstitutionSendMailScheduledService;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

@QuarkusMainTest
@TestProfile(ApplicationTest.MyTestProfile.class)
public class ApplicationTest {

    @Test
    @Launch(value = {})
    public void testLaunchCommand(QuarkusMainLauncher launcher) {
        LaunchResult result = launcher.launch();
        Assertions.assertEquals(1, result.exitCode());
    }

    public static class MyTestProfile implements QuarkusTestProfile {

        @Override
        public Set<Class<?>> getEnabledAlternatives() {
            return Set.of(MockedInstitutionSendMailScheduledServiceImpl.class);
        }
    }

    @Alternative
    @Singleton
    public static class MockedInstitutionSendMailScheduledServiceImpl implements InstitutionSendMailScheduledService {

        @Override
        public Uni<Void> retrieveInstitutionFromPecNotificationAndSendMail(String productId) {
            return Uni.createFrom().voidItem();
        }
    }

}
