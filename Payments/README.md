# Payments

Microservicio Spring Boot que implementa la gestión de pagos instantáneos y programados utilizando una Red de Petri No Autónoma (RPNA) como motor de control de flujo.

Desarrollado como caso de estudio de la tesis de Maestría en Ingeniería de Software (UNLP):
**"Modelado de Sistemas Reactivos mediante Redes de Petri no Autónomas y Microservicios"**.

El microservicio valida en la práctica el enfoque híbrido propuesto en la tesis: la lógica de control del flujo de cada caso de uso reside en una RPNA ejecutada por la librería [`PetriProcessor`](../../PetriProcessor), mientras que la lógica de negocio se implementa en *action components* desacoplados que son invocados cuando se dispara la transición correspondiente.

## Capacidades

- Recepción de eventos externos vía HTTP (`POST /v1/events`) y traducción declarativa a disparos de transiciones de la RPNA mediante el archivo `event-transition-mapping-full.json`.
- Implementación de los siete casos de uso definidos en la tesis (CU1 a CU7) sobre una única RPNA compuesta de 18 plazas y 35 transiciones.
- **Bifurcación basada en condición**: un mismo evento puede sensibilizar transiciones distintas según el contenido de su metadata (ej. el evento `FundsValidationResult` dispara la transición de éxito o de fallo según el booleano `fundsAvailable`).
- **Transiciones temporales asincrónicas**: las transiciones marcadas como `postFiringTimedTransitions` se disparan de manera diferida en un thread asincrónico (`AsyncTransitionService`).
- **Persistencia del estado de la red** en base de datos (`JpaPetriNetState`), de modo que el marcado y los UUIDs activos sobreviven a un reinicio del microservicio.
- **Dashboard web en tiempo real** (`/index.html`) que muestra el marcado, las transiciones habilitadas y el log de eventos, alimentado por un canal SSE (`GET /v1/state/stream`).

## Arquitectura

```
┌──────────────────────────────────────────────────────────────┐
│                       EventController                        │
│                    POST /v1/events                           │
└──────────────────────────────┬───────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                   EventProcessorService                      │
│   ──> resuelve evento → transición vía EventTransitionMapper │
│   ──> dispara la transición en PetriMonitor                  │
│   ──> invoca el action component asociado                    │
│   ──> propaga transiciones inmediatas / temporales           │
└─────────┬───────────────────────────┬────────────────────────┘
          │                           │
          ▼                           ▼
┌─────────────────────┐   ┌────────────────────────────────┐
│   PetriMonitor      │   │   IActionComponent             │
│  (petri-processor)  │   │   ├─ PaymentIntentActionComp.  │
│  ├─ isEnabled       │   │   ├─ AuthResultActionComp.     │
│  ├─ fireTransition  │   │   ├─ FundsValidationResultAC.  │
│  └─ control concur. │   │   ├─ PaymentProcessResultAC.   │
└─────────┬───────────┘   │   └─ ... (9 componentes)       │
          │               └────────────────────────────────┘
          ▼
┌──────────────────────────────────────────────────────────────┐
│              JpaPetriNetState (persistencia)                 │
│   Guarda PetriNetSnapshot en base de datos                   │
│   Notifica cambios al StateChangeNotifier (SSE)              │
└──────────────────────────────────────────────────────────────┘
```

## Estructura del proyecto

```
src/main/java/com/unlp/payments/
├── PaymentsApplication.java          # Punto de entrada Spring Boot
├── controller/
│   ├── EventController.java          # POST /v1/events
│   └── PetriNetStateController.java  # GET /v1/state, /config, /stream
├── service/
│   ├── EventProcessorService.java    # Orquestador evento → motor → acción
│   ├── EventTransitionMapper.java    # Carga del mapeo declarativo (JSON)
│   ├── AsyncTransitionService.java   # Disparo asincrónico de transiciones temporales
│   └── StateChangeNotifier.java      # Notificación SSE a los suscriptores
├── actions/
│   ├── IActionComponent.java         # Contrato de un action component
│   ├── PaymentIntentActionComponent.java
│   ├── AuthResultActionComponent.java
│   ├── FundsValidationResultActionComponent.java
│   ├── PaymentProcessResultActionComponent.java
│   ├── EjecutarPagoProgramadoActionComponent.java
│   ├── RegistroResultActionComponent.java
│   ├── ModificarPagoResultActionComponent.java
│   ├── GeneracionReporteResultActionComponent.java
│   └── EnvioReporteResultActionComponent.java
├── dto/                              # DTOs de eventos y metadata
├── utils/                            # Configuración de mapeo declarativo
├── persistence/                      # JPA: entity, repository, IPetriNetState
└── exceptions/                       # Manejo global de excepciones

src/main/resources/
├── application.properties
├── petri-config.json                 # Matrices de incidencia y marcado inicial
├── event-transition-mapping-full.json  # Mapeo eventos → transiciones (red completa)
├── event-transition-mapping.json     # Mapeo reducido (solo CU1)
└── static/index.html                 # Dashboard web en tiempo real
```

