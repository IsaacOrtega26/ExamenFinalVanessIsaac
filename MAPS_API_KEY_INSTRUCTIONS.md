# Instrucciones para Configurar Google Maps API Key

## El mapa está saliendo en negro porque falta la API key

### Pasos para obtener y configurar la API key:

1. **Ir a Google Cloud Console:**
   - Ve a: https://console.cloud.google.com/

2. **Crear un proyecto o seleccionar uno existente**

3. **Habilitar Google Maps SDK for Android:**
   - Ve a "APIs & Services" > "Library"
   - Busca "Maps SDK for Android"
   - Haz clic en "Enable"

4. **Crear credenciales:**
   - Ve a "APIs & Services" > "Credentials"
   - Haz clic en "Create Credentials" > "API Key"
   - Copia la API key generada

5. **Restringir la API key (recomendado):**
   - Haz clic en la API key creada
   - En "Application restrictions", selecciona "Android apps"
   - Agrega el package name: `com.example.fieldlog`
   - Agrega el SHA-1 fingerprint de tu keystore

6. **Configurar en AndroidManifest.xml:**
   - Abre: `app/src/main/AndroidManifest.xml`
   - Busca la línea: `<meta-data android:name="com.google.android.geo.API_KEY" android:value="TU_API_KEY_AQUI" />`
   - Reemplaza `TU_API_KEY_AQUI` con tu API key real

### Nota:
Sin la API key configurada, el mapa aparecerá en negro. La aplicación seguirá funcionando para todo lo demás (crear registros, ver detalles, etc.), pero el mapa no se mostrará correctamente.

