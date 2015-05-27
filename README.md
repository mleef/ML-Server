# ML-Server

A RESTful API for the construction, querying, and storing of various Machine Learning models. Currently supports Decision Tree with Perceptron and Naive Bayes models being currently added.

## Usage

### Registration

To register a new user, send a JSON object with the following attributes:

```json
{"username":"newuser", "password":"newuserpassword"}
```

to the following path:

```
http://localhost:4567/user/register
```

replacing the host name and port number with your system's configuration.

The user will recieve an error if the username is already taken.

### Login

To login a user and generate an API token, send a JSON object with the following attributes:

```json
{"username":"newuser", "password":"newuserpassword"}
```


to the following path:

```
http://localhost:4567/user/login
```

replacing the host name and port number with your system's configuration.

The user will receive a response with an API access token that must be included in all future requests. By default, the token's expire after 24 hours.


### Construction

To construct a model (currently only decision trees are supported), send a JSON object with the following attributes:

```json
{"name" : "Test Tree", "attributes" : ["x"," y", "z"], "examples" : ["1,0,0", "1,1,1"], "token":"ec34f0e91538478f9f7feb01dec5210b"}
```

to the following path:

```
http://localhost:4567/build/decision-tree
```

replacing the host name and port number with your system's configuration.

With the name, attributes, and examples replaced with whatever the user wants to use to build their model. Also the token must be replaced with a valid API token. Currently on boolean valued variables are supported. The user will receive a success response if the model has been constructed correctly, and an error if the input was malformed or some other problem arose.

### Querying

To query a model that has been previously created, send a JSON object with the following attributes:

```json
{"treename":"Test Tree", "examples" : ["1,0,0", "1,1,1"], "token":"5e1cae483e204879b5c6c3698909368b"}
```

to the following path:

```
http://localhost:4567/query/decision-tree
```

replacing the host name and port number with your system's configuration.

Also the token must be replaced with a valid API token. The user will receive the model's classification results on the provided samples, and a failure if the data is malformed or another error occured.

## Build

Instructions forthcoming...

## Dependencies

* Java 8 or above
* Maven
* MySQL

