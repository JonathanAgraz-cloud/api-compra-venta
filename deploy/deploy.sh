#!/usr/bin/env bash
# Script de despliegue: correr en el EC2, dentro de /opt/api-compra-venta,
# como el usuario apicompraventa (o con sudo -u apicompraventa ./deploy/deploy.sh).
# No corre los tests aqui a proposito: el CI de GitHub Actions ya los corrio
# en el push, y Testcontainers/Docker en el t3.micro competiria por RAM con
# la JVM de la app en produccion.
set -euo pipefail

cd "$(dirname "$0")/.."

echo "==> git pull"
git pull origin master

echo "==> mvn package (sin tests)"
./mvnw -q -DskipTests package

echo "==> reiniciando el servicio"
sudo systemctl restart api-compra-venta

echo "==> listo. Revisa el estado con: sudo systemctl status api-compra-venta"
