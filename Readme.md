# Parking

Android-приложение для автоматической парковки

## Принцип работы

1. При запуске приложения грузится список парковок и записывается в Store.
2. ParkingsPresenter выводит парковки из Store во view (ParkingsFragment) и подписывается на изменения Store.
3. Одновременно с этим запускается LocationUpdatesService, который отслеживает изменение координат устройства и передает их CoordinatesHandleWorker'у.
4. CoordinatesHandleWorker записывает координаты в Store и передает их ParkingWorker'у.
5. В случае необходимости ParkingWorker запускает или завершает парковку (см. блок-схему парковки).

## Блок-схема парковки

![Блок-схема парковки](https://i.imgur.com/1QdmZNH.png)

## Тестирование

Протестирован метод, проверяющий, попадают ли текущие координаты устройства внутрь полигона, представляющего собой границы парковки. Для этого написано 2 тестовых метода:

```kotlin
fun `Should be in the polygon`()
```

```kotlin
fun `Should not be in the polygon`()
```
