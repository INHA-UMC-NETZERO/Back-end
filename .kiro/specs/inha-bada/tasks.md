# Implementation Plan: 인하바다 (Inha-Bada) Spring Boot 백엔드 API

## Overview

Spring Boot 3.x + Java 17 기반 백엔드 API 서버를 구현한다. RDS PostgreSQL과 Spring Data JPA를 데이터 계층으로 사용하고, S3 Presigned URL로 이미지 업로드를 지원하며, AWS Bedrock(Claude Haiku)으로 시맨틱 매칭을 제공한다. 알림 처리는 Spring ApplicationEvent + @Async EventListener로 비동기 처리한다. 동시성 제어는 JPA @Version 낙관적 락 + @Retryable로 구현한다.

## Tasks

- [x] 1. 프로젝트 구조 및 공통 인프라 설정
  - [x] 1.1 Spring Boot 프로젝트 초기 설정
    - Spring Boot 3.x + Java 17 프로젝트 구조 생성
    - build.gradle에 의존성 추가: Spring Data JPA, PostgreSQL Driver, AWS SDK v2 (S3, Bedrock), Spring Web, Spring Validation, Spring Retry, jqwik (테스트), Testcontainers
    - application.yml에 PostgreSQL 연결 설정, S3 버킷명, Bedrock 모델 ID, JPA/Hibernate 설정
    - 패키지 구조: controller, service, repository, entity, dto, config, event, exception
    - _Requirements: 전체_

  - [x] 1.2 JPA Entity 및 Enum 정의
    - User, Post, Slot, ShareRequest, Notification, KeywordSubscription, Session Entity 클래스 작성
    - PostStatus(ACTIVE, CLOSED), RequestStatus(PENDING, APPROVED, REJECTED), NotificationType(KEYWORD_MATCH, REQUEST_RECEIVED, REQUEST_APPROVED, REQUEST_REJECTED) Enum 작성
    - Post Entity에 @Version 필드 추가 (낙관적 락)
    - @Table 어노테이션에 인덱스 정의 포함 (idx_posts_status_created, idx_posts_category_created, idx_posts_giver 등)
    - Post-Slot 간 @OneToMany 관계 매핑
    - JPA Auditing 설정 (createdAt, updatedAt 자동 관리)
    - _Requirements: 전체 (데이터 계층)_

  - [x] 1.3 Spring Data JPA Repository 인터페이스 정의
    - PostRepository: findByStatus, findByStatusAndCategory, searchByKeyword(@Query JPQL), findByGiverId
    - ShareRequestRepository: findByPostIdAndStatus, findByReceiverId, existsByPostIdAndReceiverIdAndStatus, sumPendingQuantityByPostId(@Query)
    - NotificationRepository: findByUserIdOrderByCreatedAtDesc
    - KeywordSubscriptionRepository: findByUserId, findByKeyword, existsByUserIdAndKeyword, deleteByUserIdAndKeyword, countByUserId
    - SessionRepository: findByToken, deleteByToken, deleteByExpiresAtBefore
    - UserRepository: findByEmail
    - _Requirements: 전체 (데이터 접근)_

  - [x] 1.4 공통 예외 처리 및 응답 구조 정의
    - GlobalExceptionHandler (@ControllerAdvice) 작성
    - ErrorResponse DTO: error 코드, message, fields(선택) 포함
    - 커스텀 예외 클래스: ValidationException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException
    - OptimisticLockException → 409 매핑
    - DataIntegrityViolationException → 409 매핑 (유니크 제약 위반)
    - HTTP 상태 코드 매핑 (400, 401, 403, 404, 409, 500)
    - _Requirements: 전체 에러 처리_

  - [x] 1.5 인증 인터셉터 및 세션 관리 구현
    - AuthInterceptor 구현: 세션 토큰 검증, 비로그인 허용 경로 설정
    - SessionService: 세션 생성(7일 TTL), 검증, 삭제 로직 (SessionRepository 기반)
    - @CurrentUser 어노테이션 + ArgumentResolver로 현재 사용자 정보 주입
    - 비로그인 허용 경로: GET /api/posts (메인 피드)
    - 만료 세션 정리 로직 (@Scheduled)
    - _Requirements: 9.1, 9.3, 9.5, 9.6, 1.6, 1.7_

  - [x] 1.6 Spring ApplicationEvent 및 @Async 설정
    - @EnableAsync 설정 (AsyncConfig.java)
    - TaskExecutor Bean 설정 (스레드 풀 크기 설정)
    - 이벤트 클래스 정의: PostCreatedEvent, RequestCreatedEvent, RequestApprovedEvent, RequestRejectedEvent, PostClosedEvent
    - _Requirements: 6.1, 6.2_

