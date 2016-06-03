# ScalaJS workshop step 1. Backend setup.

This step ensures you have a working back-end ready to serve photos for the front-end we are going to develop.

* Get Flickr API key https://www.flickr.com/services/apps/create/noncommercial/
* Put the obtained application key in `secret.conf` under the key `flickr-key`
* Check that the photo API works:
```
sbt run

curl -i -X POST \
   -H "Content-Type:application/json" \
   -d \
'{ "latitude": 56.948889, "longitude":24.106389}' \
 'http://127.0.0.1:9000/photos'
```

