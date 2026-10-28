# 우리의 기록

웹서비스설계 JWT 게시판 과제입니다. React 프론트엔드와 Spring Boot 백엔드, SQLite를 사용합니다.

## 작성자와 서비스 소개

- 학번 / 이름: 202212949 / 백경환
- 선택한 기술: Spring Boot / React
- 주요 기능: 공개 게시글 목록·상세 조회, JWT 로그인, 내 정보 조회, 글 작성, 작성자 본인의 글 수정·삭제, 로그아웃과 인증 오류 안내

‘우리의 기록’은 글을 읽고 자신의 기록을 남기려는 사용자를 위한 게시판입니다. 로그인하지 않아도 게시글 목록과 상세 내용을 조회할 수 있습니다. 로그인한 사용자는 글을 작성하고 자신이 작성한 글만 수정하거나 삭제할 수 있습니다. 서버가 JWT와 작성자 권한을 검사하며, 게시글은 SQLite 파일에 저장합니다.

## 보고서와 테스트 증거

- [구현 보고서 PDF](docs/report.pdf)
- [테스트 증거](docs/evidence/)

보고서 PDF를 위 경로에 추가하세요. 테스트 증거를 보고서에만 포함한 경우 별도 폴더 링크를 삭제하고 해당 보고서 페이지를 안내하세요. 경로를 바꿨다면 링크도 수정합니다. GitHub에서 링크가 실제로 열리는지 확인하세요.

## 프로젝트 구조

```text
.
├── README.md
├── .gitignore
├── frontend/          # 선택한 프론트 소스와 의존성 파일
├── backend/           # 선택한 백엔드 소스와 의존성 파일
└── docs/
    ├── report.pdf
    └── evidence/      # 별도 증거가 있을 때
```

[실제 제출 폴더 이름에 맞게 수정]

## 실행 환경

| 항목 | 사용 버전 |
|---|---|
| 운영체제 | Windows / PowerShell |
| 프론트 런타임 Node.js | 현재 확인된 버전 v24.15.0, 프로젝트 요구 버전 20.19.0 이상 |
| 백엔드 런타임 | JDK 17 이상 필요, pom.xml의 Java 기준 17 |
| 프레임워크 | React 19.3.0, Vite 8.3.4, Spring Boot 3.5.16 |
| JWT 라이브러리 | JJWT 0.13.0 |
| 데이터 저장 | SQLite, SQLite JDBC 3.51.3.0 |
| API 검사 도구 | Python 3, 표준 라이브러리 사용 |

프레임워크 버전은 package.json과 pom.xml 기준입니다. 설치된 JDK 버전과 새 폴더에서의 빌드 성공 여부는 별도 확인이 필요합니다.

## 설치와 실행

아래 명령은 Windows PowerShell 기준입니다. Node.js와 JDK를 설치하고 `node --version`, `java -version`으로 확인합니다. Maven Wrapper의 첫 실행에는 의존성 다운로드를 위한 인터넷 연결이 필요합니다.

### 1 저장소 복제

```powershell
git clone https://github.com/Dosan2883/wsd_assign2.git
cd wsd_assign2
```

### 2 백엔드 준비와 실행

저장소 루트에서 실행합니다. 새 무작위 키를 환경변수에 저장하며 실제 키를 문서나 소스 코드에 기록하지 않습니다.

```powershell
cd backend-spring
$env:JWT_SECRET = node -e "process.stdout.write(require('node:crypto').randomBytes(32).toString('hex'))"
$env:TOKEN_TTL = '90'
.\mvnw.cmd package
java -jar target/jwt-board-1.0.0.jar
```

- 환경설정 예시: `backend-spring/env.example.txt`
- 설정 연결: `backend-spring/src/main/resources/application.properties`. Spring은 `.env` 파일을 자동으로 읽지 않습니다.
- JWT_SECRET: 64자리 16진수 키가 필요하며, 누락·예시 값·잘못된 형식이면 서버 시작을 거부합니다. 새 터미널에서는 환경변수를 다시 설정합니다.
- TOKEN_TTL: 기본값 90초. 만료 검사 시 서버를 종료하고 `$env:TOKEN_TTL = '3'`으로 변경한 뒤 같은 실행 명령으로 재시작합니다. 검사 후 90초로 복원합니다.
- 백엔드 주소: http://127.0.0.1:8000
- 개발 실행: 백엔드 폴더에서 `.\mvnw.cmd spring-boot:run`

서버가 실행된 상태에서 다른 PowerShell 창으로 확인합니다.

```powershell
Invoke-RestMethod http://127.0.0.1:8000/api/health
```

정상 응답은 `status: ok`, `stack: spring`입니다.

### 3 프론트 빌드와 로컬 실행

백엔드를 켜 두고 새 터미널에서 저장소 루트로 이동한 뒤 실행합니다.

```powershell
cd frontend-react
npm ci
npm run build
npm run preview
```

