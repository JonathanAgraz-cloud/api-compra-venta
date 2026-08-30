package com.jonypacheco.marketplace.api;

import com.jonypacheco.marketplace.persistence.domain.entity.AlertSent;
import com.jonypacheco.marketplace.persistence.domain.entity.Listing;
import com.jonypacheco.marketplace.persistence.domain.enums.GananciaClasificacion;
import com.jonypacheco.marketplace.persistence.domain.enums.ZonaMerida;
import com.jonypacheco.marketplace.persistence.repository.AlertSentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test del controlador: verifica el mapeo a JSON/orden y que Spring
 * Security exige autenticacion. La configuracion real de usuario
 * ({@code SecurityConfig}) no se carga aqui -- {@code @WebMvcTest} usa el
 * autoconfigurado por defecto de Spring Boot (HTTP Basic con credenciales
 * generadas), suficiente para probar "sin sesion -> 401" y "con sesion ->
 * 200 + JSON correcto".
 */
@WebMvcTest(OpportunityController.class)
class OpportunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertSentRepository alertSentRepository;

    @Test
    void sinAutenticacion_regresa401() throws Exception {
        mockMvc.perform(get("/api/opportunities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void listarOportunidades_regresaJsonMapeadoDesdeAlertSentYListing() throws Exception {
        Listing listing = new Listing("fb-1", "Refrigerador", new BigDecimal("3000"),
                ZonaMerida.ALTABRISA, "https://facebook.com/marketplace/item/1");
        AlertSent alerta = new AlertSent(listing, new BigDecimal("3000"), new BigDecimal("4500"),
                new BigDecimal("2000"), GananciaClasificacion.ALTA, 5);

        when(alertSentRepository.findAllByOrderByGananciaEstimadaDesc()).thenReturn(List.of(alerta));

        mockMvc.perform(get("/api/opportunities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tituloAnuncio").value("Refrigerador"))
                .andExpect(jsonPath("$[0].url").value("https://facebook.com/marketplace/item/1"))
                .andExpect(jsonPath("$[0].zona").value("ALTABRISA"))
                .andExpect(jsonPath("$[0].clasificacion").value("ALTA"))
                .andExpect(jsonPath("$[0].gananciaEstimada").value(2000));
    }

    @Test
    @WithMockUser
    void listarOportunidades_sinAlertas_regresaListaVacia() throws Exception {
        when(alertSentRepository.findAllByOrderByGananciaEstimadaDesc()).thenReturn(List.of());

        mockMvc.perform(get("/api/opportunities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
