## My offer service with akka Http

### run

```bash
sbt clean compile run
```

wait for `Service my-offer-service is running at 127.0.0.1:8080...` message 

(you can also run it in InteliJ)

### use

you can navigate to the following routes

[ping pong](http://127.0.0.1:8080/api/v1/ping)

[post an offer json](http://127.0.0.1:8080/api/v1/offers)

[get offer by id](http://127.0.0.1:8080/api/v1/offer_by_id/2)

[get offer by status](http://127.0.0.1:8080/api/v1/offers_by_status/active)

[delete an offer by id](http://127.0.0.1:8080/api/v1/offer/delete/2)
