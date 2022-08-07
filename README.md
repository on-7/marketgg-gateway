# Market GG Gateway

Market GG 게이트웨이는 각 서비스 또는 데이터와 접속하고 API 호출을 위한 관리, 모니터링, 인증 및 일반 엑세스 제어를 담당하는 API 관리 시스템입니다.

## Motivation

여러 클라이언트가 여러 개의 서버 서비스를 각각 호출하게 된다면 매우 복잡한 호출 관계가 만들어집니다.
이러한 복잡성을 통제하기 위한 방법으로 서비스 단일 진입점을 만들어 놓았습니다.
다른 유형의 클라이언트에게 서로 다른 API 조합을 제공할 수도 있는 확장성을 가지고, 각 서비스에 접근할 때 필요한 인증/인가 기능을 한 번에 처리할 수도 있습니다.
또한, 정상적으로 동작하던 서비스에 문제가 생겨 서비스 요청에 대한 응답 지연이 발생하면 정상적인 다른 서비스로 요청 경로를 변경하는 기능을 제공합니다.

## Getting Started

```bash
./mvnw spring-boot:run
```

## Features

### [@윤동열](https://github.com/eastheat10)

### [@이제훈](https://github.com/corock)

- 웹 서버(NGINX) 연동
  - 도메인 설정 및 서브 도메인 추가
  - Reverse Proxy 설정
- 각 마이크로서비스에 대한 URL Rewriting

## Project Architecture

![marketgg-architecture-v1-0-3](https://user-images.githubusercontent.com/38161720/183289639-452bfe2d-792e-4260-a10a-652a87b62928.png)

## Tech Stack

### Authentication

![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens&style=flat)

### Build Tool

![ApacheMaven](https://img.shields.io/badge/Maven-C71A36?style=flat&logo=ApacheMaven&logoColor=white)

### Database

![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=MySQL&logoColor=white)

### DevOps

![GitHubActions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=flat&logo=GitHubActions&logoColor=white)
![SonarQube](https://img.shields.io/badge/SonarQube-4E98CD?style=flat&logo=SonarQube&logoColor=white)

### Frameworks

![SpringBoot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=SpringBoot&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=flat&logo=Swagger&logoColor=white)

### Languages

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white&style=flat)

### Web Server

![NGINX](https://img.shields.io/badge/NGINX-009639?style=flat&logo=NGINX&logoColor=white)

## Contributors

<a href="https://github.com/nhn-on7/marketgg-gateway/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=nhn-on7/marketgg-gateway" />
</a>

## License

Market GG is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
