# Requirements Document

## Introduction

인하바다(이나바다)는 인하대학교 캠퍼스 내 나눔 플랫폼으로, 학생들이 불필요한 물품을 무료로 나눌 수 있는 서비스이다. 웹뷰 기반 메인 서비스와 쿠팡 상품 페이지에서 나눔 게시글과의 시맨틱 매칭을 제공하는 Chrome Extension으로 구성된다. 수령자가 요청하고 나눔자가 승인/거절하는 구조로 운영된다.

## Glossary

- **Platform**: 인하바다 웹뷰 서비스 전체를 지칭하는 시스템
- **Feed**: 활성 나눔 게시글 카드 리스트를 표시하는 메인 화면
- **Post**: 나눔 게시글 단위로, 제목·설명·사진·카테고리·수량·일정 정보를 포함
- **Giver**: 나눔 게시글을 등록하여 물품을 나누는 사용자
- **Receiver**: 나눔 게시글에 요청을 보내 물품을 수령하는 사용자
- **Request**: 수령자가 특정 게시글에 대해 수량과 일정을 선택하여 보낸 나눔 요청 레코드
- **Slot**: 나눔자가 설정한 수령 가능 일정 단위
- **Remaining_Quantity**: 게시글의 전체 수량에서 승인된 요청 수량을 차감한 잔여 수량
- **Notification**: 사용자에게 전달되는 알림 메시지
- **Extension**: 쿠팡 상품 페이지에서 인하바다 게시글과 시맨틱 매칭을 수행하는 Chrome Extension
- **Semantic_Matching**: AWS Bedrock(Claude Haiku)를 사용하여 상품명과 게시글을 의미 기반으로 비교하는 기능
- **Presigned_URL**: S3 업로드를 위해 서버에서 발급하는 시간 제한 서명 URL

## Requirements

### Requirement 1: 메인 피드 조회

**User Story:** As a 사용자, I want 활성 나눔 게시글을 카드 형태로 조회하고 필터링할 수 있기를, so that 원하는 물품을 빠르게 찾을 수 있다.

#### Acceptance Criteria

1. WHEN 사용자가 메인 피드에 접근하면, THE Platform SHALL 활성 상태인 Post 목록을 최신 등록순으로 정렬하여 카드 리스트 형태로 표시하되, 각 카드에는 제목, 썸네일 사진, Remaining_Quantity, 카테고리를 포함한다
2. WHEN 사용자가 카테고리 필터를 선택하면, THE Feed SHALL 해당 카테고리에 속하는 Post만 표시한다
3. WHEN 사용자가 키워드를 입력하면, THE Feed SHALL 제목 또는 설명에 해당 키워드를 포함하는 Post만 표시한다
4. WHEN 사용자가 피드 하단에 도달하면, THE Feed SHALL 다음 페이지의 Post 20개를 자동으로 로드하여 기존 목록 하단에 추가한다
5. IF Post의 Remaining_Quantity가 0이면, THEN THE Feed SHALL 해당 Post 카드를 마감 상태로 시각적으로 구분하여 표시한다
6. THE Platform SHALL 비로그인 사용자에게 메인 피드 조회를 허용한다
7. WHEN 비로그인 사용자가 메인 피드 외 기능에 접근하면, THE Platform SHALL 로그인을 요구한다
8. IF 필터 또는 키워드 검색 결과에 해당하는 Post가 없으면, THEN THE Feed SHALL 결과 없음을 안내하는 메시지를 표시한다

---

### Requirement 2: 게시글 등록

**User Story:** As a Giver, I want 나눔할 물품의 정보를 등록할 수 있기를, so that 다른 학생들이 해당 물품을 확인하고 요청할 수 있다.

#### Acceptance Criteria

