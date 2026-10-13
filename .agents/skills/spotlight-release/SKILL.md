---
name: spotlight-release
description: Preparar una entrega autorizada de Spotlight Next con tests, firma histórica, verificación de actualización y ZIP fuente del mismo commit.
---

# Flujo

1. Leer AGENTS.md, PLANS.md y los verificadores existentes. Identificar el SHA exacto autorizado, versión distribuida y evidencia de checks. Mantener main y respaldos intactos salvo autorización explícita.
2. Ejecutar unitarias, lint Debug/Release, compilación y Python; ejecutar pruebas instrumentadas/visuales pertinentes. Registrar fallos y corregir causas sin debilitar tests. No firmar una versión cuyo check requerido esté pendiente/fallido.
3. Verificar versión desde `release.properties` y el APK construido: paquete `dev.bautista.spotlight.next`, código mayor que el distribuido, minSdk compatible, release sin debuggable. No confiar solo en constantes fuente.
4. Compilar Release con R8 desde el SHA verificado. Firmar exclusivamente con los GitHub Secrets históricos mediante flujo autorizado, protegido y acotado; nunca en un PR o fork. La autorización ya dada para esa versión se conserva: no pedirla nuevamente ni solicitar claves en texto.
5. Verificar apksigner y zipalign. Certificado SHA-256 esperado: `11B0B3B37544BD95B128DE52798FA7FFB7489F94819614B1ADA383BD4EE7A6CF`. Comparar también con el APK histórico distribuido cuando esté disponible. No generar clave alternativa ni presentar debug como actualización release.
6. Crear `git archive` del mismo SHA que se compiló, sin claves/credenciales/cachés. Registrar commit, versión, hashes APK/ZIP, certificado, checks y manifiesto de procedencia. Distinguir SHA del código de un eventual SHA del workflow que orquesta la entrega.
7. Preparar ambos archivos juntos en un mecanismo privado confiable. Verificar el resultado/artefactos del flujo, no solo su inicio. Actualizar documentación y PLANS.md con enlaces y limitaciones.

La compatibilidad estática de paquete/certificado/código no acredita instalación física ni conservación de datos en One UI. Si no hay dispositivo, registrar esa prueba pendiente; nunca desinstalar la app del usuario para probar una actualización.
