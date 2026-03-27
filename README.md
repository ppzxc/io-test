# io-test

I/O 성능 테스트 CLI 도구

## 기술 스택

- Java 25, Spring Boot 4.0.3, Hibernate 7.2.4
- Picocli 4.7.6
- Virtual Threads (가상 스레드) 기반 동시성

## 빌드

```bash
./gradlew build
```

## 실행

### 인프라 실행

```bash
docker compose up -d
```

### CLI 사용법

```bash
java -jar build/libs/io-test-0.0.7.jar <command> [options]
```

### 명령어

#### `db` - Database I/O 테스트

```bash
java -jar io-test-0.0.7.jar db -c 1000 -t 4 -o all
```

| 옵션 | 기본값 | 설명 |
|------|--------|------|
| `-c, --count` | 1000 | 레코드 수 |
| `-t, --threads` | 1 | 동시 스레드 수 |
| `-o, --operation` | all | write, read, delete, all |
| `--subject-size` | 46 | subject 필드 크기(bytes) |
| `--body-size` | 2000 | body 필드 크기(bytes) |

#### `mq` - Message Queue I/O 테스트

```bash
java -jar io-test-0.0.7.jar mq -c 1000 -t 4 -o all
```

| 옵션 | 기본값 | 설명 |
|------|--------|------|
| `-c, --count` | 1000 | 메시지 수 |
| `-s, --size` | 1024 | 메시지 크기(bytes) |
| `-q, --queue` | mot-test-queue | 큐 이름 |
| `-t, --threads` | 1 | 동시 스레드 수 |
| `-o, --operation` | all | publish, consume, all |

#### `file` - File I/O 테스트

```bash
java -jar io-test-0.0.7.jar file -n 1000 -t 4 -o all
```

| 옵션 | 기본값 | 설명 |
|------|--------|------|
| `-n, --files` | 1000 | 파일 수 |
| `-s, --size` | 4096 | 파일 크기(bytes) |
| `-t, --threads` | 1 | 동시 스레드 수 |
| `-o, --operation` | all | write, read, all |
| `-d, --dir` | /tmp/mot-file-test | 테스트 디렉토리 |

## 환경 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `DB_HOST` | localhost | DB 호스트 |
| `DB_PORT` | 3306 | DB 포트 |
| `DB_NAME` | mydatabase | 데이터베이스 이름 |
| `DB_USER` | myuser | DB 사용자 |
| `DB_PASSWORD` | secret | DB 비밀번호 |
| `MQ_HOST` | localhost | MQ 호스트 |
| `MQ_PORT` | 5672 | MQ 포트 |
| `MQ_USER` | myuser | MQ 사용자 |
| `MQ_PASSWORD` | secret | MQ 비밀번호 |

## Docker로 실행

```bash
docker compose -f compose.yaml -f compose.tester.yaml run tester java -jar app.jar db -c 1000 -t 4
```
