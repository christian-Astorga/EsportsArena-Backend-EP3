# Esports Arena – API REST (Evaluación 3)

Backend de la plataforma de torneos Esports Arena: **Spring Boot 3 + JPA + MySQL + Spring Security + JWT + Swagger**.

## Requisitos
- Java 17 o superior
- Maven 3.9+ (o el Maven integrado de IntelliJ)
- MySQL 8 (o usar el perfil `h2`, que no necesita instalar nada)

## Cómo ejecutarlo

**Con MySQL** (crea la base `esports_arena` automáticamente):
```bash
# opcional: variables de entorno
#   DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET, CORS_ORIGINS
mvn spring-boot:run
```

**Sin MySQL (base en memoria, ideal para probar rápido):**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

La API queda en `http://localhost:8080`. Swagger está en **http://localhost:8080/swagger-ui.html** (vista estándar) y en **http://localhost:8080/docs.html** (el mismo Swagger UI con los colores del frontend).

Para probar endpoints protegidos en Swagger: ejecutar `POST /api/v1/auth/login`, copiar el `accessToken`, pulsar **Authorize** y pegarlo.

## Usuarios de demostración (se crean al primer arranque)
| Rol | Correo | Contraseña |
|---|---|---|
| ADMINISTRADOR | admin@esports.cl | Admin1234! |
| VENDEDOR | vendedor@esports.cl | Vendedor1234! |
| CLIENTE | cliente@esports.cl | Cliente1234! |

> Cambiar estas claves y definir `JWT_SECRET` (Base64, mínimo 32 bytes) antes de cualquier despliegue real.

## Modelo de datos
```
USUARIO ──< INSCRIPCION >── TORNEO >── JUEGO ──< EQUIPO >── USUARIO (capitán)
```
| Guía (tienda) | Este proyecto |
|---|---|
| Usuario | `usuarios` (con rol) |
| Categoría | `juegos` |
| Producto | `torneos` (+ tablas de imágenes, premios, llaves y calendario) |
| Boleta / Detalle_Boleta | `inscripciones` (un usuario se inscribe a un torneo) |
| – | `equipos` (tabla adicional; el PIN se guarda con hash BCrypt) |

## Roles y permisos
| Recurso | Público | CLIENTE | VENDEDOR | ADMINISTRADOR |
|---|---|---|---|---|
| Ver torneos y juegos | ✅ | ✅ | ✅ | ✅ |
| Crear/editar/eliminar torneos y juegos | ❌ | ❌ | ❌ | ✅ |
| Inscribirse / cancelar su inscripción | ❌ | ✅ | ❌ | ✅ |
| Ver **todas** las inscripciones y su detalle | ❌ | ❌ | ✅ | ✅ |
| Ver sus propias inscripciones | ❌ | ✅ | ✅ | ✅ |
| Registrar equipo | ❌ | ✅ | ❌ | ✅ |
| Listar todos los equipos | ❌ | ❌ | ❌ | ✅ |
| CRUD de usuarios | ❌ | ❌ | ❌ | ✅ |

## Endpoints (todos bajo `/api/v1`)
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | /auth/register | Público | Registra un CLIENTE y devuelve tokens |
| POST | /auth/login | Público | Devuelve accessToken (15 min) y refreshToken (7 días) |
| POST | /auth/refresh | Público | Renueva el par de tokens con un refreshToken válido |
| GET | /auth/me | Autenticado | Usuario de la sesión (para restaurar sesión al recargar) |
| GET | /torneos, /torneos/{id} | Público | Listado y detalle |
| POST / PUT / DELETE | /torneos, /torneos/{id} | ADMINISTRADOR | CRUD de torneos |
| GET | /juegos, /juegos/{id} | Público | Catálogo de juegos |
| POST / PUT / DELETE | /juegos, /juegos/{id} | ADMINISTRADOR | CRUD de juegos |
| POST | /inscripciones | CLIENTE, ADMINISTRADOR | Inscribe al usuario autenticado |
| GET | /inscripciones | ADMINISTRADOR, VENDEDOR | Todas las inscripciones |
| GET | /inscripciones/mias | Autenticado | Inscripciones propias |
| GET | /inscripciones/{id} | Interno o dueño | Detalle |
| DELETE | /inscripciones/{id} | Dueño o ADMINISTRADOR | Cancela y libera el cupo |
| POST | /equipos | CLIENTE, ADMINISTRADOR | Registra equipo (el usuario queda como capitán) |
| GET | /equipos | ADMINISTRADOR | Todos los equipos |
| GET | /equipos/mios | Autenticado | Equipos propios |
| GET / POST / PUT / DELETE | /usuarios, /usuarios/{id} | ADMINISTRADOR | CRUD de usuarios |

## Lógica de negocio
- **Inscripción** (`InscripcionService`): el torneo debe estar *Inscripciones Abiertas*, no haber pasado su fecha de cierre, tener cupos y el usuario no puede estar ya inscrito. Al inscribir se ocupa un cupo; al cancelar se libera. `@Version` en `Torneo` evita sobrepasar cupos con peticiones simultáneas.
- **Equipos**: se exige el mínimo de integrantes del juego (5 en LoL/Valorant/CS2, 3 en Rocket League). El PIN se guarda con BCrypt y nunca se devuelve.
- **Torneos**: no se pueden eliminar con inscripciones ni dejar cupos máximos por debajo de los ocupados.
- **Usuarios**: el registro público siempre crea CLIENTE; un administrador no puede eliminarse ni quitarse el rol a sí mismo.

## Seguridad (JWT)
1. `POST /auth/login` valida credenciales (BCrypt) y devuelve dos tokens firmados (HMAC-SHA). El claim `tipo` distingue `access` de `refresh`: un refresh token **no** sirve para llamar a la API.
2. `JwtAuthenticationFilter` lee `Authorization: Bearer <token>`, valida firma y expiración, y carga el usuario (si fue desactivado, deja de tener acceso).
3. `@PreAuthorize` en los controladores restringe por rol. Sin token → **401**; con token pero sin permiso → **403**.
4. Sesión sin estado (`STATELESS`), CSRF desactivado (no se usan cookies), CORS limitado al origen del frontend (`CORS_ORIGINS`, por defecto `http://localhost:5173`).

## Formato de errores
```json
{ "timestamp": "...", "status": 409, "error": "Conflict", "mensaje": "El torneo \"X\" no tiene cupos disponibles", "detalles": [] }
```
400 validación · 401 sin sesión · 403 sin permiso · 404 no existe · 409 regla de negocio.

## Pruebas
```bash
mvn test
```
- `InscripcionServiceTest`: lógica de negocio con repositorios simulados (Mockito).
- `JwtServiceTest`: generación, expiración y manipulación de tokens.
- `SeguridadIntegracionTest`: aplicación completa con H2; verifica 401/403 por rol y el flujo login → refresh.
