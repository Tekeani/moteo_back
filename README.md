# moteo_back

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need
  to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Database Connection

The application is configured to connect to a Neon PostgreSQL database. The connection string is already set up in the `Database.kt` file:

```kotlin
private const val jdbcUrl = "jdbc:postgresql://ep-autumn-river-ab8ox58o-pooler.eu-west-2.aws.neon.tech/moteo?sslmode=require"
```

### Setting Up Database Credentials

Before running the application, you need to replace the placeholder username and password in the `Database.kt` file with your actual Neon database credentials:

1. Open `src/main/kotlin/Database.kt`
2. Replace `your_username` with your actual Neon database username
3. Replace `your_password` with your actual Neon database password

```kotlin
private const val username = "your_username" // Replace with your actual Neon database username
private const val password = "your_password" // Replace with your actual Neon database password
```

### Testing the Database Connection

Once you've set up your credentials, you can test the database connection by:

1. Running the application with `./gradlew run`
2. Accessing the `/db-test` endpoint in your browser or using a tool like curl or Postman

If the connection is successful, you should see a message like "Connected to PostgreSQL: [version]".

## Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                                        |
|------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [Server-Sent Events (SSE)](https://start.ktor.io/p/sse)                | Support for server push events                                                     |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Call Logging](https://start.ktor.io/p/call-logging)                   | Logs client requests                                                               |
| [Authentication](https://start.ktor.io/p/auth)                         | Provides extension point for handling the Authorization header                     |
| [Authentication JWT](https://start.ktor.io/p/auth-jwt)                 | Handles JSON Web Token (JWT) bearer authentication scheme                          |
| [Status Pages](https://start.ktor.io/p/status-pages)                   | Provides exception handling for routes                                             |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
|-------------------------------|----------------------------------------------------------------------|
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```
