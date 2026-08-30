# Despliegue a EC2 + RDS

Runbook para llevar la app (backend + frontend integrado) a producción, en la
misma instancia EC2 donde ya corre OpenClaw (`18.226.8.248`, t3.micro,
us-east-2). Corresponde a la sección 5ter de `arquitectura-tecnica.md`.

La app corre como servicio de systemd (`deploy/api-compra-venta.service`),
bajo un usuario de sistema dedicado (`apicompraventa`, separado de `openclaw`
por aislamiento/permisos mínimos), contra una base de datos real en Amazon
RDS. Actualizaciones futuras se hacen con `deploy/deploy.sh`.

## Fase 1 — Crear el RDS (consola de AWS)

1. Entra a la consola de AWS, región **us-east-2 (Ohio)** (verifica arriba a
   la derecha que sea la región correcta).
2. En la instancia EC2 (`18.226.8.248`): pestaña **Security** → anota el
   **Security Group** y la **VPC** actuales.
3. RDS → **Create database** → Engine **MySQL** → plantilla **Free tier**.
   - Clase de instancia: `db.t3.micro` o `db.t4g.micro` (la que ofrezca free
     tier en ese momento).
   - Almacenamiento: 20 GB.
   - **Misma VPC** que el EC2 (paso 2).
   - **Public access: No** (nunca exponer el RDS a internet).
   - Define un usuario y password maestros — serán `DB_USERNAME` /
     `DB_PASSWORD`.
   - Nombre de la base de datos inicial: `marketplace`.
4. Security Group del RDS: agrega una regla de entrada
   **TCP 3306, source = el Security Group del EC2** (nunca `0.0.0.0/0`). Si
   RDS creó un Security Group nuevo automáticamente, edítalo para dejar solo
   esa regla.
5. Cuando el RDS quede disponible ("Available"), copia su **endpoint**
   (algo como `xxxxxxxxxx.xxxxxxxxxx.us-east-2.rds.amazonaws.com`) — es tu
   `DB_HOST`.

## Fase 2 — Abrir el puerto del dashboard (consola de AWS)

En el Security Group del EC2: agrega una regla de entrada para el puerto
**8080** (el dashboard/API). Recomendado: source = "My IP" en vez de
`0.0.0.0/0` — ya queda protegido por Basic Auth de cualquier forma, pero
restringir la fuente reduce la superficie de ataque.

## Fase 3 — Preparar el EC2 (por SSH)

Conéctate como siempre:

```
ssh -i "C:\Users\Jony\OneDrive\Proyecto Compra-Venta\Key-Proyecto_PC_Cloud.pem" ec2-user@18.226.8.248
```

1. **Java 21** (verificar/instalar):
   ```
   java -version
   sudo dnf install -y java-21-amazon-corretto   # Amazon Linux 2023
   ```

2. **Usuario de sistema dedicado** para la app:
   ```
   sudo useradd --system --home /opt/api-compra-venta --create-home --shell /usr/sbin/nologin apicompraventa
   ```

3. **Clonar el repo** (público, no necesita credenciales):
   ```
   sudo git clone https://github.com/JonathanAgraz-cloud/api-compra-venta.git /opt/api-compra-venta
   sudo chown -R apicompraventa:apicompraventa /opt/api-compra-venta
   ```

4. **Compilar** (como el usuario de la app):
   ```
   cd /opt/api-compra-venta
   sudo -u apicompraventa ./mvnw -q -DskipTests package
   ```

5. **Chromium para Playwright** (el scraper lo necesita):
   ```
   sudo -u apicompraventa ./mvnw exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps chromium"
   ```
   Nota: `--with-deps` está pensado para Debian/Ubuntu; en Amazon Linux puede
   fallar al instalar dependencias del sistema. Si pasa, lo resolvemos juntos
   revisando el error exacto (puede necesitar instalar paquetes equivalentes
   a mano con `dnf`).

6. **Copiar la sesión de Facebook** — desde tu PowerShell (no desde el EC2):
   ```
   scp -i "C:\Users\Jony\OneDrive\Proyecto Compra-Venta\Key-Proyecto_PC_Cloud.pem" "C:\Users\Jony\.marketplace-scraper\facebook-session.json" ec2-user@18.226.8.248:/tmp/
   ```
   Luego, en el EC2:
   ```
   sudo mv /tmp/facebook-session.json /opt/api-compra-venta/facebook-session.json
   sudo chown apicompraventa:apicompraventa /opt/api-compra-venta/facebook-session.json
   sudo chmod 600 /opt/api-compra-venta/facebook-session.json
   ```

7. **Variables de entorno de producción** — crear `/etc/api-compra-venta.env`
   (usa `.env.example` como referencia de qué variables existen):
   ```
   sudo nano /etc/api-compra-venta.env
   ```
   Contenido (reemplaza los valores reales):
   ```
   SPRING_PROFILES_ACTIVE=prod
   DB_HOST=<endpoint-del-rds>
   DB_PORT=3306
   DB_NAME=marketplace
   DB_USERNAME=<usuario-maestro-rds>
   DB_PASSWORD=<password-maestro-rds>
   TELEGRAM_BOT_TOKEN=<token-real>
   TELEGRAM_CHAT_ID=<chat-id-real>
   SCRAPER_SESSION_FILE=/opt/api-compra-venta/facebook-session.json
   SCRAPER_HEADLESS=true
   SCHEDULER_ENABLED=true
   DASHBOARD_USERNAME=<usuario-del-dashboard>
   DASHBOARD_PASSWORD=<password-del-dashboard>
   ```
   Permisos (nunca legible por otros usuarios del sistema):
   ```
   sudo chown apicompraventa:apicompraventa /etc/api-compra-venta.env
   sudo chmod 600 /etc/api-compra-venta.env
   ```

8. **Instalar el servicio de systemd**:
   ```
   sudo cp /opt/api-compra-venta/deploy/api-compra-venta.service /etc/systemd/system/
   sudo systemctl daemon-reload
   sudo systemctl enable --now api-compra-venta
   ```

9. **Verificar**:
   ```
   sudo systemctl status api-compra-venta
   sudo journalctl -u api-compra-venta -f
   ```
   Deberías ver a Flyway validando las migraciones contra el RDS y, si es
   dentro del horario 08:00-23:00 América/Mérida, al scheduler arrancando.
   Abre `http://18.226.8.248:8080` en el navegador — debe pedir usuario y
   contraseña (Basic Auth) y luego mostrar el dashboard.

## Actualizar la app después del primer despliegue

Cada vez que haya cambios nuevos en `master`:

```
ssh -i "C:\Users\Jony\OneDrive\Proyecto Compra-Venta\Key-Proyecto_PC_Cloud.pem" ec2-user@18.226.8.248
sudo -u apicompraventa /opt/api-compra-venta/deploy/deploy.sh
```
