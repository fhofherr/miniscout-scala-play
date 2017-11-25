# miniscout

`miniscout` is a demo application implementing an web service that
allows to post adverts for new and used cars.

## Development

`miniscout` uses [SBT](http://www.scala-sbt.org/).

To run the tests execute:

```bash
sbt test
```

To run the application execute:

```bash
sbt run
```

Since this is a demo application it uses an in-memory database. All data
is lost on application shut down.

## API Examples

### Create a new car advert

```bash
curl -H "Content-Type: application/json" \
     -X POST -d @./example_data/new_car_advert.json \
     http://localhost:9000
```

```json
{
    "id":"f87bc3c6-2f97-4c9f-bab6-51b67e01c5d5",
    "title":"new car",
    "fuel":"Gasoline",
    "price":20349,
    "new":true
}
```

### Get a car advert

```bash
curl -H "Content-Type: application/json" \
     -X GET http://localhost:9000/f87bc3c6-2f97-4c9f-bab6-51b67e01c5d5
```

```json
{
  "id": "f87bc3c6-2f97-4c9f-bab6-51b67e01c5d5",
  "title": "new car",
  "fuel": "Gasoline",
  "price": 20349,
  "new": true
}
```

### Update a car advert

```bash
curl -H "Content-Type: application/json" -X PUT \
     -d @./example_data/update_car_advert.json \
     http://localhost:9000/f87bc3c6-2f97-4c9f-bab6-51b67e01c5d5
```

```json
{
  "id": "f87bc3c6-2f97-4c9f-bab6-51b67e01c5d5",
  "title": "updated car",
  "fuel": "Gasoline",
  "price": 20349,
  "new": true
}
```

### Delete a car advert

```bash
curl -H "Content-Type: application/json" -X DELETE \
     http://localhost:9000/f87bc3c6-2f97-4c9f-bab6-51b67e01c5d5
```