- [ ] 2. 인증 API 구현
  - [x] 2.1 AuthController 및 AuthService 구현 (목업 인증 - JWT 없이 UUID 토큰 방식)
    - POST /api/auth/login: @inha.edu 이메일 기반 목업 인증 (이메일 도메인 검증 + 자동 회원 생성)
    - POST /api/auth/logout: 세션 삭제
    - LoginRequest DTO: email, nickname 검증
    - 이메일 도메인 검증 (@inha.edu)
    - 인증 성공 시 UUID 세션 토큰 발급 (Session 엔티티 저장), 실패 시 오류 메시지 반환
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [ ]* 2.2 인증 접근 제어 프로퍼티 테스트 작성
    - **Property 5: 인증 접근 제어**
    - jqwik @Property: 유효하지 않은 토큰으로 인증 필수 API 호출 시 401 반환 검증
    - **Validates: Requirements 1.7, 9.1**

  - [ ]* 2.3 인증 세션 라운드트립 프로퍼티 테스트 작성
    - **Property 19: 인증 세션 라운드트립**
    - jqwik @Property: 로그인 후 발급된 세션으로 인증 API 접근 가능 검증
    - **Validates: Requirements 9.2, 9.3**

  - [ ]* 2.4 인증 실패 오류 메시지 프로퍼티 테스트 작성
    - **Property 20: 인증 실패 오류 메시지**
    - jqwik @Property: @inha.edu 아닌 이메일 또는 잘못된 인증 정보 시 실패 사유 포함 오류 메시지 검증
    - **Validates: Requirements 9.4**

- [ ] 3. Checkpoint - 인증 및 인프라 검증
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. 게시글 등록 및 S3 업로드 구현
  - [x] 4.1 UploadController 및 UploadService 구현
    - POST /api/uploads/presigned-url: fileName, contentType 받아 S3 Presigned URL 반환
    - S3Presigner를 사용하여 PUT 용 Presigned URL 생성 (유효시간 10분)
    - 파일 크기 제한 (10MB), Content-Type 조건 포함
    - S3 키 형식: posts/{postId}/{uuid}.{ext}
    - _Requirements: 2.8_

  - [x] 4.2 PostController 및 PostService - 게시글 등록 구현
    - POST /api/posts: CreatePostRequest DTO (title, description, imageKeys, category, totalQuantity, slots)
    - 입력 검증: 제목(1~50자), 수량(1~99), 사진(1~5장), 슬롯(1~10개)
    - 슬롯 시간 검증: 종료 > 시작, 시작 > 현재 시각
    - Post + Slot 엔티티 저장: status=ACTIVE, remainingQuantity=totalQuantity
    - 저장 후 PostCreatedEvent 발행 (ApplicationEventPublisher)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.9, 2.10_

  - [ ]* 4.3 게시글 등록 입력 검증 프로퍼티 테스트 작성
    - **Property 7: 게시글 등록 입력 검증**
    - jqwik @Property: 수량 범위, 사진 수, 제목 길이, 슬롯 수 범위 벗어나면 거부 및 오류 메시지 검증
    - **Validates: Requirements 2.2, 2.3, 2.4, 2.7, 2.9**

  - [ ]* 4.4 수령 일정 시간 검증 프로퍼티 테스트 작성
    - **Property 6: 수령 일정 시간 검증**
    - jqwik @Property: 슬롯 시작/종료 시각 유효성 검증 (종료 > 시작, 시작 > 현재)
    - **Validates: Requirements 2.5, 2.6**

