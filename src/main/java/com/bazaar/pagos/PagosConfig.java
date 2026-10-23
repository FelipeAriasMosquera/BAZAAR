package com.bazaar.pagos;

import com.bazaar.pagos.dominio.ConsultarPagoUseCase;
import com.bazaar.pagos.dominio.RegistrarPagoUseCase;
import com.bazaar.pagos.dominio.ReembolsarPagoUseCase;
import com.bazaar.pagos.puertos.OrdenPort;
import com.bazaar.pagos.puertos.PagoRepositoryPort;
import com.bazaar.pagos.puertos.PasarelaPagoPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Aquí (y solo aquí) se conecta el dominio con Spring. Los casos de uso son clases
 * normales sin @Service; esta clase los registra como beans y Spring les inyecta la
 * implementación de cada puerto que haya en el proyecto.
 */
@Configuration
public class PagosConfig {

    @Bean
    public RegistrarPagoUseCase registrarPagoUseCase(PagoRepositoryPort pagos,
                                                     PasarelaPagoPort pasarela,
                                                     OrdenPort ordenes) {
        return new RegistrarPagoUseCase(pagos, pasarela, ordenes);
    }

    @Bean
    public ConsultarPagoUseCase consultarPagoUseCase(PagoRepositoryPort pagos) {
        return new ConsultarPagoUseCase(pagos);
    }

    @Bean
    public ReembolsarPagoUseCase reembolsarPagoUseCase(PagoRepositoryPort pagos, PasarelaPagoPort pasarela) {
        return new ReembolsarPagoUseCase(pagos, pasarela);
    }
}
