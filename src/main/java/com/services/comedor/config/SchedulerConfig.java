package com.services.comedor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración del Scheduler para tareas automáticas
 * Habilita la ejecución de jobs programados en el sistema
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Esta clase habilita el scheduler en Spring Boot
    // Los jobs se ejecutan según las expresiones cron definidas en cada método
}