- [x] 5. 메인 피드 및 게시글 상세 조회 구현
  - [x] 5.1 FeedController 및 PostService - 피드 조회 구현
    - GET /api/posts: 활성 게시글 목록 (Pageable 기반 페이지네이션, 기본 20개)
    - 카테고리 필터: PostRepository.findByStatusAndCategoryOrderByCreatedAtDesc
    - 키워드 검색: PostRepository.searchByKeyword (JPQL LIKE 쿼리, 인덱스 활용)
    - 결과 없을 시 빈 목록 + 적절한 메시지 반환
    - Page 응답: content, totalElements, hasNext, page 정보 포함
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.8_

  - [x] 5.2 FeedController - 게시글 상세 조회 구현
    - GET /api/posts/{id}: Post + Slot 목록 조회 (JPA Lazy Fetch)
    - 응답: 제목, 설명, 사진 URL, remainingQuantity, totalQuantity, giverId, giverName, createdAt, slots[]
    - 404: 존재하지 않는 게시글
    - remainingQuantity=0 시 마감 표시 정보 포함
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [ ]* 5.3 활성 피드 필터링 정확성 프로퍼티 테스트 작성
    - **Property 1: 활성 피드 필터링 정확성**
    - jqwik @Property: ACTIVE 상태만 반환, 카테고리 필터 정확성 검증
    - **Validates: Requirements 1.1, 1.2**

  - [ ]* 5.4 키워드 검색 정확성 프로퍼티 테스트 작성
    - **Property 2: 키워드 검색 정확성**
    - jqwik @Property: 검색 결과 모두 제목 또는 설명에 키워드 포함 검증
    - **Validates: Requirements 1.3**

  - [ ]* 5.5 커서 기반 페이지네이션 무결성 프로퍼티 테스트 작성
    - **Property 3: 커서 기반 페이지네이션 무결성**
    - jqwik @Property: 전체 순회 시 중복 없이 N개, 시간 역순 정렬 검증
    - **Validates: Requirements 1.4**

  - [ ]* 5.6 잔여수량 0 마감 표시 프로퍼티 테스트 작성
    - **Property 4: 잔여수량 0 마감 표시**
    - jqwik @Property: remainingQuantity=0이면 마감, >0이면 활성 검증
    - **Validates: Requirements 1.5, 3.3**

  - [ ]* 5.7 게시글 상세 응답 완전성 프로퍼티 테스트 작성
    - **Property 8: 게시글 상세 응답 완전성**
    - jqwik @Property: 상세 조회 시 필수 필드 모두 포함 검증
    - **Validates: Requirements 3.1, 3.2**

- [ ] 6. Checkpoint - 피드 및 게시글 기능 검증
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. 나눔 요청 및 승인/거절 구현
  - [x] 7.1 RequestController 및 RequestService - 요청 생성 구현
    - POST /api/posts/{id}/requests: CreateRequestDto (quantity, slotId)
    - 입력 검증: quantity >= 1, slotId 유효성
    - 비즈니스 검증: 본인 게시글 요청 불가, 중복 pending 요청 불가(existsByPostIdAndReceiverIdAndStatus), 수량 초과 불가(sumPendingQuantityByPostId)
    - ShareRequest 엔티티 저장: status=PENDING
    - RequestCreatedEvent 발행 → Giver에게 알림 생성
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.9, 4.10, 4.11_

  - [x] 7.2 RequestController - 요청 승인/거절 구현
    - PATCH /api/requests/{id}/approve: 요청 승인 + remainingQuantity 차감 (@Version 낙관적 락)
    - PATCH /api/requests/{id}/reject: 요청 거절
    - 권한 검증: 해당 Post의 Giver만 승인/거절 가능
    - 승인 시: status→APPROVED, remainingQuantity -= quantity (OptimisticLockException 발생 시 @Retryable 재시도, 최대 3회, 100ms backoff)
    - 거절 시: status→REJECTED
    - 상태 변경 시 RequestApprovedEvent/RequestRejectedEvent 발행 → Receiver에게 알림 생성
    - remainingQuantity가 0이 되면 Post 상태를 CLOSED로 변경
    - _Requirements: 4.6, 4.7, 4.8_

  - [ ]* 7.3 나눔 요청 수량 제한 프로퍼티 테스트 작성
    - **Property 9: 나눔 요청 수량 제한**
    - jqwik @Property: pending 합산 + 신규 > remainingQuantity이면 거부 검증
    - **Validates: Requirements 4.2, 4.3**

  - [ ]* 7.4 요청 상태 초기화 프로퍼티 테스트 작성
    - **Property 10: 요청 상태 초기화**
    - jqwik @Property: 생성된 Request 상태는 항상 PENDING 검증
    - **Validates: Requirements 4.4**

  - [ ]* 7.5 요청 상태 전이와 알림 프로퍼티 테스트 작성
    - **Property 11: 요청 상태 전이와 알림**
    - jqwik @Property: 승인→APPROVED+알림, 거절→REJECTED+알림 검증
    - **Validates: Requirements 4.5, 4.6, 4.7**

  - [ ]* 7.6 승인 시 수량 차감 정확성 프로퍼티 테스트 작성
    - **Property 12: 승인 시 수량 차감 정확성**
    - jqwik @Property: 승인 전 remaining - Q = 승인 후 remaining 검증
    - **Validates: Requirements 4.8**

