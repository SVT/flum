# FluM - Fluent Mock Web Server

A wrapper around the [okhttp mockwebserver](https://github.com/square/okhttp/tree/master/mockwebserver)
that adds a more fluent api for scripting the mock server. The design goal has been to allow an api
for defining expected requests and corresponding responses that gives fluent and easy to read code.
A major point is that the api allows defining the expected request, the corresponding response,
and verification to do on the received request at the end of the test, all at the same point.

This is a work in progress, the available features, allthough they should cover most cases,
are based on what has been needed for the authors. Suggestions on features to add are much
welcome, see [CONTRIBUTING](#getting-involved)
 
Project is built in kotlin. Compatible with java 11.

Junit 5.5 or newer is needed to use the junit5 extension.

Jar is available from jcenter. Use in gradle like below.

```compile 'se.svt.oss:flum:x.y.z'```

## TODO
- Verification of binary body

## Dependencies

Kotlin

## Basic Usage
```
class ExampleTest {

    val flum = Flum().apply {
        start()
    }

    @AfterEach
    fun tearDown() {
        try {
            flum.verify()
        } finally {
            flum.shutdown()
        }
    }

    @Test
    fun testUsingFlum() {
        flum.expectGet("test request 1")
                .toPath("/myService")
                .thenRespond()
                .withStatus(200)
                .withBody("SUCCESS")
                .afterwardsVerifyRequest()
                .hasQueryParameter("myParam", "myValue")

        flum.expectPost("test request 2")
                .toPath("/myService")
                .thenRespond()
                .withStatus(202)
                .withBody("ALSO SUCCESS")
                .afterwardsVerifyRequest()
                .hasBody("Hello World!")
        ...
        // CALL PRODUCTION CODE THAT IS SUPPOSED TO EXECUTE THE EXPECTED REQUEST
        ...
    }
}
```
## Using with the junit5 extension
The `FlumExtension` will inject a Flum instance into any field of the correct type in the test instance. It will also
provide a Flum instance to any test methods that takes a paramter of type Flum. It will also verify and shutdow flum 
after each test method.

Note that if your test is using LifeCycle `PER_CLASS` injecting flum into a field in the test instance is not
 supported, instead use a test method parameter.

The port that flum uses will be selected by the below methods, in falling order of priority
1. From port parameter passed into the FlumExtension constructor
2. From field or method in testinstance annotated with `@FlumPort`
to determine the port
3. From method `getFlumPort` in testinstance
4. Random free port will be selected
```
@ExtendWith(value = [FlumExtension::class])
class ExampleTest {

    // Use the @FlumPort annotation to use specific port
    @FlumPort
    val flumPort = // Some custom logic to get port 

    // Flum will be injected here by extension
    lateinit var flum: Flum

    @Test
    fun testUsingFlum() {
        flum.expectGet("test request 1")
                .toPath("/myService")
                .thenRespond()
                .withStatus(200)
                .withBody("SUCCESS")
                .afterwardsVerifyRequest()
                .hasQueryParameter("myParam", "myValue")

        flum.expectPost("test request 2")
                .toPath("/myService")
                .thenRespond()
                .withStatus(202)
                .withBody("ALSO SUCCESS")
                .afterwardsVerifyRequest()
                .hasBody("Hello World!")
        ...
        // CALL PRODUCTION CODE THAT IS SUPPOSED TO EXECUTE THE EXPECTED REQUEST
        ...
    }
}
```

## How to test the software

```
./gradlew test
```


## Update version and release to jcenter/bintray (for maintainers)

1. Make sure you are on master branch and that everything is pushed to master
2. ./gradlew release to tag a new version (uses Axion release plugin - needs ssh key for repo)
3.  ./gradlew bintrayUpload to upload to repo - needs BINTRAY_KEY and BINTRAY_USER environment variables

## Known issues


## Getting help

If you have questions, concerns, bug reports, etc, please file an issue in this repository's Issue Tracker.

## Getting involved

This project is very much a work in progress so all kinds of feedback are welcome, bug reports, 
feature requests etc are welcome. Details on how to contribute can be found in [CONTRIBUTING](CONTRIBUTING.md).


----

## Open source licensing info

[LICENSE](LICENSE)

----

## Primary Maintainer

Gustav Grusell [https://github.com/grusell]

## Credits and references

[okhttp mockwebserver](https://github.com/square/okhttp/tree/master/mockwebserver)