1. WHEN Giver가 게시글 등록을 요청하면, THE Platform SHALL 제목, 설명, 사진, 카테고리, 총 수량, 수령 가능 일정(Slot) 입력 양식을 제공한다
2. IF 총 수량이 1 미만이거나 99 초과인 경우, THEN THE Platform SHALL 게시글 등록을 거부하고 허용 범위(1~99)를 안내하는 오류 메시지를 표시한다
3. IF 사진이 1장 미만이거나 5장을 초과하는 경우, THEN THE Platform SHALL 게시글 등록을 거부하고 허용 범위(1~5장)를 안내하는 오류 메시지를 표시한다
4. IF 제목이 1자 미만이거나 50자를 초과하는 경우, THEN THE Platform SHALL 게시글 등록을 거부하고 허용 범위를 안내하는 오류 메시지를 표시한다
5. WHEN Giver가 수령 가능 일정을 등록하면, THE Platform SHALL 종료 시각이 시작 시각보다 이후인 경우에만 등록을 허용한다
6. IF 시작 시각이 현재 시각 이전인 Slot을 등록하려는 경우, THEN THE Platform SHALL 해당 일정 등록을 거부하고 시작 시각이 현재 시각 이후여야 함을 안내하는 오류 메시지를 표시한다
7. IF 수령 가능 일정(Slot)이 1개 미만이거나 10개를 초과하는 경우, THEN THE Platform SHALL 게시글 등록을 거부하고 허용 범위(1~10개)를 안내하는 오류 메시지를 표시한다
8. WHEN Giver가 사진 업로드를 요청하면, THE Platform SHALL 유효 시간 10분의 S3 Presigned_URL을 발급하여 클라이언트 직접 업로드를 지원한다
9. IF 게시글 등록 시 필수 항목(제목, 설명, 사진, 카테고리, 총 수량, Slot)이 누락된 경우, THEN THE Platform SHALL 누락된 항목명을 명시한 오류 메시지를 표시한다
10. WHEN 게시글 등록이 완료되면, THE Platform SHALL 해당 Post의 상태를 active로 설정하고 Remaining_Quantity를 총 수량과 동일하게 초기화한다

---

### Requirement 3: 게시글 상세 조회

**User Story:** As a Receiver, I want 게시글의 상세 정보를 확인할 수 있기를, so that 나눔 요청 여부를 판단할 수 있다.

#### Acceptance Criteria

1. WHEN 로그인 사용자가 게시글을 선택하면, THE Platform SHALL 제목, 설명, 사진, Remaining_Quantity, 전체 수량, Giver 닉네임, 등록 시각을 표시한다
2. WHEN 사용자가 게시글 상세를 조회하면, THE Platform SHALL Giver가 등록한 수령 가능 Slot 목록(시작 시각, 종료 시각)을 표시한다
3. WHILE Post의 Remaining_Quantity가 0인 동안, THE Platform SHALL 나눔 요청 버튼을 비활성화하고 "마감되었습니다" 메시지를 표시한다
4. IF 존재하지 않거나 삭제된 Post에 접근하는 경우, THEN THE Platform SHALL "게시글을 찾을 수 없습니다" 오류 메시지를 표시한다
5. IF 로그인 사용자가 본인이 등록한 Post를 조회하는 경우, THEN THE Platform SHALL 나눔 요청 버튼을 숨기고 대신 게시글 관리(수정/마감) 옵션을 표시한다

---

### Requirement 4: 나눔 요청

**User Story:** As a Receiver, I want 원하는 물품에 대해 수량과 일정을 선택하여 요청할 수 있기를, so that Giver에게 나눔 의사를 전달할 수 있다.

#### Acceptance Criteria

1. WHEN Receiver가 나눔 요청을 생성하면, THE Platform SHALL 가져갈 수량 입력(1 이상 정수)과 Slot 선택(단일) 양식을 제공한다
2. IF 요청 수량이 1 미만이거나 Remaining_Quantity를 초과하는 경우, THEN THE Platform SHALL 요청 생성을 거부하고 허용 범위(1 이상 Remaining_Quantity 이하)를 안내하는 오류 메시지를 표시한다
3. IF 해당 Post에 대한 모든 pending 상태 Request의 수량 합산과 신규 요청 수량의 합이 Remaining_Quantity를 초과하면, THEN THE Platform SHALL 요청 생성을 거부하고 현재 요청 가능한 수량을 안내하는 오류 메시지를 표시한다
4. WHEN Request가 생성되면, THE Platform SHALL Request 레코드를 pending 상태로 저장한다
5. WHEN Request가 생성되면, THE Platform SHALL Giver에게 새로운 요청 Notification을 전송한다
6. WHEN Giver가 Request를 승인하면, THE Platform SHALL Request 상태를 approved로 변경하고 Receiver에게 승인 Notification을 전송한다
7. WHEN Giver가 Request를 거절하면, THE Platform SHALL Request 상태를 rejected로 변경하고 Receiver에게 거절 Notification을 전송한다
8. WHEN Request가 승인되면, THE Platform SHALL 해당 Post의 Remaining_Quantity를 승인된 수량만큼 차감한다
9. IF Receiver가 본인이 등록한 Post에 나눔 요청을 시도하는 경우, THEN THE Platform SHALL 요청 생성을 거부하고 본인 게시글에는 요청할 수 없음을 안내하는 오류 메시지를 표시한다
10. IF Receiver가 동일 Post에 이미 pending 상태의 Request를 보유한 경우, THEN THE Platform SHALL 새로운 요청 생성을 거부하고 기존 요청이 처리 중임을 안내하는 오류 메시지를 표시한다
11. IF Receiver가 Slot을 선택하지 않고 요청을 제출하는 경우, THEN THE Platform SHALL 요청 생성을 거부하고 Slot 선택이 필요함을 안내하는 오류 메시지를 표시한다

---

### Requirement 5: 마이페이지

**User Story:** As a 로그인 사용자, I want 내가 등록한 나눔과 요청한 나눔 내역을 확인할 수 있기를, so that 나눔 활동을 관리할 수 있다.

#### Acceptance Criteria

1. WHEN 사용자가 마이페이지에 접근하면, THE Platform SHALL "내가 올린 나눔" 탭과 "내가 받은 나눔" 탭을 제공한다
2. WHEN 사용자가 "내가 올린 나눔" 탭을 선택하면, THE Platform SHALL 본인이 등록한 Post 목록과 각 Post의 Remaining_Quantity, Request 목록(요청자 닉네임, 수량, 희망 Slot, 상태)을 표시하고 각 pending Request에 대해 승인/거절 기능을 제공한다
3. WHEN 사용자가 "내가 받은 나눔" 탭을 선택하면, THE Platform SHALL 본인이 요청한 Request 목록과 각 Request의 Post 제목, 요청 수량, 상태를 표시한다
4. WHEN Giver가 본인 게시글의 수동 마감을 요청하면, THE Platform SHALL 해당 Post를 마감 상태로 변경하고, 해당 Post에 남아있는 모든 pending Request를 자동으로 거절 처리하며 각 Receiver에게 거절 Notification을 전송한다

---

### Requirement 6: 알림

**User Story:** As a 사용자, I want 관심 키워드에 매칭되는 새 게시글과 나눔 요청 관련 알림을 받을 수 있기를, so that 중요한 정보를 놓치지 않을 수 있다.

#### Acceptance Criteria

1. WHEN 새로운 Post가 등록되고 해당 Post의 제목 또는 설명이 사용자의 관심 키워드를 부분 문자열로 포함하면, THE Platform SHALL 해당 사용자에게 매칭된 키워드, 관련 Post 링크, 발생 시각을 포함한 키워드 매칭 Notification을 전송한다
2. THE Platform SHALL 새 게시글 등록 이벤트를 감지하여 관심 키워드 매칭을 비동기로 수행한다
3. WHEN 사용자가 알림 목록에 접근하면, THE Platform SHALL 수신된 Notification을 최신순(발생 시각 내림차순)으로 표시하며, 각 Notification에는 메시지, 관련 Post 링크, 발생 시각, 읽음 여부를 포함한다
4. WHEN Giver에게 새로운 Request가 접수되면, THE Platform SHALL Giver에게 요청한 Receiver 정보, 해당 Post 링크, 발생 시각을 포함한 요청 수신 Notification을 전송한다
5. WHEN 사용자가 Notification을 선택하면, THE Platform SHALL 해당 Notification을 읽음 상태로 변경하고 관련 Post 상세 페이지로 이동한다

---

### Requirement 7: 관심 키워드 관리

**User Story:** As a 사용자, I want 관심 키워드를 등록하고 관리할 수 있기를, so that 원하는 물품이 등록될 때 알림을 받을 수 있다.

#### Acceptance Criteria

