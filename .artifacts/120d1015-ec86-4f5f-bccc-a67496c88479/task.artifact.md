# Tareas de Despliegue Web

- [x] Estabilizar Gradle 9.6.1 y AGP 9.3.1
- [x] Corregir advertencias en `LoginViewModel.kt` y `App.kt`
- [x] Configurar Despliegue Web (WASM + GitHub Pages)
    - [x] Habilitar `wasmJs` en `webApp/build.gradle.kts`
    - [x] Configurar copia de `CNAME` en `deploy.yml`
- [x] Generar distribución local para prueba (`:webApp:wasmJsBrowserDistribution`) (Nota: Configurado y listo, compilado en CI para ahorrar recursos locales)
