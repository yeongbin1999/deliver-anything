# deliver-anything
프로그래머스 백엔드 데브코스 6기 8회차 4차 프로젝트

# Workflow Guide

## 1. Issue → Branch
- **이슈는 반드시 GitHub Project 보드에서 생성**
  - Projects → Buddy App → **New issue** 버튼 클릭
  - 이슈 템플릿(`Type`, `Scope`, `Summary`, `Details`)에 맞춰 작성
- 규칙에 맞는 이슈만 자동 브랜치 생성됨
- 브랜치 네이밍 규칙:
  ```
  {type}/{scope}/{issue_number}
  ```
  예) `feat/be/12`

### Type
- `feat` : 새로운 기능
- `fix` : 버그 수정
- `refactor` : 리팩터링
- `docs` : 문서 작업
- `chore` : 환경/설정/잡일
- `test` : 테스트 코드

### Scope
- `fe` : Frontend
- `be` : Backend
- `infra` : Infra / 배포 / 환경

---

## 2. Pull Request
- 브랜치 작업 완료 후 → **PR 생성**
- **PR 제목 자동 동기화**: 이슈 제목 + 번호  
  예)  
  ```
  feat(be): 로그인 API 추가 (#12)
  ```

### PR 병합 규칙
- `dev` 브랜치로 머지:  
  - 관련 이슈 자동 close  
  - 작업 브랜치 자동 삭제
- `main` 브랜치로 머지:  
  - 배포 파이프라인(CD) 실행

---

## 3. Branch Strategy
- `main` : 배포용 브랜치 (Release 태그, Docker 빌드/푸시, 배포 실행)  
- `dev` : 통합 개발 브랜치 (이슈별 브랜치가 합쳐지는 곳)  
- `feat/*`, `fix/*`, `refactor/*`, `docs/*`, `chore/*`, `test/*` :  
  → 이슈 단위 작업 브랜치 (머지 후 자동 삭제)

---

## 4. CI/CD
### CI (Backend CI)
- **트리거**: `dev`, `main` 브랜치에서 push & PR  
- **동작**:
  - Gradle 빌드 & 테스트 실행
  - Redis 컨테이너 서비스 지원
  - `.env` 파일 GitHub Secrets 기반 로드

### CD (Backend CD)
- **트리거**: `main` 브랜치 push  
- **동작**:
  - Git Tag + Release 생성
  - Docker 이미지 빌드 & GHCR Push
  - AWS EC2 Blue/Green 배포 (SSM SendCommand 이용)

---

## 5. Issue Template
- 하나의 공통 템플릿 제공
  - **Type** : feat / fix / refactor / docs / chore / test  
  - **Scope** : fe / be / infra  
  - **Summary** : 간단 요약 (브랜치명/PR 제목 반영)  
  - **Details** : 작업 설명 & 완료 기준

---

## ✅ Workflow 요약
1. **Issue 생성 (Projects 보드에서만)**  
2. 규칙에 맞으면 **브랜치 자동 생성**  
3. 작업 후 **PR 생성 → PR 제목 자동 동기화**  
4. **PR 병합**
   - `dev`: 이슈 닫기 + 브랜치 삭제  
   - `main`: CD 실행 (배포)  
5. **Release & 배포** → Docker + AWS EC2 Blue/Green