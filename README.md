# GyoGemu
Row Counting Game Example

## Running Tests
```
cd server
mvn test
```
### Running Server
```
cd server
mvn spring-boot:run
```

### Running Game CLI
```
cd cli
mvn spring-boot:run
```
* Defaults to localhost:8080 but a different host can be passed from the command line.
* Multiple clients can be run in separate terminal on same machine for demo/test purposes.

## Project Layout

This project consists of the following modules
* rest service (server) module 
* client library (client) module
* common library (common) mudule
* terminal based client app (cli) module

Projects files for Intellij are also included

## Testing

* All testing is done on the server module by way of MVC service tests, application level unit tests which operate on the http endpoints.
* These tests were developed using a TDD approach creating failing tests first. 
* Class and line code coverage is almost 100%.
* Normally I'd add a few sanity journey style integration tests as well but left them out in this example.
* Also normally i'd add some testing on the client using a mock server but left this out and performed manual tessting instead.

## Assumptions
* Server does not use any storage, uses an in memory map at the DB layer instead to keep the example simple.
* There is no automatic removal of completed games.
* There is no authorization on API access for simplicity sake.
* The client code is a bit scrappy, focus was on the rest service and its tests mostly.

