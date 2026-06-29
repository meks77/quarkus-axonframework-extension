package at.meks.quarkiverse.axon.shared.model;

import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PersonalInformation {

    @CommandHandler
    void handle(Api.AddPersonalInformationCommand command, EventAppender eventAppender) {
        eventAppender.append(new Api.PersonalInformationAddedEvent(command.id(), command.personName()));
    }

}
