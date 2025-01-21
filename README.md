### How to connect to DB:

1. Go into Docker container
2. execute "psql DB_NAME DB_USERNAME"
3. You are ready to execute SQLs

### Helpful SQLs
Show all existing tables:
SELECT table_name FROM information_schema WHERE table_name = 'public'

### ENV-Datei nutzen
siehe Repo: https://github.com/cdimascio/dotenv-kotlin
