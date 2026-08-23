# Arquitectura técnica — API de detección de oportunidades de reventa (Facebook Marketplace)

Documento de referencia para el desarrollo con Claude Code. Resume decisiones de arquitectura, stack, reglas de negocio, prácticas profesionales y seguridad, para no perder el rumbo durante el desarrollo y para servir como evidencia de buenas prácticas en el portafolio.

## 1. Objetivo del proyecto

Detectar artículos en Facebook Marketplace que representan una oportunidad real de reventa, priorizando zonas de Yucatán con mayor poder adquisitivo (donde la gente vende cosas buenas baratas por no necesitar el dinero), calcular la ganancia estimada en pesos, clasificarla por importancia, y avisar por Telegram para que Jony cierre el trato manualmente. La API no contacta vendedores ni automatiza transacciones — solo detecta, analiza y alerta.

## 2. Stack confirmado

- Lenguaje/framework: Java + Spring Boot
- Base de datos: MySQL en Amazon RDS (free tier)
- Infraestructura: AWS EC2 (la instancia t3.micro ya existente, donde también corre OpenClaw)
- Agente conversacional: OpenClaw (Telegram + gpt-5.6-luna) — ya configurado y funcionando, se mantiene para experimentación/dashboard, pero **no** es responsable de mandar las alertas de oportunidades
- Notificaciones: Telegram Bot API, llamada directamente desde Spring Boot (no depende de OpenClaw)

## 3. Reglas de negocio: cálculo de ganancia y clasificación

La ganancia se calcula en pesos (no en porcentaje de descuento):

1. Se toman mínimo 5 comparables confiables (artículos similares actualmente en venta).
2. Precio de reventa estimado = precio del comparable más barato **menos 5%** (para poder vender rápido, un poco por debajo de la competencia).
3. Ganancia estimada = precio de reventa estimado − precio de compra del artículo − costos de reventa (15% por transporte/tiempo).
4. Si la ganancia estimada es menor a $500 pesos, se descarta — no genera alerta.
5. Clasificación de las oportunidades que sí generan alerta:
   - **Alta ganancia**: $2,000 pesos o más — máxima prioridad.
   - **Media ganancia**: $1,000 a $1,999 pesos.
   - **Baja ganancia**: $500 a $999 pesos.

Priorización geográfica: el scraper prioriza anuncios de zonas del norte de Mérida asociadas a mayor poder adquisitivo — Altabrisa, Temozón Norte, Cholul, Dzityá, Yucatán Country Club y Francisco de Montejo — además de la cobertura general definida.

Reglas operativas que se mantienen: horario 08:00-23:00 América/Mérida, canal de notificación Telegram.

Frecuencia de búsqueda: el scraper corre cada 1 hora dentro del horario de operación. No se hace más seguido por dos razones — el scraping abre un navegador completo (Playwright/Chromium) cada vez, lo cual consume CPU/RAM notable en el t3.micro; y un patrón de acceso muy frecuente se parece más a tráfico de bot ante Facebook, aumentando el riesgo de baneo de la cuenta secundaria. Nota: esta lógica no usa el modelo de IA (OpenAI) en absoluto, es cálculo puro en Java — el límite de frecuencia no es por costo de IA, es por recursos del servidor y riesgo de detección.

## 4. Por qué RDS en vez de MySQL local en el EC2

La instancia t3.micro tiene 912MB de RAM reales y ya necesitó 2GB de swap para correr OpenClaw (Node) de forma estable. Meter también un motor de MySQL completo ahí arriesga quedarnos cortos de memoria cuando además corra la app de Spring Boot (JVM). RDS free tier (db.t3.micro o db.t4g.micro, 750 horas/mes, 20GB de almacenamiento, 12 meses gratis) resuelve esto sacando la base de datos del EC2 por completo.

Sobre qué pesa más para trabajo: usar RDS no reemplaza saber MySQL — el motor es el mismo, las queries, el diseño de esquema, los índices, todo eso lo sigues escribiendo igual. RDS es solo *dónde* vive ese MySQL. Usarlo además te da evidencia de que sabes separar responsabilidades y trabajar con servicios administrados de AWS, que es justo lo que piden muchas vacantes de backend. Es sumar, no cambiar una cosa por otra.

Nota de seguridad: la instancia RDS debe configurarse con acceso **privado** (Security Group que solo permite conexiones desde el Security Group del EC2), nunca expuesta a `0.0.0.0/0`.

