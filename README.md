### How to connect to DB:

1. Go into Docker container
2. execute "psql mydb my_user"
3. You are ready to execute SQLs

### Helpful SQLs
Show all existing tables:
SELECT table_name FROM information_schema WHERE table_name = 'public'
