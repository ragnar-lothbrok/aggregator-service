# aggregator-service
This will call user service for authentication.

This is aggregator service. All calls to differenbt services will route thorugh this class.

Here we will be calling login-service from this class.

Stack : Netflix-OSS

Login URL :  "http://localhost:8082/cart/login";

Configurations : 
application.properties
spring.application.name=aggregator-service
spring.freemarker.enabled=false
health.hystrix.enabled=true
spring.aop.proxy-target-class=true
user.api.url=http://user-login
redis.host-name=localhost
redis.port=6379
user.login.uri=http://localhost:8082/cart/login
url.social.token.name=X-SOCIAL-TOKEN
url.social.token.type=X-SOCIAL-TYPE
url.getUserInformation.headername=Login-Token
httpclient.timeout=10000
aggregator.redis=true
redis.sentinels.uri=ip1~26379,ip2~26379,ip3~26379
redis.env=statging
server.tomcat.basedir=/var/log/aggregator
server.tomcat.accessLogEnabled=true
server.tomcat.accessLogPattern=%h %p %t "%r" %s %b


application.yml
security:
  user:
    password: none
  ignored: /favicon.ico,/index.html,/home.html,/dashboard.html,/js/**,/css/**,/webjars/**
  sessions: ALWAYS
  oauth2:
    client:
      accessTokenUri: http://localhost:8081/uaa/oauth/token
      userAuthorizationUri: http://localhost:8081/uaa/oauth/authorize
      clientId: acme
      clientSecret: acmesecret
    sso:
      loginPath: /cart/login
    resource:
      jwt:
        keyValue: |
          -----BEGIN PUBLIC KEY-----
          MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnGp/Q5lh0P8nPL21oMMrt2RrkT9AW5jgYwLfSUnJVc9G6uR3cXRRDCjHqWU5WYwivcF180A6CWp/ireQFFBNowgc5XaA0kPpzEtgsA5YsNX7iSnUibB004iBTfU9hZ2Rbsc8cWqynT0RyN4TP1RYVSeVKvMQk4GT1r7JCEC+TNu1ELmbNwMQyzKjsfBXyIOCFU/E94ktvsTZUHF4Oq44DBylCDsS1k7/sfZC2G5EU7Oz0mhG8+Uz6MSEQHtoIi6mc8u64Rwi3Z3tscuWG2ShtsUFuNSAFNkY7LkLn+/hxLCu2bNISMaESa8dG22CIMuIeRLVcAmEWEWH5EEforTg+QIDAQAB
          -----END PUBLIC KEY-----
      id: openid
      serviceId: ${PREFIX:}resource
      userInfoUri: http://localhost:8081/uaa/user
      preferTokenInfo: false

