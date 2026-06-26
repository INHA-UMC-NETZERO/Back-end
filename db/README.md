# DB schema guide

The application targets PostgreSQL and can create tables from JPA entities when:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create
```

Before starting the application, the target database and user must already exist.

The embedding migration runs automatically from:

```text
src/main/resources/db/embedding_migration.sql
```

Because the migration uses the PostgreSQL `vector` type, create the pgvector
extension once before starting the app:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

On RDS, this may require the master user / `rds_superuser`.

## RDS connection

- Host: `inhabada-identifier.c054eoe2obxd.us-east-1.rds.amazonaws.com`
- Port: `5432`
- DB: `postgres`
- User: `inhabada`

Pass the password through an environment variable. Do not commit database
passwords to git.

```bash
SPRING_DATASOURCE_URL='jdbc:postgresql://inhabada-identifier.c054eoe2obxd.us-east-1.rds.amazonaws.com:5432/postgres?sslmode=require' \
SPRING_DATASOURCE_USERNAME='inhabada' \
SPRING_DATASOURCE_PASSWORD='<rds-password>' \
java -jar inha-bada-0.0.1-SNAPSHOT.jar
```

## Manual schema option

If you later switch back to `ddl-auto: validate`, apply `schema.sql` to the
database before starting the app:

```bash
PGPASSWORD='<rds-password>' psql \
  "host=inhabada-identifier.c054eoe2obxd.us-east-1.rds.amazonaws.com port=5432 dbname=postgres user=inhabada sslmode=require" \
  -v ON_ERROR_STOP=1 -f schema.sql
```

Expected tables include:

- `users`
- `posts`
- `slots`
- `share_requests`
- `notifications`
- `keyword_subscriptions`
- `sessions`
