# 🏦 Sistema de Microservicios Bancarios

Sistema bancario moderno desarrollado con arquitectura de microservicios utilizando Spring Boot 3.x, Java 21, MongoDB, Kafka y Redis.

## 📋 Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Microservicios](#microservicios)
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Contribución](#contribución)

## 🎯 Descripción General

Sistema bancario completo que permite la gestión de clientes, productos bancarios pasivos (cuentas), productos activos (créditos), transacciones, tarjetas de débito, transferencias, monedero electrónico Yanki y comercio de criptomonedas BootCoin.

### Características Principales

- ✅ Gestión de clientes personales y empresariales
- ✅ Cuentas bancarias (ahorro, corriente, plazo fijo)
- ✅ Productos crediticios (préstamos personales, empresariales, tarjetas de crédito)
- ✅ Sistema de transacciones con control de comisiones
- ✅ Tarjetas de débito con múltiples cuentas asociadas
- ✅ Transferencias entre cuentas
- ✅ Monedero electrónico Yanki
- ✅ Trading de criptomonedas BootCoin
- ✅ Reportes y analytics
- ✅ Autenticación y autorización con JWT

### Perfiles de Cliente

**Personal:**
- Standard: Máximo 1 cuenta de ahorro, 1 cuenta corriente, 1 plazo fijo
- VIP: Requiere tarjeta de crédito, sin comisiones de mantenimiento, mínimo promedio diario

**Empresarial:**
- Standard: Solo cuentas corrientes (múltiples), créditos empresariales
- PYME: Requiere tarjeta de crédito, sin comisiones

## 🏗 Arquitectura

El sistema está compuesto por 13 microservicios distribuidos en las siguientes categorías:

### Microservicios de Infraestructura
1. **Config Server**: Gestión centralizada de configuraciones
2. **Eureka Server**: Service Discovery
3. **API Gateway**: Punto de entrada único, routing y autenticación

### Microservicios de Negocio
4. **Auth Service**: Autenticación y autorización JWT
5. **Customer Service**: Gestión de clientes
6. **Passive Product Service**: Cuentas bancarias
7. **Active Product Service**: Productos crediticios
8. **Transaction Service**: Procesamiento de transacciones
9. **Debit Card Service**: Gestión de tarjetas de débito
10. **Transfer Service**: Transferencias entre cuentas
11. **Report Service**: Generación de reportes
12. **Yanki Service**: Monedero electrónico
13. **BootCoin Service**: Trading de criptomonedas

### Componentes de Soporte
- **MongoDB**: Base de datos NoSQL
- **Redis**: Cache distribuido
- **Apache Kafka**: Message broker para eventos
- **Zookeeper**: Coordinación de Kafka

## 🛠 Tecnologías

### Backend
- Java 21 (Virtual Threads, Pattern Matching, Records)
- Spring Boot 3.2.x
- Spring Cloud (Config, Netflix Eureka, Gateway)
- Spring WebFlux (Programación Reactiva)
- Spring Data MongoDB Reactive
- Spring Kafka
- Spring Security + JWT

### Base de Datos
- MongoDB 7.x
- Redis 7.x

### Message Broker
- Apache Kafka 3.x
- Zookeeper 3.x

### DevOps & Tools
- Docker & Docker Compose
- Maven 3.8+
- Resilience4j (Circuit Breaker)
- MapStruct (DTO Mapping)
- Lombok
- OpenAPI/Swagger

### Monitoreo
- Spring Boot Actuator
- MongoDB Express (UI)
- Kafdrop (Kafka UI)

## 📦 Microservicios

### 1. Config Server
**Puerto:** 8888
**Función:** Gestión centralizada de configuraciones para todos los microservicios.

**Endpoints:**
- `GET /{application}/{profile}` - Obtener configuración

### 2. Eureka Server
**Puerto:** 8761
**Función:** Service Discovery y registro de microservicios.

**Dashboard:** http://localhost:8761

### 3. API Gateway
**Puerto:** 8080
**Función:** Enrutamiento, autenticación JWT, rate limiting, CORS.

**Rutas:**
- `/auth/**` - Auth Service
- `/customers/**` - Customer Service
- `/passive-products/**` - Passive Product Service
- `/active-products/**` - Active Product Service
- `/transactions/**` - Transaction Service
- `/debit-cards/**` - Debit Card Service
- `/transfers/**` - Transfer Service
- `/reports/**` - Report Service
- `/yanki/**` - Yanki Service
- `/bootcoin/**` - BootCoin Service

### 4. Auth Service
**Puerto:** 8081
**Base de datos:** auth_db

**Endpoints principales:**
```
POST   /auth/register       - Registro de usuario
POST   /auth/login          - Inicio de sesión
POST   /auth/validate       - Validar token
POST   /auth/refresh        - Refrescar token
GET    /users               - Listar usuarios
GET    /users/{id}          - Obtener usuario
PUT    /users/{id}          - Actualizar usuario
DELETE /users/{id}          - Eliminar usuario
```

### 5. Customer Service
**Puerto:** 8082
**Base de datos:** customer_db

**Endpoints principales:**
```
POST   /customers                      - Crear cliente
GET    /customers                      - Listar clientes
GET    /customers/{id}                 - Obtener cliente
GET    /customers/document/{number}    - Buscar por documento
PUT    /customers/{id}                 - Actualizar cliente
DELETE /customers/{id}                 - Eliminar cliente
GET    /customers/validate/{id}        - Validar cliente
```

**Eventos Kafka:**
- Publica: CustomerCreatedEvent, CustomerUpdatedEvent, CustomerDeletedEvent

### 6. Passive Product Service
**Puerto:** 8083
**Base de datos:** passive_product_db

**Endpoints principales:**
```
POST   /passive-products                    - Crear cuenta
GET    /passive-products                    - Listar cuentas
GET    /passive-products/{id}               - Obtener cuenta
GET    /passive-products/customer/{id}      - Cuentas por cliente
GET    /passive-products/{id}/balance       - Obtener saldo
PUT    /passive-products/{id}               - Actualizar cuenta
DELETE /passive-products/{id}               - Eliminar cuenta
```

**Reglas de Negocio:**
- Personal Standard: Máx. 1 ahorro, 1 corriente, 1 plazo fijo
- Personal VIP: Requiere tarjeta de crédito, sin comisiones
- Empresarial: Solo cuentas corrientes (múltiples)
- PYME: Requiere tarjeta de crédito

**Eventos Kafka:**
- Publica: AccountCreatedEvent, BalanceChangedEvent
- Escucha: CustomerDeletedEvent

### 7. Active Product Service
**Puerto:** 8084
**Base de datos:** active_product_db

**Endpoints principales:**
```
POST   /active-products                     - Crear crédito
GET    /active-products                     - Listar créditos
GET    /active-products/{id}                - Obtener crédito
GET    /active-products/customer/{id}       - Créditos por cliente
POST   /active-products/{id}/charge         - Registrar consumo
POST   /active-products/{id}/payment        - Registrar pago
GET    /active-products/{id}/balance        - Obtener balance
GET    /active-products/customer/{id}/overdue - Verificar morosidad
PUT    /active-products/{id}                - Actualizar crédito
DELETE /active-products/{id}                - Eliminar crédito
```

**Reglas de Negocio:**
- Personal: Máx. 1 crédito personal
- Empresarial: Múltiples créditos
- Control de morosidad automático

**Eventos Kafka:**
- Publica: CreditCreatedEvent, PaymentReceivedEvent, OverdueDebtDetectedEvent

### 8. Transaction Service
**Puerto:** 8085
**Base de datos:** transaction_db

**Endpoints principales:**
```
POST   /transactions/deposit            - Depósito
POST   /transactions/withdrawal         - Retiro
POST   /transactions/payment            - Pago
POST   /transactions/charge             - Cargo
GET    /transactions/{id}               - Obtener transacción
GET    /transactions/product/{id}       - Transacciones por producto
GET    /transactions/customer/{id}      - Transacciones por cliente
```

**Reglas de Negocio:**
- Cálculo automático de comisiones
- Validación de límites de transacciones gratuitas
- Restricciones para cuentas de plazo fijo

**Eventos Kafka:**
- Publica: TransactionCompletedEvent, CommissionChargedEvent

### 9. Debit Card Service
**Puerto:** 8086
**Base de datos:** debit_card_db

**Endpoints principales:**
```
POST   /debit-cards                         - Crear tarjeta
GET    /debit-cards/{id}                    - Obtener tarjeta
GET    /debit-cards/customer/{id}           - Tarjetas por cliente
POST   /debit-cards/{id}/associate-account  - Asociar cuenta
PUT    /debit-cards/{id}/main-account       - Definir cuenta principal
POST   /debit-cards/{id}/payment            - Realizar pago
POST   /debit-cards/{id}/withdrawal         - Realizar retiro
GET    /debit-cards/{id}/balance            - Consultar saldo
GET    /debit-cards/{id}/movements          - Últimos 10 movimientos
```

**Características:**
- Asociación a múltiples cuentas
- Cuenta principal con fallback automático
- Historial de movimientos

**Eventos Kafka:**
- Publica: DebitCardCreatedEvent, DebitCardPaymentEvent
- Escucha: AccountCreatedEvent

### 10. Transfer Service
**Puerto:** 8087
**Base de datos:** transfer_db

**Endpoints principales:**
```
POST   /transfers/own-accounts          - Transferencia entre cuentas propias
POST   /transfers/third-party           - Transferencia a terceros
GET    /transfers/{id}                  - Obtener transferencia
GET    /transfers/account/{id}          - Transferencias por cuenta
```

**Reglas de Negocio:**
- Validación de saldo suficiente
- Actualización atómica de saldos
- Registro automático de transacciones

**Eventos Kafka:**
- Publica: TransferCompletedEvent, TransferFailedEvent

### 11. Report Service
**Puerto:** 8088
**Función:** Generación de reportes y análisis

**Endpoints principales:**
```
GET    /reports/customer/{id}/daily-average     - Promedio diario de saldo
GET    /reports/commissions                     - Reporte de comisiones
GET    /reports/customer/{id}/consolidated      - Reporte consolidado
GET    /reports/product/{type}                  - Reporte por tipo de producto
```

**Características:**
- Consolidación de datos de múltiples servicios
- Exportación a Excel y PDF
- Análisis en tiempo real

### 12. Yanki Service
**Puerto:** 8089
**Base de datos:** yanki_db

**Endpoints principales:**
```
POST   /yanki/wallets                   - Crear monedero
POST   /yanki/send                      - Enviar pago
POST   /yanki/load                      - Cargar saldo
POST   /yanki/withdraw                  - Retirar saldo
POST   /yanki/{id}/associate-card       - Asociar tarjeta débito
GET    /yanki/{id}/balance              - Consultar saldo
GET    /yanki/phone/{number}            - Buscar por teléfono
```

**Características:**
- No requiere ser cliente del banco
- Pagos con número de celular
- Integración opcional con tarjeta de débito
- Carga/descarga de saldo desde cuenta bancaria

**Eventos Kafka:**
- Publica: YankiPaymentEvent, YankiLoadEvent

### 13. BootCoin Service
**Puerto:** 8090
**Base de datos:** bootcoin_db

**Endpoints principales:**
```
POST   /bootcoin/wallets                        - Crear wallet
GET    /bootcoin/wallets/{id}                   - Obtener wallet
GET    /bootcoin/wallets/{id}/balance           - Consultar balance
POST   /bootcoin/exchange-rates                 - Configurar tasa cambio
GET    /bootcoin/exchange-rates/current         - Obtener tasa actual
POST   /bootcoin/transactions/buy-request       - Solicitar compra
POST   /bootcoin/transactions/{number}/accept   - Aceptar intercambio
POST   /bootcoin/transactions/{number}/complete - Completar transacción
GET    /bootcoin/transactions/{number}          - Obtener transacción
```

**Flujo de Compra/Venta:**
1. Usuario solicita compra especificando monto y método de pago
2. Otro usuario acepta el intercambio
3. Sistema genera número de transacción
4. Se validan los datos de la operación
5. Se completa la transacción

**Métodos de Pago:**
- Yanki
- Transferencia bancaria

## 🔧 Requisitos Previos

### Software Necesario

```bash
# Java 21
java -version

# Maven 3.8+
mvn -version

# Docker Desktop
docker --version
docker-compose --version

# Git
git --version
```

### Herramientas Recomendadas

- **IDE:** IntelliJ IDEA / Eclipse / VS Code con Java extensions
- **Cliente MongoDB:** MongoDB Compass
- **Cliente Redis:** Redis Desktop Manager (opcional)
- **API Testing:** Postman
- **Diagramas:** Draw.io

## 📥 Instalación

### 1. Clonar Repositorios

```bash
# Crear directorio principal
mkdir banking-microservices && cd banking-microservices

# Clonar repositorios (ejemplo)
git clone <url-config-server>
git clone <url-eureka-server>
git clone <url-api-gateway>
# ... clonar todos los repositorios
```

### 2. Configurar Variables de Entorno

Crear archivo `.env` en el directorio raíz:

```env
# MongoDB
MONGO_HOST=localhost
MONGO_PORT=27017
MONGO_USERNAME=admin
MONGO_PASSWORD=admin123

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# JWT
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRATION=86400000

# Config Server
CONFIG_SERVER_URI=http://localhost:8888

# Eureka Server
EUREKA_SERVER_URI=http://localhost:8761/eureka
```

## ⚙️ Configuración

### 1. Iniciar Infraestructura con Docker Compose

```bash
# Levantar servicios de infraestructura
docker-compose -f docker-compose-infrastructure.yml up -d

# Verificar estado
docker-compose ps
```

**Servicios disponibles:**
- MongoDB: http://localhost:27017
- MongoDB Express: http://localhost:8081
- Redis: localhost:6379
- Kafka: localhost:9092
- Kafdrop: http://localhost:9000
- Zookeeper: localhost:2181

### 2. Inicializar Base de Datos

```bash
# Ejecutar scripts de inicialización
docker exec -it mongodb mongosh < mongodb-init-scripts.js
```

### 3. Compilar Microservicios

```bash
# Compilar todos los servicios
./build-all.sh

# O individualmente
cd config-server && mvn clean install
cd eureka-server && mvn clean install
# ... etc
```

## 🚀 Ejecución

### Orden de Arranque

**Es importante seguir este orden:**

1. **Config Server** (puerto 8888)
```bash
cd config-server
mvn spring-boot:run
```

2. **Eureka Server** (puerto 8761)
```bash
cd eureka-server
mvn spring-boot:run
```

3. **API Gateway** (puerto 8080)
```bash
cd api-gateway
mvn spring-boot:run
```

4. **Auth Service** (puerto 8081)
```bash
cd auth-service
mvn spring-boot:run
```

5. **Microservicios de Negocio** (en cualquier orden)
```bash
# Customer Service (8082)
cd customer-service && mvn spring-boot:run

# Passive Product Service (8083)
cd passive-product-service && mvn spring-boot:run

# Active Product Service (8084)
cd active-product-service && mvn spring-boot:run

# Transaction Service (8085)
cd transaction-service && mvn spring-boot:run

# Debit Card Service (8086)
cd debit-card-service && mvn spring-boot:run

# Transfer Service (8087)
cd transfer-service && mvn spring-boot:run

# Report Service (8088)
cd report-service && mvn spring-boot:run

# Yanki Service (8089)
cd yanki-service && mvn spring-boot:run

# BootCoin Service (8090)
cd bootcoin-service && mvn spring-boot:run
```

### Ejecución con Docker

```bash
# Construir imágenes
./docker-build-all.sh

# Levantar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

### Verificar Estado

```bash
# Verificar Eureka Dashboard
open http://localhost:8761

# Verificar servicios registrados
curl http://localhost:8761/eureka/apps

# Health check de servicios
curl http://localhost:8080/actuator/health
```

## 🧪 Testing

### Unit Tests

```bash
# Ejecutar tests de un servicio
cd customer-service
mvn test

# Ejecutar todos los tests
./run-all-tests.sh
```

### Integration Tests

```bash
# Tests de integración
mvn verify -P integration-tests
```

### Tests con Postman

```bash
# Importar colecciones desde postman-collections/
# Configurar environment con variables:
# - base_url: http://localhost:8080
# - token: (se genera después del login)
```

**Colecciones disponibles:**
- Auth Collection
- Customer Collection
- Passive Products Collection
- Active Products Collection
- Transactions Collection
- Debit Cards Collection
- Transfers Collection
- Yanki Collection
- BootCoin Collection
- Reports Collection

## 📚 API Documentation

### Swagger UI

Cada microservicio expone su documentación OpenAPI:

```
Config Server:        N/A
Eureka Server:        N/A
API Gateway:          http://localhost:8080/swagger-ui.html
Auth Service:         http://localhost:8081/swagger-ui.html
Customer Service:     http://localhost:8082/swagger-ui.html
Passive Products:     http://localhost:8083/swagger-ui.html
Active Products:      http://localhost:8084/swagger-ui.html
Transaction Service:  http://localhost:8085/swagger-ui.html
Debit Card Service:   http://localhost:8086/swagger-ui.html
Transfer Service:     http://localhost:8087/swagger-ui.html
Report Service:       http://localhost:8088/swagger-ui.html
Yanki Service:        http://localhost:8089/swagger-ui.html
BootCoin Service:     http://localhost:8090/swagger-ui.html
```

### Postman Collections

Las colecciones completas están disponibles en el repositorio `postman-collections`.

## 🔐 Autenticación

### Obtener Token JWT

```bash
# Registro
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@bank.com",
    "roles": ["ADMIN"]
  }'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### Usar Token en Requests

