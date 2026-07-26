# Plan de Rediseño y Actualización de Imagen Corporativa

El objetivo es modernizar la interfaz de usuario de Terasenior, integrando el nuevo logo y aplicando principios de diseño centrados en personas mayores (accesibilidad, legibilidad y facilidad de interacción), manteniendo una estética actual y profesional.

## Cambios Propuestos

### 1. Gestión de Recursos
*   **Mover Logo**: Trasladar `logo.jpg` de la raíz a `shared/src/commonMain/composeResources/drawable/logo.jpg`.

### 2. Identidad Visual (Diseño de Interfaz)
*   **Paleta de Colores**: Definir un tema basado en Material 3 con colores calmados (azules terapéuticos y verdes suaves) pero con alto contraste para facilitar la lectura.
*   **Tipografía**: Aumentar los tamaños de fuente base y asegurar que los pesos sean claros.
*   **Componentes**:
    *   Tarjetas con esquinas más redondeadas.
    *   Botones de gran tamaño (min-height 56dp).
    *   Espaciado generoso para evitar "ruido" visual.

### 3. Implementación de Pantallas

#### [MODIFY] [App.kt](file:///E:/Proyectos/Terasenior/shared/src/commonMain/kotlin/com/terapia/terasenior/App.kt)
*   Crear e integrar un envoltorio de tema personalizado (`TeraseniorTheme`).

#### [MODIFY] [LoginScreen.kt](file:///E:/Proyectos/Terasenior/shared/src/commonMain/kotlin/com/terapia/terasenior/ui/login/LoginScreen.kt)
*   Incluir el logo en la parte superior.
*   Simplificar los campos de texto.
*   Mejorar la jerarquía visual del formulario.

#### [MODIFY] [TreatmentMenuScreen.kt](file:///E:/Proyectos/Terasenior/shared/src/commonMain/kotlin/com/terapia/terasenior/treatment/ui/TreatmentMenuScreen.kt)
*   Rediseñar los botones de ejercicios para que parezcan "tarjetas de actividad".
*   Aumentar el tamaño de los iconos/emojis.

#### [MODIFY] [NumberSearchGame.kt](file:///E:/Proyectos/Terasenior/shared/src/commonMain/kotlin/com/terapia/terasenior/treatment/ui/NumberSearchGame.kt)
*   Optimizar la cuadrícula para que los números sean lo más grandes posible.
*   Mejorar los estados visuales (acierto/error).

### 4. Despliegue
*   Realizar commit y push para disparar el despliegue automático en GitHub Pages.

## Verificación Planificada

### Automatizada
1.  `.\gradlew :shared:assemble`: Verificar que los recursos se procesan correctamente.
2.  `.\gradlew :webApp:wasmJsBrowserDistribution`: Comprobar que la build de producción es válida.

### Manual
*   Confirmar visualmente en el navegador que el logo se muestra y la interfaz es legible.

---
> [!TIP]
> Como diseñador, me enfocaré en el "Contraste" y el "Tamaño de Toque", vital para nuestra audiencia senior.