- 최종 확인 주소: http://127.0.0.1:4173
- 개발 중 실행: `npm run dev`, http://127.0.0.1:5173
- API 연결: 브라우저의 `/api` 요청을 Vite가 `http://127.0.0.1:8000`으로 전달합니다. 개발용 `server.proxy`와 빌드 확인용 `preview.proxy`는 `frontend-react/vite.config.js`에 있습니다.
- 백엔드 포트 변경: 서버 시작 전에 `$env:PORT = '새 포트 번호'`를 설정하고 두 프록시의 대상 포트도 같은 값으로 수정합니다. 프론트 포트는 Vite의 `server.port`와 `preview.port`에서 변경합니다.
- 종료: 프론트와 백엔드를 실행한 각 터미널에서 Ctrl+C를 누릅니다.
- 코드 변경 후 재실행: 프론트는 종료 후 `npm run build`, `npm run preview`를 순서대로 실행합니다. 백엔드는 종료 후 `.\mvnw.cmd package`로 다시 빌드하고 같은 환경변수가 설정된 터미널에서 JAR를 재실행합니다.

### 4 테스트 계정과 DB

| 사용자 | 이메일 | 실습용 비밀번호 |
|---|---|---|
| Alice | alice@class.test | Classroom!2026 |
| Bob | bob@class.test | Classroom!2026 |

- 계정은 과제 전용 테스트 계정이며 `Users.java`에서 관리합니다.
- 위 실행 절차의 SQLite 파일 위치는 `backend-spring/data/board.sqlite`입니다. 실행 디렉터리를 기준으로 폴더·파일·posts 테이블을 자동 생성합니다.
- 경로 변경: 서버 시작 전 `$env:DB_FILE = '원하는 SQLite 파일 경로'`를 설정합니다.
- DB 초기화: 서버를 종료한 뒤 백엔드 폴더에서 `Remove-Item -LiteralPath .\data\board.sqlite`를 실행하고 재시작합니다. 기존 실습 게시글이 모두 삭제됩니다. DB_FILE을 변경했다면 해당 파일 경로를 사용합니다.
- 서버 재시작 후 데이터 유지: 파일 DB를 사용하는 구현은 확인했습니다. 실제 재시작 검증 결과는 아직 기록되지 않았습니다. 글 작성 후 서버를 재시작하고 같은 글의 내용과 작성자를 조회해 확인합니다.

## 구현 완료 범위

아래 완료 표시는 소스 코드 구현 기준입니다. 실제 실행 검증과 화면 증거 수집은 별도입니다. Java 파일은 `backend-spring/src/main/java/edu/classroom/board/`에 있습니다.

| 기능 | 완료 여부 | 주요 파일 또는 함수 |
|---|---|---|
| 공개 목록과 상세 | 구현 완료 | BoardController.list/get, PostStore.list/get, frontend-react/src/main.jsx |
| 로그인과 JWT 발급 | 구현 완료 | BoardController.login, Users.verify, AuthService.issueToken |
| JWT 검증과 내 정보 조회 | 구현 완료 | AuthService.currentUserId, BoardController.me |
| 글 작성과 작성자 저장 | 구현 완료 | BoardController.create, PostStore.create |
| 본인 글 수정·삭제 | 구현 완료 | BoardController.update/delete, AuthService.assertOwner, api.js의 canEdit |
| 토큰 만료와 로그아웃 | 구현 완료 | AuthService.currentUserId, api.js의 request/clearSession, main.jsx |
| 입력 오류와 권한 오류 처리 | 구현 완료 | BoardController.fields/postId, Errors, api.js의 errorMessage |

현재 제한: 사용자 계정은 고정된 테스트 계정이며 회원가입 기능은 없습니다. 로그아웃은 브라우저 메모리의 토큰을 지우지만 서버에서 이미 발급한 토큰을 즉시 폐기하지 않습니다. 복사된 토큰은 만료 전까지 사용할 수 있습니다. 만료 상태는 다음 API 요청의 401 응답으로 처리하며 자동 만료 타이머는 없습니다. 실행 중 발생하는 오류 유무는 테스트 결과로 추가 확인해야 합니다.

## 테스트 방법과 결과 요약

이번 README 보완에서는 소스 코드와 설정을 대조했습니다. API 검사, 브라우저 검사 및 DB 재시작 검사는 실행하지 않았으며 통과 결과로 기록하지 않습니다.

백엔드를 실행한 상태에서 저장소 루트의 새 터미널로 다음 명령을 실행합니다. Python 3가 필요합니다.

```powershell
python tools/check_api.py --base http://127.0.0.1:8000 --mode final
```

만료 검사는 백엔드를 TOKEN_TTL=3으로 재시작한 뒤 실행합니다.

```powershell
python tools/check_api.py --base http://127.0.0.1:8000 --mode final --expiry
```

검사 도구는 테스트 게시글을 생성·수정·삭제합니다. 실패 시 출력된 게시글 ID를 확인합니다. 자동 검사는 화면 동작과 서버 재시작 후 데이터 유지를 검증하지 않으므로 별도 수동 검사가 필요합니다.