- [x] 8. 마이페이지 API 구현
  - [x] 8.1 MyPageController 및 서비스 로직 구현
    - GET /api/mypage/posts: 내가 올린 나눔 목록 (PostRepository.findByGiverIdOrderByCreatedAtDesc)
    - GET /api/mypage/requests: 내가 받은 나눔 목록 (ShareRequestRepository.findByReceiverIdOrderByCreatedAtDesc)
    - PATCH /api/posts/{id}/close: 수동 마감 (Giver 본인만, 상태→CLOSED, PostClosedEvent 발행)
    - PostClosedEvent Listener: pending 요청 일괄 거절 + 각 Receiver에게 거절 알림 생성
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [ ]* 8.2 마이페이지 소유자 격리 프로퍼티 테스트 작성
    - **Property 13: 마이페이지 소유자 격리**
    - jqwik @Property: 내 게시글 탭은 본인 게시글만, 내 요청 탭은 본인 요청만 포함 검증
    - **Validates: Requirements 5.2, 5.3**

  - [ ]* 8.3 수동 마감 시 pending 요청 자동 거절 프로퍼티 테스트 작성
    - **Property 14: 수동 마감 시 pending 요청 자동 거절**
    - jqwik @Property: 마감 시 모든 pending Request가 REJECTED 변경 + 거절 알림 생성 검증
    - **Validates: Requirements 5.4**

- [ ] 9. Checkpoint - 나눔 요청 및 마이페이지 검증
  - Ensure all tests pass, ask the user if questions arise.

- [x] 10. 관심 키워드 관리 구현
  - [x] 10.1 KeywordController 및 KeywordService 구현
    - GET /api/keywords: 내 키워드 목록 (KeywordSubscriptionRepository.findByUserId)
    - POST /api/keywords: 키워드 등록 (길이 1~20자, 중복 검사 existsByUserIdAndKeyword, 최대 20개 제한 countByUserId)
    - DELETE /api/keywords/{keyword}: 키워드 삭제 (deleteByUserIdAndKeyword)
    - 중복 등록 시 409, 최대 개수 초과 시 400 응답
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

  - [ ]* 10.2 키워드 CRUD 라운드트립 프로퍼티 테스트 작성
    - **Property 15: 키워드 CRUD 라운드트립**
    - jqwik @Property: 등록 후 조회 시 포함, 삭제 후 조회 시 미포함, 중복 등록 거부 검증
    - **Validates: Requirements 7.1, 7.2, 7.3, 7.4**

- [x] 11. 알림 API 및 이벤트 리스너 구현
  - [x] 11.1 NotificationController 및 NotificationService 구현
    - GET /api/notifications: 알림 목록 조회 (NotificationRepository.findByUserIdOrderByCreatedAtDesc, Pageable)
    - PATCH /api/notifications/{id}/read: 알림 읽음 처리
    - NotificationService.createNotification(): Notification 엔티티 생성 저장
    - _Requirements: 6.3, 6.4, 6.5_

  - [x] 11.2 키워드 매칭 이벤트 리스너 구현
    - KeywordMatchEventListener: @Async @TransactionalEventListener(phase = AFTER_COMMIT)
    - PostCreatedEvent 수신 → 게시글 title+description에서 키워드 매칭 수행
    - KeywordSubscriptionRepository.findAll()로 전체 구독 조회 → 키워드별 그룹핑 → contains 매칭
    - 매칭된 사용자에게 KEYWORD_MATCH 알림 생성 (본인 게시글 제외)
    - RequestCreatedEvent, RequestApprovedEvent, RequestRejectedEvent 리스너도 구현
    - @Retryable 적용 (예외 발생 시 최대 3회 재시도)
    - _Requirements: 6.1, 6.2_

  - [ ]* 11.3 키워드 매칭 알림 생성 프로퍼티 테스트 작성
    - **Property 16: 키워드 매칭 알림 생성**
    - jqwik @Property: 게시글에 키워드 포함 시 알림 생성, 미포함 시 미생성, 본인 게시글 제외 검증
    - **Validates: Requirements 6.1**

  - [ ]* 11.4 알림 시간순 정렬 프로퍼티 테스트 작성
    - **Property 17: 알림 시간순 정렬**
    - jqwik @Property: 알림 목록이 생성 시간 역순 정렬 검증
    - **Validates: Requirements 6.3**

