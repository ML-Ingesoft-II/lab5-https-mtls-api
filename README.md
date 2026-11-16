# Laboratorio 5 – Seguridad en el Canal de Comunicación (HTTPS, HSTS y mTLS)

API REST desarrollada en **Java 17** y **Spring Boot** para el Laboratorio 5 de Ingeniería de Software II (UNAL).

## Requisitos previos
- Java 17 o superior (`java` y `keytool` disponibles en el `PATH`).
- Maven (o el wrapper `./mvnw` incluido en el proyecto).

---

## 1. Generación de Certificados y Almacenes (Ejercicio 3)

Por políticas de seguridad, este repositorio excluye los almacenes criptográficos (`*.p12`), certificados (`*.crt`) y variables de entorno (`.env`). Antes de ejecutar el proyecto, genera los archivos localmente desde la raíz del repositorio:

### 1.1. Keystore del servidor (`keystore.p12`) y certificado público (`seii.crt`)
```bash
# Generar par de llaves RSA y certificado autofirmado para localhost
keytool -genkeypair -alias seii -keyalg RSA -keysize 2048 -validity 30 \
  -storetype PKCS12 -keystore src/main/resources/keystore.p12 \
  -dname "CN=localhost" -ext "SAN=dns:localhost,ip:127.0.0.1"

# Exportar certificado público en formato PEM
keytool -exportcert -alias seii -keystore src/main/resources/keystore.p12 -rfc -file seii.crt

```

### 1.2. Truststore para el cliente Java (`confianza.p12`)

```bash
keytool -importcert -alias seii -file seii.crt -keystore confianza.p12 -storetype PKCS12 -noprompt

```

---

## 2. Ejecución de la API por HTTPS (Puerto 8443)

Define la variable de entorno `KEYSTORE_PASSWORD` con la misma contraseña utilizada al crear `keystore.p12` y arranca la aplicación:

```bash
export KEYSTORE_PASSWORD=clave123
./mvnw spring-boot:run

```

*(Nota para mTLS – Ejercicio 4: cuando se habilite autenticación mutua de clientes, definir adicionalmente `export TRUSTSTORE_PASSWORD=<clave>`).*

---

## 3. Verificación Rápida

### Con `curl`

```bash
curl -i --cacert seii.crt https://localhost:8443/api/v1/salud

```

### Con cliente Java (`HttpClient`)

```bash
javac -d target/classes src/main/java/com/unal/isw2/lab5/ClienteHttps.java
java -Djavax.net.ssl.trustStore=confianza.p12 \
     -Djavax.net.ssl.trustStorePassword=clave123 \
     -cp target/classes com.unal.isw2.lab5.ClienteHttps

```

```

### Verificación final antes del `git push`:
Ejecuta `git status` en tu terminal y confirma que **no** aparezcan `keystore.p12`, `confianza.p12` ni `seii.crt` en la lista[cite: 4]. Luego haz `git add .`, `git commit -m "feat: configurar API HTTPS con HSTS y cliente truststore (Ejercicio 3)"` y sube los cambios para que Óscar continúe con el Ejercicio 4[cite: 4].