| 확인 항목 | 코드 기준 기대 동작 | 실제 실행 결과 |
|---|---|---|
| Alice/Bob 권한 구분 | 다른 작성자의 글 수정·삭제는 403 FORBIDDEN, 원본 데이터 유지 | 미기록 |
| 정상 조회·로그인·수정 | 200 | 미기록 |
| 글 생성·삭제 | 201 및 Location, 204 및 빈 본문 | 미기록 |
| 잘못된 입력·계정·없는 글 | 400 BAD_INPUT, 401 INVALID_CREDENTIALS, 404 NOT_FOUND | 미기록 |
| 토큰 없음·변조·만료 | 401 TOKEN_REQUIRED / INVALID_TOKEN / TOKEN_EXPIRED | 미기록 |
| 보호 API의 인증 실패 | WWW-Authenticate: Bearer 응답 헤더 | 미기록 |
| 화면·새로고침·로그아웃·DB 유지 | 401 시 로그인 해제, 403 시 로그인 유지, 새로고침 시 메모리 세션 초기화, DB 글 유지 | 미기록 |

전체 수동 절차는 [T01~T17 시나리오](05_테스트시나리오.md)를 따릅니다. 실제 HTTP 상태, JSON code, 응답 헤더와 화면을 기록할 위치는 [보고서 작성본](04_보고서_작성본.docx)의 4절 ‘테스트 결과’ 및 후속 증거 절입니다.

## 핵심 구현 설명

1. 로그인: [BoardController.java](../backend-spring/src/main/java/edu/classroom/board/BoardController.java)의 `login`이 입력을 검사하고 `Users.verify`로 계정을 확인합니다. [AuthService.java](../backend-spring/src/main/java/edu/classroom/board/AuthService.java)의 `issueToken`이 사용자 ID인 sub, iss, aud, exp를 포함한 HS256 JWT를 발급합니다.
2. 보관과 전송: [api.js](../frontend-react/src/api.js)의 `login`이 토큰과 사용자 정보를 모듈 메모리에 저장합니다. `request`는 토큰이 있으면 Authorization: Bearer 헤더를 붙입니다. 새로고침과 로그아웃은 메모리의 로그인 상태를 초기화합니다.
3. 서버 검증: `AuthService.currentUserId`가 헤더 형식, 서명, HS256 알고리즘, 만료, issuer, audience, sub 형식 및 사용자 존재 여부를 검사합니다. 인증 실패는 401과 원인별 오류 code로 반환합니다.
4. 작성자 권한: 글 생성 시 서버가 검증한 사용자 ID를 작성자로 저장합니다. 수정·삭제 시 `assertOwner`가 인증 사용자와 DB의 authorId를 비교하여 불일치하면 403을 반환합니다. 요청 본문에 authorId를 추가하면 허용되지 않은 필드로 거부합니다. 화면의 수정·삭제 버튼도 [main.jsx](../frontend-react/src/main.jsx)와 `canEdit`으로 제한합니다.

## AI 활용 요약과 참고 자료

- AI 사용 여부: 사용
- 도구와 사용 목적: ChatGPT 6.0 Astra Medium, 설계 및 보고서 작성 / ChatGPT 6.0 Luna Extra High, 코드 작성
- 반영 위치와 직접 수정·검증한 내용: 보고서 설명 부분 / TODO에 맞는 코드 작성
- 상세 활용 기록: [보고서 해당 절]
- 참고한 수업 자료·외부 코드·공식 문서: [자료 제목, URL, 사용 위치]

AI 미사용이면 미사용을 명시하고 참고 자료만 기록하세요. 사용량 자체로 가감점하지 않으며 기록과 검증의 명확성을 평가합니다.

## 제출 확인

- [ ] README의 작성 안내를 현재 구현 내용과 확인 상태로 교체했다.
- [ ] 새 폴더에 clone하여 README만으로 설치·환경설정·빌드·로컬 실행했다.
- [ ] 4173번 빌드 화면에서 API 연결과 전체 필수 기능을 확인했다.
- [ ] 보고서 PDF와 테스트 증거를 추가하고 GitHub에서 링크가 열리는지 확인했다.
- [ ] 보고서 5절에 실제 AI 활용 사례와 본인의 수정·검증 내용을 기록했다.
- [ ] 교수자가 저장소를 열람·복제할 수 있다.
- [ ] 비밀 키·실제 토큰·DB·node_modules·.venv·target을 커밋하지 않았다.
- [ ] 최종 push 후 저장소 URL과 제출 기준 커밋 SHA를 LMS에 제출했다.

이 문서의 상대 링크는 현재 위치인 docs 폴더 기준입니다. 루트 README.md로 옮길 때는 같은 폴더의 문서 링크에 `docs/`를 붙이고 소스 링크 앞의 `../`를 제거해야 합니다.
