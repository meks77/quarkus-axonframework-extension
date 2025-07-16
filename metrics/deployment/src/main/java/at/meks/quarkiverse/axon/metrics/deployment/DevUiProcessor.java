package at.meks.quarkiverse.axon.metrics.deployment;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;

public class DevUiProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    CardPageBuildItem create() {
        CardPageBuildItem card = new CardPageBuildItem();
        card.setLogo("AxonFramework-DarkIcon.svg", "AxonFramework-DarkIcon.svg");
        card.addLibraryVersion("org.axonframework", "axon-micrometer", "Axon Micrometer",
                "https://www.axoniq.io/products/axon-framework");
        return card;
    }

}
