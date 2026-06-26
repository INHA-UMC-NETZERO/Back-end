-- =============================================================================
-- 인하바다 (Inha-Bada) 스키마 — PostgreSQL / public schema
-- 대상: AWS RDS (DB: postgres)
-- 앱 설정이 spring.jpa.hibernate.ddl-auto=validate 이므로 이 테이블들이
-- 미리 존재해야 애플리케이션이 정상 부팅된다.
-- 생성 순서는 FK 의존성에 따른다: users → posts → slots → share_requests
--                                  → notifications → keyword_subscriptions → sessions
--
-- [pg_trgm 권한 주의]
-- CREATE EXTENSION pg_trgm 은 RDS에서 rds_superuser(보통 마스터 유저) 권한이 필요하다.
-- 접속 유저(inhabada)가 마스터면 그대로 실행된다. 권한 오류가 나면 아래
-- "CREATE EXTENSION ..." 한 줄과 idx_posts_title_trgm / idx_posts_desc_trgm
-- 두 인덱스만 제거하라. 검색은 표준 JPQL LIKE 라 기능에는 영향 없고 성능만 달라진다.
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- -----------------------------------------------------------------------------
-- Users  (design.md 발췌에 누락된 테이블 — entity/User.java 기준으로 보완)
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(100) NOT NULL UNIQUE,
    nickname   VARCHAR(30)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- Posts
-- -----------------------------------------------------------------------------
CREATE TABLE posts (
    id                 BIGSERIAL PRIMARY KEY,
    giver_id           BIGINT NOT NULL REFERENCES users(id),
    title              VARCHAR(50) NOT NULL,
    description        TEXT NOT NULL,
    category           VARCHAR(30) NOT NULL,
    image_keys         TEXT[] NOT NULL,
    total_quantity     INTEGER NOT NULL CHECK (total_quantity BETWEEN 1 AND 99),
    remaining_quantity INTEGER NOT NULL CHECK (remaining_quantity >= 0),
    status             VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    version            INTEGER NOT NULL DEFAULT 0,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP
);
CREATE INDEX idx_posts_status_created   ON posts(status, created_at DESC);
CREATE INDEX idx_posts_category_created ON posts(category, status, created_at DESC);
CREATE INDEX idx_posts_giver            ON posts(giver_id, created_at DESC);
-- 키워드 검색용 GIN 인덱스 (pg_trgm 활용, 성능 최적화용)
CREATE INDEX idx_posts_title_trgm       ON posts USING GIN (title gin_trgm_ops);
CREATE INDEX idx_posts_desc_trgm        ON posts USING GIN (description gin_trgm_ops);

-- -----------------------------------------------------------------------------
-- Slots
-- -----------------------------------------------------------------------------
CREATE TABLE slots (
    id         BIGSERIAL PRIMARY KEY,
    post_id    BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time   TIMESTAMP NOT NULL,
    CHECK (end_time > start_time)
);
CREATE INDEX idx_slots_post ON slots(post_id);

-- -----------------------------------------------------------------------------
-- Share Requests
-- -----------------------------------------------------------------------------
CREATE TABLE share_requests (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT NOT NULL REFERENCES posts(id),
    receiver_id BIGINT NOT NULL REFERENCES users(id),
    slot_id     BIGINT NOT NULL REFERENCES slots(id),
    quantity    INTEGER NOT NULL CHECK (quantity >= 1),
    status      VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);
CREATE INDEX idx_requests_post_status ON share_requests(post_id, status);
CREATE INDEX idx_requests_receiver    ON share_requests(receiver_id, created_at DESC);

-- -----------------------------------------------------------------------------
-- Notifications
-- -----------------------------------------------------------------------------
CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    type            VARCHAR(30) NOT NULL,
    message         VARCHAR(200) NOT NULL,
    related_post_id BIGINT REFERENCES posts(id),
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC);

-- -----------------------------------------------------------------------------
-- Keyword Subscriptions
-- -----------------------------------------------------------------------------
CREATE TABLE keyword_subscriptions (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id),
    keyword    VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, keyword)
);
CREATE INDEX idx_keyword_subs_user    ON keyword_subscriptions(user_id);
CREATE INDEX idx_keyword_subs_keyword ON keyword_subscriptions(keyword);

-- -----------------------------------------------------------------------------
-- Sessions
-- -----------------------------------------------------------------------------
CREATE TABLE sessions (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id),
    token      VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sessions_token   ON sessions(token);
CREATE INDEX idx_sessions_expires ON sessions(expires_at);