- [x] 12. 시맨틱 매칭 API 구현
  - [x] 12.1 MatchingController 및 SemanticMatchingService 구현
    - POST /api/matching/semantic: SemanticMatchRequest (productName)
    - 활성 게시글 최근 N개 조회 (PostRepository.findByStatusOrderByCreatedAtDesc)
    - Bedrock Converse API 호출 (Claude Haiku): 프롬프트 구성 + JSON 응답 파싱
    - 유사도 0.7 이상 + 최대 5개 결과 반환
    - 응답: MatchResult (postId, title, imageUrl, remainingQuantity, webviewLink, similarity)
    - 에러 처리: ThrottlingException 재시도 (지수 백오프, 최대 3회), Timeout 시 빈 결과 반환
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.7_

  - [ ]* 12.2 매칭 결과 응답 완전성 프로퍼티 테스트 작성
    - **Property 18: 매칭 결과 응답 완전성**
    - jqwik @Property: 매칭 결과에 제목, 사진URL, remainingQuantity, 웹뷰 링크 포함 검증
    - **Validates: Requirements 8.4**

- [ ] 13. Checkpoint - 키워드, 알림, 매칭 기능 검증
  - Ensure all tests pass, ask the user if questions arise.

- [x] 14. 통합 및 마무리
  - [x] 14.1 API 엔드포인트 통합 연결 및 CORS 설정
    - WebMvcConfigurer에서 CORS 설정 (Chrome Extension 지원)
    - 모든 Controller 엔드포인트 라우팅 확인
    - Request/Response DTO의 직렬화/역직렬화 검증
    - health check 엔드포인트 추가 (GET /api/health)
    - Spring Retry 설정 확인 (@EnableRetry)
    - _Requirements: 전체_

  - [ ]* 14.2 Testcontainers 기반 통합 테스트 작성
    - Testcontainers + PostgreSQL 기반 Repository 계층 통합 테스트
    - 게시글 등록 → 피드 조회 → 요청 → 승인 흐름 E2E 테스트
    - 인증 흐름 통합 테스트
    - Spring ApplicationEvent 발행 → 알림 생성 E2E 흐름 테스트
    - 낙관적 락 동시성 테스트 (멀티 스레드 환경)
    - _Requirements: 전체_

- [ ] 15. Final Checkpoint - 전체 기능 검증
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using jqwik framework
- Unit tests validate specific examples and edge cases
- RDS PostgreSQL + Spring Data JPA를 핵심 데이터 계층으로 사용하며, 정규화된 관계형 테이블 설계 적용
- Spring ApplicationEvent + @Async로 비동기 알림 처리 (DynamoDB Streams + Lambda 대체)
- JPA @Version 낙관적 락 + @Retryable로 동시성 제어 (DynamoDB Conditional Write 대체)
- Chrome Extension 자체 구현은 이 백엔드 API 스코프 외이나, 매칭 API는 포함

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.4"] },
    { "id": 2, "tasks": ["1.3", "1.5", "1.6"] },
    { "id": 3, "tasks": ["2.1", "4.1"] },
    { "id": 4, "tasks": ["2.2", "2.3", "2.4", "4.2"] },
    { "id": 5, "tasks": ["4.3", "4.4", "5.1"] },
    { "id": 6, "tasks": ["5.2", "5.3", "5.4", "5.5", "5.6", "5.7"] },
    { "id": 7, "tasks": ["7.1", "10.1"] },
    { "id": 8, "tasks": ["7.2", "7.3", "7.4", "10.2"] },
    { "id": 9, "tasks": ["7.5", "7.6", "8.1"] },
    { "id": 10, "tasks": ["8.2", "8.3", "11.1"] },
    { "id": 11, "tasks": ["11.2", "11.3", "11.4"] },
    { "id": 12, "tasks": ["12.1"] },
    { "id": 13, "tasks": ["12.2", "14.1"] },
    { "id": 14, "tasks": ["14.2"] }
  ]
}
```
