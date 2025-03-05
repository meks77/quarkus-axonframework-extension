package at.meks.quarkiverse.axon.shared.model;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateLifecycle;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PersonalInformation {

    String personName;

    @CommandHandler
    void handle(Api.AddPersonalInformationCommand command) {
        AggregateLifecycle.apply(new Api.PersonalInformationAddedEvent(command.id(), command.personName()));
    }

    @EventSourcingHandler
    void handle(Api.PersonalInformationAddedEvent event) {
        this.personName = event.personName();
    }

}
