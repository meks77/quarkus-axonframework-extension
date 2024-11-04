package io.quarkiverse.axonframework.extension.runtime;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.dev.spi.HotReplacementContext;
import io.quarkus.dev.spi.HotReplacementSetup;

public class HotReplacement implements HotReplacementSetup {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final Logger LOG = LoggerFactory.getLogger(HotReplacement.class);

    @Override
    public void setupHotDeployment(HotReplacementContext context) {
        executor.scheduleWithFixedDelay(() -> scanForFileChanges(context), 1, 1, TimeUnit.SECONDS);
    }

    private void scanForFileChanges(HotReplacementContext context) {
        try {
            boolean hasChanges = context.doScan(false);
            LOG.info("Filechange scan finished with {}", hasChanges);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
