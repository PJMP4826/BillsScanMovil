# BillsScan

![Vista previa de BillsScan](https://raw.githubusercontent.com/PJMP4826/BillsScanMovil/refs/heads/master/app/docs/MaquetacionMovil.png)

BillsScan es una aplicación móvil diseñada para la digitalización, almacenamiento y consulta de tickets o recibos de compra. Su objetivo es ayudar a las empresas a gestionar de manera eficiente la información de sus compras, facilitando el acceso y organización de los comprobantes digitales.

## Descripción del Proyecto

Esta aplicación permite a los usuarios:
- Subir imágenes de tickets de compra.
- Consultar y visualizar los tickets almacenados.
- Generar reportes en formato Excel con la información de los tickets.
- Acceder a un historial de compras detallado.
- Ver la información de contacto del beneficiario.

BillsScan está especialmente diseñada para el departamento de compras y el equipo de contabilidad de las empresas, ofreciendo una plataforma sencilla e intuitiva para mantener un registro digital de sus adquisiciones.

## Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **Framework UI:** Jetpack Compose
- **Backend:** Firebase (Firestore, Authentication, Storage)
- **Almacenamiento Local:** SharedPreferences
- **Entorno de Desarrollo:** Android Studio

## Instalación y Configuración

Clonar el repositorio:

```bash
git clone https://github.com/PJMP4826/BillsScanMovil.git
```


## Escáner de Documentos con ML Kit

### Descripción

Integra la API de escáner de documentos de ML Kit en tu aplicación Android para añadir fácilmente una función de escaneo de documentos. Esta sección proporciona detalles sobre la implementación, el uso y las opciones de personalización del escáner de documentos.

### Vista Previa

![Vista previa del escáner](https://github.com/shubhanshu24510/CameraX/assets/100926922/8c2bcf8b-00fa-43a1-984f-2d8e422656b8)

### Ejemplo de Resultado

![Resultado 1](https://github.com/shubhanshu24510/CameraX/assets/100926922/1da7df99-49d7-4780-9089-6d44e3f55a48)
![Resultado 2](https://github.com/shubhanshu24510/CameraX/assets/100926922/79feaca2-7cb8-446d-a976-9d1031c81d3a)
![Resultado 3](https://github.com/shubhanshu24510/CameraX/assets/100926922/2659d5a4-b966-49e0-8f2f-5a9c22938b2e)
![Resultado 4](https://github.com/shubhanshu24510/CameraX/assets/100926922/daea3d9a-9a5b-4ba3-a4b3-ad65a1dc63e8)

### Detalles de la Librería

- **Nombre del SDK:** play-services-mlkit-document-scanner
- **Implementación:** Los modelos, la lógica de escaneo y el flujo de la interfaz se descargan dinámicamente a través de Google Play Services.
- **Impacto en el Tamaño de la App:** Aproximadamente 300 KB de incremento en el tamaño de la descarga.
- **Tiempo de Inicialización:** Los usuarios pueden experimentar un breve retraso mientras se descargan los modelos, la lógica y el flujo de la interfaz antes del primer uso.

### Requisitos Técnicos

Asegúrate de que tu aplicación Android cumpla con los siguientes requisitos:

- **Versión Mínima del SDK:** Nivel de API de Android 21 o superior.
- En el archivo build.gradle a nivel de proyecto, incluye el repositorio de Maven de Google en las secciones buildscript y allprojects.

### Instalación

Añade la dependencia de la librería del escáner de documentos de ML Kit en el archivo build.gradle a nivel de módulo (app):

```gradle
dependencies {
    // ...
    implementation 'com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1'
}
```

### Configuración del Escáner de Documentos

Personaliza el flujo de usuario del escáner de documentos según los requisitos de tu aplicación. La pantalla de previsualización y el visor admiten varias opciones de control, como:

- Importar desde la galería de fotos
- Establecer un límite en el número de páginas escaneadas
- Modo de escáner (para controlar el conjunto de características del flujo)

Ejemplo de configuración:

```kotlin
val options = GmsDocumentScannerOptions.Builder()
    .setGalleryImportAllowed(false)
    .setPageLimit(2)
    .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
    .setScannerMode(SCANNER_MODE_FULL)
    .build()
```

### Escaneo de Documentos

Después de configurar tus GmsDocumentScannerOptions, obtén una instancia de GmsDocumentScanner y lanza la actividad del escáner siguiendo las APIs de AndroidX Activity Result:

```kotlin
/*val scanner = GmsDocumentScanning.getClient(options)
val scannerLauncher = registerForActivityResult(StartIntentSenderForResult()) { result ->
    if (result.resultCode == RESULT_OK) {
        val result = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
        result.getPages()?.let { pages ->
            for (page in pages) {
                val imageUri = pages.get(0).getImageUri()
            }
        }
        result.getPdf()?.let { pdf ->
            val pdfUri = pdf.getUri()
            val pageCount = pdf.getPageCount()
        }
    }
}

scanner.getStartScanIntent(activity)
    .addOnSuccessListener { intentSender ->
        scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
    }
    .addOnFailureListener { exception ->
        // Manejar el fallo
    }
```*/

## Uso de la Aplicación

1. **Inicio de Sesión:**
    - Introduce tu correo electrónico y contraseña para acceder.

2. **Subir Ticket:**
    - Selecciona una imagen de tu ticket y súbela a la plataforma.

3. **Consultar Tickets:**
    - Visualiza los tickets almacenados con sus detalles (empresa, productos, fecha y hora de registro).

4. **Generar Reporte:**
    - Descarga un archivo Excel con la información de todos los tickets registrados.

5. **Historial:**
    - Revisa el resumen de compras por empresa y el total gastado.

6. **Información de Contacto:**
    - Accede a los datos del beneficiario registrado.

