-- =============================================================================
-- Extra migration for embedding search on posts.
-- Model: LM Studio bge-m3 -> vector(1024)
--
-- This file runs automatically after Hibernate creates the base schema.
-- The pgvector extension must already exist in the database:
--
--   CREATE EXTENSION IF NOT EXISTS vector;
--
-- On RDS, create the extension once with the master user / rds_superuser.
-- =============================================================================

ALTER TABLE posts
    ADD COLUMN IF NOT EXISTS embedding vector(1024);

CREATE INDEX IF NOT EXISTS idx_posts_embedding_hnsw
    ON posts USING hnsw (embedding vector_cosine_ops)
    WHERE status = 'ACTIVE' AND embedding IS NOT NULL;

-- =============================================================================
-- The embedding column must be populated by either:
--
-- 1. The Spring app when a post is created/updated.
-- 2. A backfill job that finds rows where embedding IS NULL and updates them.
--
-- Use the same embedding model and dimension as the search side:
-- bge-m3, 1024 dimensions.
-- =============================================================================
