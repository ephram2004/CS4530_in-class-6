version: '3.8'

services:
  db:
    image: postgres:16
    container_name: project_postgres
    restart: unless-stopped
    env_file: .env
    volumes:
      - db_data:/var/lib/postgresql/data
      - ./init:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"

volumes:
  db_data: