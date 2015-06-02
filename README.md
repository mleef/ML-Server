# ML-Server

A RESTful API for the construction, querying, and storing of various Machine Learning models. Currently supports Decision Tree with Perceptron and Naive Bayes models being currently added.

## Installation

Coming soon...

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

Sample successful response:

```json
{"key":"8358928a59d4481882cf25d032853c87","expires":"2015-06-02 16:07:43.627"}
```

Sample response if username/password combo is wrong, user does not exist, or user is already logged in:

```json
{"status":"Error","message":"Incorrect username/password combination or still active token."}
```


### Construction

To construct a model, send a JSON object with the following attributes:

```json
{"model-name" : "Test Tree", "attributes" : ["x"," y", "z"], "examples" : ["1,0,0", "1,1,1"], "token":"ec34f0e91538478f9f7feb01dec5210b"}
```

to the following path:

```
http://localhost:4567/build/decision-tree
```

replacing the host name and port number with your system's configuration. NOTE: The last variable in the list is assumed to be the class variable.

Sample successful response:

```json
{"status":"Success","message":"Tree built successfully."}
```

Sample response if construction fails for some reason, will contain info about the failure:

```json
{"status":"Error","message":"Tree name already exists for this account. Choose another name for your model."}
```

### Querying

To query a model that has been previously created, send a JSON object with the following attributes:

```json
{"model-name":"Test Tree", "examples" : ["1,0,0", "1,1,1"], "token":"5e1cae483e204879b5c6c3698909368b"}
```

to the following path:

```
http://localhost:4567/query/decision-tree
```

replacing the host name and port number with your system's configuration.

A successful response will produce labels for the inputted test samples:

```json
{"results":["0","1"]}
```

and an error response for malformed input:

```json
{"status":"Error","message":"Non integer values detected in test samples."}
```

## Helper Scripts

To set up the necessary MySQL tables and schemas, use the attached setup.sql script in the /scripts folder like so on your MySQL Server Instance:

```
/usr/local/mysql-5.6.24-osx10.8-x86_64/bin/mysql -u root < setup.sql
```
To convert existing data in .csv format into JSON ready to be usable by the api, use the CSVtoJSON.py script in the /scripts folder like so:

```
python CSVtoJSON.py [csv data] > [json data output]
```

## Dependencies

* Java 8 or above
* Maven
* MySQL

