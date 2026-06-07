# EventFlow

EventFlow is a full-stack event management web app built with Spring Boot, Angular, PostgreSQL, JWT authentication, and Cloudinary flyer uploads.

The app lets users discover events, view full event details, join or cancel registrations, create and manage events, move events to draft/cancelled/completed states, and use an organizer dashboard.

## Features

- Angular web app with landing, events workspace, about, contact, profile menu, and footer
- User registration and login
- JWT access token + refresh token flow
- Role-based access with `USER` and `ADMIN`
- Event creation, update, draft, publish, cancel, and completed/full state
- Join/cancel registration flow
- Organizer dashboard with created events and participant counts
- Cloudinary unsigned upload for event flyers
- Input validation and basic input sanitization
- Basic in-memory API rate limiting
- PostgreSQL persistence

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Angular
- Cloudinary unsigned uploads
- Maven

## Requirements

- JDK 17
- PostgreSQL database
- Node.js and npm for the Angular frontend
- Cloudinary account if you want image upload for flyers

For production, yes: you need a PostgreSQL database server or a hosted PostgreSQL platform. Locally you can run PostgreSQL on your machine. Online, use a managed PostgreSQL service from your hosting provider or a dedicated Postgres platform.

## Environment Variables

The backend no longer stores database credentials or JWT secrets directly in `application.yaml`. Set these variables before running the API.

### Local PowerShell

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/eventflow"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="change-me-local"
$env:JWT_SECRET="replace-with-a-long-random-secret-at-least-32-chars"
$env:JPA_DDL_AUTO="update"
$env:CORS_ALLOWED_ORIGINS="http://localhost:4200,http://127.0.0.1:4200"
```

Then run the backend in the same terminal:

```powershell
.\mvnw.cmd spring-boot:run
```

### IntelliJ IDEA

Open:

```text
Run > Edit Configurations > Environment variables
```

Add:

```text
DB_URL=jdbc:postgresql://localhost:5432/eventflow
DB_USERNAME=postgres
DB_PASSWORD=change-me-local
JWT_SECRET=replace-with-a-long-random-secret-at-least-32-chars
JPA_DDL_AUTO=update
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://127.0.0.1:4200
```

### Production

Use real production values:

```text
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=<database-user>
DB_PASSWORD=<database-password>
JWT_SECRET=<long-random-secret>
JWT_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000
JPA_DDL_AUTO=validate
CORS_ALLOWED_ORIGINS=https://your-domain.com
APP_LOG_LEVEL=INFO
```

Do not commit real secrets to GitHub.

## Backend

Run:

```powershell
.\mvnw.cmd spring-boot:run
```

Build:

```powershell
.\mvnw.cmd -DskipTests package
```

Compile check:

```powershell
.\mvnw.cmd -DskipTests compile
```

## Frontend

The Angular app is in `frontend`.

Install dependencies:

```powershell
cd frontend
npm install
```

Run dev server:

```powershell
npm start
```

If `ng` is not installed globally, use:

```powershell
.\node_modules\.bin\ng.cmd serve
```

Open:

```text
http://localhost:4200
```

Build:

```powershell
npm run build
```

## Cloudinary Flyer Upload

The frontend uploads flyers directly to Cloudinary using an unsigned upload preset.

Configure:

```text
frontend/src/environments/environment.ts
frontend/src/environments/environment.prod.ts
```

Example:

```ts
cloudinary: {
  cloudName: 'your-cloud-name',
  uploadPreset: 'your-unsigned-upload-preset'
}
```

Cloudinary preset requirements:

- Signing mode: `Unsigned`
- Resource type: `Image` or `Auto`
- Allow image formats such as `jpg`, `jpeg`, `png`, `webp`
- Prefer a max file size limit
- Configure the folder in the Cloudinary preset, not from Angular

Never put your Cloudinary API secret in Angular.

## Authentication

Login/register responses return:

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

The Angular app stores both tokens and automatically calls:

```text
POST /api/v1/refresh-token
```

when an authenticated API call returns `401`.

Protected API calls use:

```text
Authorization: Bearer <accessToken>
```

## Main API Endpoints

Auth:

- `POST /api/v1/register`
- `POST /api/v1/login`
- `POST /api/v1/refresh-token`
- `POST /api/v1/admin/login`

User:

- `GET /api/v1/me`
- `PUT /api/v1/me`
- `POST /api/v1/me/change-password`

Events:

- `GET /api/v1/events`
- `GET /api/v1/events/{id}`
- `GET /api/v1/events/my-created`
- `GET /api/v1/events/my-joined`
- `POST /api/v1/events`
- `PUT /api/v1/events/{id}`
- `POST /api/v1/events/{id}/draft`
- `POST /api/v1/events/{id}/publish`
- `POST /api/v1/events/{id}/cancel`

Registrations:

- `GET /api/v1/registrations/my`
- `GET /api/v1/registrations/my-created-events`
- `POST /api/v1/registrations`
- `POST /api/v1/registrations/{id}/cancel`

Admin:

- `GET /api/v1/admin/users`
- `PUT /api/v1/admin/users/{id}/role`
- `DELETE /api/v1/admin/users/{id}`
- `GET /api/v1/admin/events`
- `GET /api/v1/admin/registrations`

Admin registration is not public. Create the first admin manually in the database or with a protected seed script.

## Security Notes

Implemented:

- BCrypt password hashing
- Stateless JWT access tokens
- Refresh token endpoint
- Role-based admin routes
- Owner checks for event management
- Basic rate limiting
- DTO validation
- Basic input sanitization before persistence
- Generic 500 error message
- Configured CORS origins

Before production:

- Use HTTPS
- Use a managed PostgreSQL database or secured PostgreSQL server
- Use a strong `JWT_SECRET`
- Set `JPA_DDL_AUTO=validate`
- Keep `CORS_ALLOWED_ORIGINS` restricted to your frontend domain
- Create the first admin securely
- Consider moving tokens to secure HttpOnly cookies for stronger XSS resistance
- Consider external rate limiting with Cloudflare, Nginx, or your platform
- Add real migrations with Flyway or Liquibase

## Deployment Notes

You need:

- Backend host for Spring Boot
- Frontend host for Angular static files
- PostgreSQL database server/platform
- Cloudinary account for flyer uploads

Typical setup:

- Angular deployed as static site
- Spring Boot deployed as web service/API
- PostgreSQL deployed as managed database
- Environment variables configured in the hosting dashboard

## Deploying This Monorepo

This repository contains both apps:

```text
EventFlow/
  src/        Spring Boot backend
  pom.xml    Backend build
  frontend/  Angular frontend
