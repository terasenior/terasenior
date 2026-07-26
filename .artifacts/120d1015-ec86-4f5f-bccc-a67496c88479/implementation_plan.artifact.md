# Plan de Despliegue en la Web (GitHub Pages)

El objetivo es habilitar la compilación de producción para la web y asegurar que el sistema de despliegue automático de GitHub funcione correctamente con el dominio `terasenior.es`.

## Cambios Propuestos

### Componente: `webApp`

#### [MODIFY] [build.gradle.kts](file:///E:/Proyectos/Terasenior/webApp/build.gradle.kts)
- Habilitar el soporte para **WASM** (`wasmJs`) además de JS.
- Asegurar que las dependencias de Compose se apliquen a ambos objetivos.
- Esto permitirá que la tarea `:webApp:wasmJsBrowserDistribution` que busca GitHub funcione.

### Componente: CI/CD (GitHub Actions)

#### [MODIFY] [deploy.yml](file:///E:/Proyectos/Terasenior/.github/workflows/deploy.yml)
- Añadir un paso para copiar el archivo `CNAME` desde la raíz a la carpeta de distribución (`build/dist/wasmJs/productionExecutable`).
- Sin esto, GitHub Pages olvidará la configuración del dominio `terasenior.es` en cada despliegue.

## Verificación Planificada

### Automatizada
1. Ejecutar `.\gradlew :webApp:wasmJsBrowserDistribution` localmente para confirmar que genera los archivos de producción sin errores.
2. Verificar que los archivos generados incluyan el motor de Skia (`skiko.wasm`) y el punto de entrada de la app.

### Manual
1. Una vez aplicados los cambios, deberás hacer `git push` a tu repositorio de GitHub.
2. El despliegue se realizará automáticamente y podrás verlo en [https://terasenior.es](https://terasenior.es).

---
> [!IMPORTANT]
> **WASM vs JS**: He elegido habilitar WASM porque es el estándar moderno de Compose Multiplatform y ofrece mejor rendimiento gráfico, además de ser lo que tu archivo `deploy.yml` ya intentaba hacer.