## Requisitos

- **Java 21**
- **Maven 3.6+** (el repositorio incluye `mvnw`).
- **Librería `petri-processor` versión 1.0.0** instalada en el repositorio Maven local. Si todavía no la tenés, antes de compilar este proyecto ejecutá `./mvnw clean install` desde el repositorio de [`PetriProcessor`](../../PetriProcessor).

## Compilación

```bash
./mvnw clean package
```

## Ejecución

```bash
./mvnw spring-boot:run
```

El microservicio queda escuchando en `http://localhost:8025`.

Por defecto utiliza una base de datos **H2 en memoria** (configurable en `application.properties`). La consola H2 está disponible en `http://localhost:8025/h2-console`.

El archivo `application.properties` también incluye la dependencia opcional con MySQL para casos en los que se quiera persistencia más allá del ciclo de vida del proceso; alcanza con reemplazar la URL de la base de datos y agregar las credenciales.

## Dashboard

Una vez levantado el servicio, el dashboard está disponible en:

```
http://localhost:8025/index.html
```

El dashboard se conecta al endpoint SSE (`/v1/state/stream`) y muestra en tiempo real:

- El marcado actual de la red.
- Las transiciones habilitadas en cada momento.
- El log de eventos procesados (incluyendo el resultado de cada disparo).
- Contadores de flujos exitosos y fallidos.
- Botones de prueba para disparar cada uno de los eventos soportados, con o sin metadata.

## Endpoints

| Método | Path                  | Descripción                                                   |
|--------|-----------------------|---------------------------------------------------------------|
| `POST` | `/v1/events`          | Recibe un evento y dispara las transiciones correspondientes. |
| `GET`  | `/v1/state`           | Devuelve el marcado actual y las transiciones habilitadas.    |
| `GET`  | `/v1/state/config`    | Devuelve el mapeo `place → nombre` y `transición → nombre`.   |
| `GET`  | `/v1/state/stream`    | Canal SSE de cambios de estado (lo consume el dashboard).     |
| `POST` | `/v1/state/reset`     | Reinicia el estado de la red.                                 |

### Ejemplo: enviar un evento

```bash
curl -X POST http://localhost:8025/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "FundsValidationResult",
    "uuid": "f3e9c8a4-1234-4567-89ab-cdef01234567",
    "metadata": { "fundsAvailable": true }
  }'
```

## Flujo end-to-end

1. El cliente envía un evento `POST /v1/events` con un `eventId`, un `uuid` de instancia y metadata opcional.
2. `EventController` delega en `EventProcessorService.handleEvent`.
3. `EventTransitionMapper` consulta su mapa interno (construido en el arranque a partir de `event-transition-mapping-full.json`) y devuelve la o las `TransitionMapping` que matchean el evento. Si el evento trae metadata, se filtra por `expectedConditionResult`.
4. Para cada transición resultante:
   1. Se construye una `PetriTransition` con el id de transición y el `uuid`.
   2. Se invoca `petriMonitor.fire(petriTransition)`. Esto puede dormir el hilo si la transición no está sensibilizada o si se trata de una transición temporal que aún no venció.
   3. Si la transición tiene un action component asociado, se ejecuta su método `executeAction(metadata)`.
   4. Se disparan las transiciones inmediatas declaradas en `postFiringTransitions` (sincrónicamente).
   5. Se disparan las transiciones temporales declaradas en `postFiringTimedTransitions` (asincrónicamente, vía `AsyncTransitionService`).
5. Cada cambio en el marcado se persiste en la base de datos a través de `JpaPetriNetState.save` y se notifica a los suscriptores SSE mediante `StateChangeNotifier`, actualizando el dashboard en tiempo real.

## Casos de uso soportados

Los nueve eventos definidos en `SupportedEvents.java` cubren los siete casos de uso del caso de estudio:

| Evento                       | CUs asociados | Descripción                                        |
|------------------------------|---------------|----------------------------------------------------|
| `PaymentIntent`              | Entrada de CU1-CU3, CU6, CU7 | Inicia un nuevo flujo en la red.    |
| `AuthenticationResult`       | CU7           | Resultado de autenticación; bifurca por `useCase`. |
| `FundsValidationResult`      | CU1, CU4      | Validación de fondos; bifurca por `fundsAvailable`.|
| `PaymentProcessResult`       | CU1, CU4      | Resultado de la ejecución del pago.                |
| `EjecutarPagoProgramado`     | CU4           | Disparo manual o por scheduler de un pago programado. |
| `RegistroResult`             | CU2           | Resultado del registro de un pago programado.      |
| `ModificarPagoResult`        | CU3           | Resultado de la modificación de un pago programado.|
| `GeneracionReporteResult`    | CU6           | Resultado de la generación de un reporte.          |
| `EnvioReporteResult`         | CU6           | Resultado del envío del reporte al usuario.        |