```

That is fine. You deploy the same GitHub repository twice, but each platform uses a different root directory.

### 1. Deploy Backend on Render

Create a Render Web Service from this GitHub repository.

Render settings:

```text
Root Directory: leave empty
Runtime: Java
Build Command: ./mvnw -DskipTests package
Start Command: java -jar target/EventFlow-0.0.1-SNAPSHOT.jar
```

On Windows locally you use `mvnw.cmd`, but Render/Linux uses `./mvnw`.

Add a Render PostgreSQL database, then set backend environment variables:

```text
DB_URL=jdbc:postgresql://<render-postgres-host>:5432/<database>
DB_USERNAME=<render-postgres-user>
DB_PASSWORD=<render-postgres-password>
JWT_SECRET=<long-random-secret>
JPA_DDL_AUTO=update
CORS_ALLOWED_ORIGINS=https://your-vercel-domain.vercel.app
```

For first deployment, `JPA_DDL_AUTO=update` is convenient. Later, use migrations and switch to `validate`.

After deploy, Render gives you an API URL like:

```text
https://eventflow-api.onrender.com
```

### 2. Configure Angular API URL for Vercel

The Angular production environment is generated at build time from Vercel environment variables.

In Vercel, add:

```text
API_BASE_URL=https://eventflow-api.onrender.com
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_UPLOAD_PRESET=eventflow
```

Use your real Render backend URL for `API_BASE_URL`.

### 3. Deploy Frontend on Vercel

Create a Vercel project from the same GitHub repository.

Vercel settings:

```text
Root Directory: frontend
Framework Preset: Angular
Install Command: npm install
Build Command: npm run build
Output Directory: dist/eventflow-web/browser
```

If you use the generated production environment, set:

```text
Build Command: npm run build:prod
```

The file `frontend/vercel.json` rewrites browser routes to `index.html`, so refreshing `/events`, `/about`, or `/contact` works.

### 4. Update CORS After Vercel Deploy

Once Vercel gives you the frontend URL, update Render:

```text
CORS_ALLOWED_ORIGINS=https://your-vercel-domain.vercel.app
```

Then redeploy/restart the backend.

### Deployment Order

Recommended order:

1. Create Render PostgreSQL.
2. Deploy Spring Boot backend on Render.
3. Add Render backend URL to Vercel as `API_BASE_URL`.
4. Deploy Angular frontend on Vercel with root directory `frontend`.
5. Copy Vercel frontend URL into Render `CORS_ALLOWED_ORIGINS`.
6. Test login, event list, Cloudinary upload, and registration flow.

## GitHub Safety Checklist

Before pushing:

- Do not commit `frontend/node_modules`
- Do not commit `frontend/dist`
- Do not commit `.env` files
- Do not commit real database credentials
- Do not commit real production JWT secrets
- Make sure `application.yaml` uses environment variables

## Project Structure

- `src/main/java/com/eventflow/eventflow/controller` - REST controllers
- `src/main/java/com/eventflow/eventflow/service` - business logic
- `src/main/java/com/eventflow/eventflow/repository` - Spring Data repositories
- `src/main/java/com/eventflow/eventflow/model` - JPA entities
- `src/main/java/com/eventflow/eventflow/dto` - request/response DTOs
- `src/main/resources/application.yaml` - environment-based backend config
- `frontend` - Angular web app

## License

No license file is included yet. Add one before publishing if you want clear reuse terms.