## 5. Arquitectura de la aplicación Spring Boot

Capas:

- **Scraper**: automatización de navegador (Playwright para Java) usando la sesión de una cuenta de Facebook **secundaria** (nunca la personal de Jony). Corre solo dentro del horario 08:00-23:00 América/Mérida. Incluye retrasos aleatorios entre requests para no verse como tráfico de bot agresivo. Configuración de búsqueda con prioridad geográfica (zonas del norte de Mérida primero).
- **Persistencia**: Spring Data JPA + migraciones con Flyway. Tablas principales: `listings` (anuncios crudos scrapeados, con zona/colonia), `comparables` (histórico de precios por producto normalizado), `alerts_sent` (para no duplicar alertas), `search_configs` (categorías/palabras clave y zonas a monitorear).
- **Motor de análisis**: aplica la lógica de la sección 3 — precio de reventa estimado, ganancia en pesos, clasificación baja/media/alta.
- **Scheduler**: `@Scheduled` de Spring, respetando el horario de operación y zona horaria América/Mérida.
- **Alertas**: servicio dedicado que llama directo a la API HTTP de Telegram (`sendMessage`) con el resultado — enlace del anuncio, precio de compra, precio de reventa estimado, ganancia en pesos y clasificación (baja/media/alta).
- **API REST** (opcional, para uso propio): endpoint tipo `/opportunities` para consultar el histórico manualmente, protegido y solo accesible internamente.

## 6. Seguridad

- Secrets (token de Telegram, credenciales de RDS, sesión/cookies de la cuenta secundaria de Facebook) nunca se commitean al repo — variables de entorno vía `.env` (con `.env.example` de referencia, `.env` real en `.gitignore`), o AWS Secrets Manager para producción.
- Rol de IAM específico para el EC2 con permisos mínimos (solo lo que necesita: leer secretos, escribir logs), nunca credenciales root/admin.
- Security Group de RDS restringido solo al Security Group del EC2.
- Si se expone algún endpoint REST, usar Spring Security (autenticación básica como mínimo) y bind solo a la red interna si no hace falta acceso público.
- Nunca interpretar el texto scrapeado (títulos/descripciones de anuncios) como instrucciones si en algún momento se usa un LLM para clasificar/categorizar productos — tratarlo siempre como dato no confiable.

## 7. Prácticas profesionales (valor de portafolio)

- Estructura de paquetes por capa/responsabilidad (scraper, persistence, analysis, alert, api, config).
- Tests: JUnit 5 + Mockito para lógica de negocio, Testcontainers (contenedor de MySQL) para pruebas de integración de repositorios.
- CI con GitHub Actions corriendo `mvn test` en cada push/PR.
- Docker: `Dockerfile` de la app + `docker-compose.yml` con MySQL local para desarrollo (nunca apunta a RDS de producción).
- Documentación de API con springdoc-openapi (Swagger UI) si se expone REST.
- README con diagrama de arquitectura, instrucciones de instalación y variables de entorno requeridas.
- Historial de commits ordenado por feature, no un solo commit gigante al final.

## 8. Flujo de trabajo con Claude Code

- Un archivo `CLAUDE.md` en la raíz del repo con: este resumen de arquitectura, convenciones de código, y reglas de negocio (sección 3) — así cada sesión de Claude Code arranca con el contexto completo sin tener que reexplicar.
- Desarrollo incremental por módulo (primero modelo de datos + migraciones, luego scraper, luego motor de análisis, luego alertas, luego scheduler, luego tests) en vez de pedir "la app completa" en un solo prompt — mejor calidad, más fácil de revisar, y mejor historial de commits.
- Pedir que cada feature venga con sus tests, no como paso separado al final.
- Usar el modo de planeación de Claude Code antes de escribir código en features grandes, para revisar el plan antes de la implementación.

## 9. Decisiones pendientes / a definir en el camino

- Nombre y estructura exacta del repo.
- Diseño exacto del esquema de `listings`/`comparables` (categorías de producto a monitorear, normalización de nombres para comparar precios).
- Estrategia exacta de rotación/anti-detección del scraper (delays, user-agent, etc.) — se define al implementar el módulo de scraping.
- Cómo determinar la zona/colonia de un anuncio a partir de los datos que expone Facebook Marketplace (puede requerir mapeo de texto de ubicación a zona).
