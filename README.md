# ScalaJS workshop step 1. Backend setup.

* Get Flickr API key https://www.flickr.com/services/apps/create/noncommercial/
* Put it in `secret.conf`
* Check everything works:
```
sbt run

curl -i -X POST \
   -H "Content-Type:application/json" \
   -d \
'{ "latitude": 56.948889, "longitude":24.106389}' \
 'http://127.0.0.1:9000/photos'
```

