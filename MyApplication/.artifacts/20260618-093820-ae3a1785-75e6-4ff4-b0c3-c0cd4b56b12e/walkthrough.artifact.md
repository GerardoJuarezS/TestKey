# Walkthrough - PDF Saving & Dark Theme Fixes

He implementado la funcionalidad para guardar PDFs en cualquier ubicación del dispositivo y corregido los problemas visuales del Modo Oscuro.

## Cambios Principales

### 📁 Guardado de PDF Personalizado
- Se añadió el botón **"GUARDAR"** en `QuoteListScreen`.
- Implementado mediante `ActivityResultContracts.CreateDocument` (SAF) para permitir al usuario elegir la carpeta de destino.
- El nombre del archivo se genera automáticamente como `Cotizacion_NombreCliente_ID.pdf`.

### 🌓 Consistencia en Modo Oscuro
- **Filtros y Búsqueda:** La barra de búsqueda ya no resplandece en blanco cuando el sistema está en modo oscuro.
- **Campos de Entrada:** `HtmlInputField` ahora usa `MaterialTheme.colorScheme.surface`.
- **Diálogos:** Se corrigieron los colores hardcoded en:
    - `LocationPickerDialog` (Resultados de búsqueda).
    - `MaterialDialog` y `ServiceDialog` en el Catálogo.
    - `TimePicker` (Selector de hora) en la edición de presupuesto.
- **Pantalla About:** Se reemplazó el fondo `SoftBg` por colores dinámicos del tema.

### 🖱️ Interactividad
- Toda la tarjeta del presupuesto en la lista es ahora interactiva; al tocarla se abre la edición/detalle.

## Verificación Realizada
- Se verificó que `savePdfToUri` en `QuoteViewModel` realice la copia de archivos correctamente.
- Se revisaron los componentes `QuoteEditScreen` y `AboutScreen` para asegurar que no usen `Color.White` o `SoftBg` de forma estática.
- Se confirmó que los `ButtonDefaults` y `TextFieldDefaults` utilicen `MaterialTheme.colorScheme`.