```bash
# Incluir token en Authorization header
curl -X GET http://localhost:8080/customers \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## 📊 Monitoreo

### Actuator Endpoints

Todos los servicios exponen endpoints de actuator:

```bash
# Health
curl http://localhost:8082/actuator/health

# Metrics
curl http://localhost:8082/actuator/metrics

# Info
curl http://localhost:8082/actuator/info
```

### Kafka UI (Kafdrop)

Acceder a http://localhost:9000 para:
- Ver topics
- Monitorear mensajes
- Inspeccionar consumer groups

### MongoDB Express

Acceder a http://localhost:8081 para:
- Administrar bases de datos
- Ver colecciones
- Ejecutar queries

## 🏗 Arquitectura y Patrones

### Patrones Implementados

- **API Gateway Pattern**: Punto de entrada único
- **Service Discovery**: Registro dinámico de servicios
- **Circuit Breaker**: Resiliencia con Resilience4j
- **Event-Driven Architecture**: Comunicación asíncrona con Kafka
- **CQRS**: Separación de comandos y consultas
- **Saga Pattern**: Transacciones distribuidas
- **Database per Service**: Base de datos independiente por microservicio
- **Reactive Programming**: WebFlux y MongoDB Reactive

### Comunicación entre Servicios

**Síncrona:**
- REST API con Spring WebFlux
- OpenFeign para comunicación entre servicios

**Asíncrona:**
- Apache Kafka para eventos
- Redis para cache distribuido

## 🤝 Contribución

### Workflow de Desarrollo

1. Fork el repositorio
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

### Estándares de Código

- Java Code Style: Google Java Style Guide
- Commits: Conventional Commits
- Branches: GitFlow

### Code Review Checklist

- [ ] Tests unitarios agregados
- [ ] Tests de integración actualizados
- [ ] Documentación actualizada
- [ ] Swagger/OpenAPI actualizado
- [ ] Eventos Kafka documentados
- [ ] Logs apropiados
- [ ] Manejo de excepciones
- [ ] Performance considerado

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver archivo `LICENSE` para más detalles.

## 👥 Autores

- **Tu Nombre** - *Desarrollo Inicial* - [GitHub](https://github.com/tu-usuario)

## 🙏 Agradecimientos

- Spring Boot Team
- Anthropic (Claude AI)
- Comunidad Open Source

## 📧 Contacto

Para preguntas o sugerencias:
- Email: tu-email@ejemplo.com
- GitHub Issues: [Issues](https://github.com/tu-usuario/banking-microservices/issues)

---

⭐ Si este proyecto te resultó útil, considera darle una estrella en GitHub!
