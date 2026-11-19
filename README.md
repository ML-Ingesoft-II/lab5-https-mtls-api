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

## 2.bis. Mutual TLS – mTLS (Ejercicio 4c/4d)

El `application.properties` activa autenticación mutua con `server.ssl.client-auth=need`, por lo que el servidor **exige** un certificado de cliente válido presente en `truststore.p12`. Genera primero el certificado del cliente y el truststore del servidor:

```bash
# Certificado del cliente (microservicio de pagos)
keytool -genkeypair -alias pagos -keyalg RSA -keysize 2048 -validity 30 \
  -storetype PKCS12 -keystore cliente-pagos.p12 -dname "CN=servicio-pagos" \
  -storepass clave123

# Exportar su parte publica
keytool -exportcert -alias pagos -keystore cliente-pagos.p12 -rfc \
  -file cliente-pagos.crt -storepass clave123

# Importarla al truststore del servidor
keytool -importcert -alias pagos -file cliente-pagos.crt -noprompt \
  -storetype PKCS12 -keystore src/main/resources/truststore.p12 \
  -storepass clave123
```

Arranca la API con las dos contraseñas definidas:

```bash
export KEYSTORE_PASSWORD=clave123
export TRUSTSTORE_PASSWORD=clave123
./mvnw spring-boot:run
```

Prueba sin y con certificado de cliente:

```bash
# Sin certificado de cliente -> falla en el handshake TLS
curl --cacert seii.crt https://localhost:8443/api/v1/salud

# Con certificado de cliente -> 200 y responde con el CN del cliente
curl --cacert seii.crt --cert-type P12 --cert cliente-pagos.p12:clave123 \
  https://localhost:8443/api/v1/salud
# -> {"estado":"ok","cliente":"CN=servicio-pagos"}
```
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
