5(ранее 6-й) проект roadmap Cloud-storage

ссылка на проект
https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/

Приложение для хранения файлов

Возможности пользователя:

- Регистрация и авторизация
- Создание папок
- Перемещение папок
- Переименовывание папок
- Скачивание файлов
- Поиск нужных файлов
- Перемещение файлов
- Удаление файлов и папок

Стек:

- Java 25, Spring Boot 4, PostgreSQL, Liquibase, S3 MinIo, Redis, Docker, Swagger, React

Как поднять локально:

- Установить Docker и JDK 25

- Проверить актуальные версии в терминале: 

docker compose version
java -version


- Склонировать репозиторий

- создать .env используя example-env, значения должны совпадать с src/main/resources/application.properties

- Поднять докер в терминале

docker compose up -d postgres
docker compose up -d redis
docker compose up -d minio

- Поднять бек в терминале

./gradlew bootRun

- Поднять фронт в терминале

docker compose up -d --build frontend

- Чекнуть работу конейнеров

docker compose ps


Для проверки работы, в браузере на порту 8081 

http://localhost:8081

Бек на порту 8080 для проверки через Postman

http://localhost:8080

Сваггер

http://localhost:8080/swagger-ui/index.html

Эндпоинты согласно ТЗ