1. WHEN 사용자가 1자 이상 20자 이하의 관심 키워드를 등록하면, THE Platform SHALL 해당 키워드를 사용자의 관심 키워드 목록에 저장한다
2. WHEN 사용자가 관심 키워드를 삭제하면, THE Platform SHALL 해당 키워드를 사용자의 관심 키워드 목록에서 제거한다
3. WHEN 사용자가 관심 키워드 목록을 조회하면, THE Platform SHALL 등록된 모든 키워드를 등록 시각과 함께 표시한다
4. IF 사용자가 이미 등록된 키워드와 동일한 키워드를 등록하려는 경우, THEN THE Platform SHALL 중복 등록을 거부하고 이미 등록된 키워드임을 나타내는 오류 메시지를 표시한다
5. IF 사용자의 관심 키워드 수가 최대 20개에 도달한 상태에서 새 키워드를 등록하려는 경우, THEN THE Platform SHALL 등록을 거부하고 최대 등록 가능 수를 초과했음을 나타내는 오류 메시지를 표시한다
6. IF 사용자가 빈 문자열 또는 20자를 초과하는 키워드를 등록하려는 경우, THEN THE Platform SHALL 등록을 거부하고 키워드 길이 조건을 나타내는 오류 메시지를 표시한다

---

### Requirement 8: Chrome Extension - 쿠팡 매칭

**User Story:** As a 사용자, I want 쿠팡에서 상품을 볼 때 인하바다에 유사한 나눔 게시글이 있는지 확인할 수 있기를, so that 구매 전 무료 나눔을 받을 수 있는 기회를 확인할 수 있다.

#### Acceptance Criteria

1. WHEN 사용자가 쿠팡 상품 페이지에서 "비교하기" 버튼을 클릭하면, THE Extension SHALL 해당 페이지의 상품명을 DOM에서 추출한다
2. WHEN 사용자가 쿠팡 장바구니 페이지에서 개별 상품의 "비교하기" 버튼을 클릭하면, THE Extension SHALL 해당 상품의 상품명을 DOM에서 추출한다
3. WHEN 상품명이 추출되면, THE Extension SHALL 백엔드 Semantic_Matching API를 호출하여 유사도 점수 0.7 이상인 활성 Post를 최대 5개까지 검색한다
4. WHEN Semantic_Matching 결과 유사한 Post가 존재하면, THE Extension SHALL 매칭된 Post의 미리보기(제목, 사진, Remaining_Quantity)와 웹뷰 이동 링크를 팝업으로 표시한다
5. WHEN Semantic_Matching 결과 유사한 Post가 존재하지 않으면, THE Extension SHALL "매칭 결과 없음" 메시지를 팝업으로 표시한 후 3초 뒤 자동으로 닫는다
6. WHEN 사용자가 매칭된 Post의 웹뷰 이동 링크를 클릭하면, THE Extension SHALL 해당 Post의 상세 페이지를 새 탭에서 열어준다
7. IF Semantic_Matching API 호출이 실패하면, THEN THE Extension SHALL "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요" 오류 메시지를 팝업으로 표시한다

---

### Requirement 9: 인증

**User Story:** As a 인하대학교 학생, I want 인하대학교 구성원만 서비스를 이용할 수 있기를, so that 캠퍼스 내 신뢰할 수 있는 나눔이 이루어질 수 있다.

#### Acceptance Criteria

1. THE Platform SHALL 인증된 사용자만 게시글 상세, 게시글 등록, 나눔 요청, 마이페이지, 알림 기능에 접근하도록 제한한다
2. WHEN 사용자가 로그인을 요청하면, THE Platform SHALL @inha.edu 이메일 도메인 기반 인증을 통해 인하대학교 소속 확인을 수행한다
3. WHEN 인증에 성공하면, THE Platform SHALL 세션을 생성하고 사용자를 인증된 상태로 전환한다
4. WHEN 인증에 실패하면, THE Platform SHALL 인증 실패 사유를 포함한 오류 메시지를 표시한다
5. THE Platform SHALL 세션 유효 기간을 7일로 설정하며, 유효 기간이 만료되면 자동으로 로그아웃 처리한다
6. WHEN 세션이 만료된 사용자가 인증 필수 기능에 접근하면, THE Platform SHALL 로그인 페이지로 리다이렉트하고 "세션이 만료되었습니다. 다시 로그인해주세요" 메시지를 표시한다
