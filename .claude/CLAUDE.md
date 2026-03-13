# io-test

## 프로젝트 개요
MariaDB와 RabbitMQ의 I/O 성능 테스트 CLI 도구

## 기술 스택
- **Java**: 25
- **Spring Boot**: 4.0.3
- **Spring Boot JPA** (Hibernate 7.2.4.Final)
- **Picocli**: CLI 인터페이스
- **Lombok**
- **실행 환경**: Docker 기반

## 테스트 대상

### MariaDB 11.4
- 물리서버 2대 replication 구성
- DB 스키마: `src/main/resources/mo_scheme.sql`
- 주요 테이블: `mo`, `mt`, `attachment`, `acl`, `acl_ip`, `acl_list`, `push`, `user`, `stat_*`
- JDBC 드라이버: `org.mariadb.jdbc:mariadb-java-client`

### RabbitMQ 4.2.4
- localhost에서 동작
- Spring AMQP 사용

## 동작 방식
- CLI 입력을 받아 테스트 실행 후 결과 출력하고 종료 (one-shot)
- picocli로 커맨드/옵션 파싱

## 빌드 & 실행
```bash
./gradlew build
docker compose up
```

## 프로젝트 구조
```
src/main/java/kr/nanoit/mot/tester/   # 메인 소스
src/main/resources/
  application.yaml                      # Spring 설정
  mo_scheme.sql                         # DB 테이블 스키마
src/test/java/                          # 테스트
```
