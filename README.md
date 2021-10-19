# GeoService
This is a small service handling following set of operations with geo marks:
* CRUD operations for marks (users allowed to have only one mark)
* Check operation to determine if user's current location is near  their mark
* Operation for retrieving a number of users having their marks in specified cell

### API
* `GET: /` — index method, prints API
* `GET: /marks/<user_id>/near?lon=[value]&lat=[value]` — check if user is near their mark using Haversine formula
* `POST: /marks` — creates new mark (a.k.a new user)
* `PUT: /marks/<user_id>` — updates user's mark
* `DELETE: /marks/<user_id>` — deletes user's mark
* `PUT: /web/cells/<tileX>/<tileY>?distanceError=[value]` — puts a cell into the geo web

### POST/PUT Data examples
* For user marks:
```json
{
  "lon": 60.12,
  "lat": 61.41
}
```
