package at.meks.quarkiverse.axon.eventstore.jpa.deployment;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;

public class DevUiProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    CardPageBuildItem create() {
        CardPageBuildItem card = new CardPageBuildItem();
        card.setLogo("AxonFramework-DarkIcon.svg", "AxonFramework-DarkIcon.svg");
        return card;
    }

}
