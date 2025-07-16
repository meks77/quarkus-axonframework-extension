package at.meks.quarkiverse.axon.server.deployment;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;

public class DevUiProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    CardPageBuildItem create() {
        CardPageBuildItem card = new CardPageBuildItem();
        card.setLogo("AxonServer-DarkIcon.png.webp", "AxonServer-DarkIcon.png.webp");
        card.addLibraryVersion("org.axonframework", "axon-server-connector", "Axon Server Connector",
                "https://www.axoniq.io/products/axon-server");
        return card;
    }

}
