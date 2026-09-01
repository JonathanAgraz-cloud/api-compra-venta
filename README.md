# API Compra-Venta

Sistema en producción que detecta oportunidades de reventa en Facebook Marketplace y envía alertas en tiempo real por Telegram. Construido con Java + Spring Boot, desplegado en AWS (EC2 + RDS), con scraping automatizado, motor de análisis de precios y CI/CD.

![CI](https://github.com/JonathanAgraz-cloud/api-compra-venta/actions/workflows/ci.yml/badge.svg)

## ¿Qué hace?

La API revisa periódicamente artículos publicados en Facebook Marketplace (enfocado en tecnología: celulares, laptops, TVs, consolas, audífonos, monitores, etc.), estima su valor de reventa comparándolos contra artículos similares en venta, calcula la ganancia potencial y avisa por Telegram únicamente cuando encuentra una oportunidad que vale la pena.

- Analiza más de 40 categorías de tecnología de forma automática
- Corre cada hora dentro de un horario configurado, para no saturar el servidor ni parecer un patrón de bot
- Clasifica las oportunidades por nivel de ganancia (baja / media / alta) para priorizar cuáles revisar primero
- Manda también un aviso de "corrida sin oportunidades nuevas", para confirmar que el sistema sigue vivo

El cierre de cada trato lo hace una persona, no la API — el sistema solo detecta y notifica.

## Arquitectura

```
Scraper (Playwright)  →  Motor de análisis de precios  →  Alertas (Telegram API)
        ↓                          ↓
   MySQL (Amazon RDS)  ←  Scheduler (corridas cada hora)
        ↓
   Dashboard web (HTTP Basic Auth)
```

- **Backend:** Java 21 + Spring Boot (Web, Data JPA, Security, Validation)
- **Scraping:** Playwright, con manejo de sesión propio para Facebook
- **Base de datos:** MySQL en Amazon RDS, migraciones versionadas con Flyway
- **Alertas:** integración directa con la API de Telegram
- **Infraestructura:** AWS EC2 (t3.micro) + RDS, desplegado como servicio systemd
- **Testing:** JUnit 5 + Testcontainers, 130+ tests automatizados
- **CI/CD:** GitHub Actions, corre la suite completa en cada push

Diseño técnico completo en [`arquitectura-tecnica.md`](./arquitectura-tecnica.md).

## Estado

✅ En producción desde agosto 2026, con datos reales y alertas confirmadas llegando por Telegram.

| Módulo | Estado |
|---|---|
| Modelo de datos | ✅ Terminado |
| Scraper | ✅ Terminado y validado contra Facebook real |
| Motor de análisis de precios | ✅ Terminado |
| Alertas por Telegram | ✅ Terminado |
| Scheduler | ✅ Terminado |
| Dashboard web | ✅ Terminado (`GET /api/opportunities`, HTTP Basic Auth) |
| Despliegue AWS (EC2 + RDS) | ✅ Terminado |

## Correrlo en local

```bash
# 1. Copiar variables de entorno de ejemplo
cp .env.example .env
# 2. Levantar MySQL local con Docker
docker-compose up -d
# 3. Correr la aplicación
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```

---

Proyecto personal construido para resolver un problema real (reventa en Facebook Marketplace) y como pieza de portafolio.
