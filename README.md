# Ryver Bank Auth

The Ryver Bank authentication service handles all authentication and authorization responsibilities for the Ryver Bank API.

The microservices use JWTs for authentication and authorization.

## JWT claims format

The generated JWT has the following claims:

```json
{
    "sub": "manager_1",
    "uid": 1,
    "auth": "ROLE_MANAGER",
    "exp": 1601958943
}
```

### `sub`

The subject claim holds the username of the user.

### `uid`

The unique ID claim holds the unique ID of the user in the database. This claim should be used to verify the identity of the user.

### `auth`

The authorities claim holds a comma-separated String representing the different granted authorities for the user.

### `exp`

The expiry of the token signifies till when the token should be considered valid in seconds since Unix Epoch.

Beyond this time, the token should be invalidated.

## Parsing a JWT

JWTs are parsed using the `com.auth0.java-jwt` package.

```xml
<dependency>
  <groupId>com.auth0</groupId>
  <artifactId>java-jwt</artifactId>
  <version>3.10.3</version>
</dependency>
```

To view example code for how the token is parsed, view `cs203t10.ryver.auth.security.JWTAuthorizationFilter`.

The token is first extracted from the Authorization header, and decoded with `JWT.build().verify()`.

```java
DecodedJWT jwt = JWT
        .require(HMAC512(SECRET.getBytes()))
        .build()
        .verify(token.replace(TOKEN_PREFIX, ""));
```

> `TOKEN_PREFIX` is simply the `"Bearer "` portion of the Authorization header.

Once decoded, the claims of the JWT can be examined.

```java
final Long uid = jwt.getClaim(UID_KEY).asLong();
```

