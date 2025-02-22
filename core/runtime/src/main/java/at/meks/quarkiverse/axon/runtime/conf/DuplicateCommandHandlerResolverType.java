package at.meks.quarkiverse.axon.runtime.conf;

public enum DuplicateCommandHandlerResolverType {
    logAndOverride,
    silentOverride,
    rejectDuplicates
}
