package com.jonypacheco.marketplace.scheduler.runner;

import com.jonypacheco.marketplace.scheduler.OpportunityPipelineScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Dispara una corrida real del pipeline completo (scraper -> analisis ->
 * alertas) al arrancar la app, SOLO si el perfil {@code pipeline-manual}
 * esta activo explicitamente (ej. {@code SPRING_PROFILES_ACTIVE=dev,pipeline-manual}).
 * Llama directo a {@link OpportunityPipelineScheduler#runOpportunityPipeline()},
 * sin pasar por el chequeo de {@code scheduler.enabled} -- pensado para que
 * Jony pueda probar el pipeline completo a mano antes de activar el cron en
 * produccion. Mismo patron que {@code ManualScraperRunner}.
 */
@Component
@Profile("pipeline-manual")
public class PipelineManualRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PipelineManualRunner.class);

    private final OpportunityPipelineScheduler scheduler;

    public PipelineManualRunner(OpportunityPipelineScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run(String... args) {
        log.info("Iniciando corrida manual del pipeline completo (perfil pipeline-manual activo)...");
        scheduler.runOpportunityPipeline();
        log.info("Corrida manual del pipeline terminada.");
    }
}
