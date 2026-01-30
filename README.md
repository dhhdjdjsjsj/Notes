# Notes (Android "Блокнот")

Современное Android-приложение для заметок на Kotlin с Jetpack Compose, Material 3 и архитектурой MVVM.

## Возможности
- Список заметок, поиск и сортировка по дате.
- Создание, редактирование и удаление заметок.
- Автосохранение во время ввода.
- Material You, поддержка светлой/тёмной темы.

## Структура проекта
```
app/src/main/java/com/example/notes/
├── data/
│   ├── dao/          # Room DAO
│   ├── database/     # Room Database
│   ├── entity/       # Room Entity
│   └── NoteRepository.kt
├── theme/            # Material 3 тема
├── ui/
│   ├── components/   # Переиспользуемые компоненты
│   └── screens/      # Экраны приложения
├── viewmodel/        # MVVM ViewModel
└── MainActivity.kt
```

## Запуск
1. Откройте проект в Android Studio.
2. Дождитесь синхронизации Gradle.
3. Запустите конфигурацию **app** на эмуляторе или устройстве.

Для проверки сборки из терминала:
```
gradle assembleDebug
```
