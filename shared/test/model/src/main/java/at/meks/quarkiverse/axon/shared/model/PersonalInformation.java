package at.meks.quarkiverse.axon.shared.model;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateLifecycle;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PersonalInformation {

    @CommandHandler
    void handle(Api.AddPersonalInformationCommand command) {
        AggregateLifecycle.apply(new Api.PersonalInformationAddedEvent(command.id(), command.personName()));
    }

}
