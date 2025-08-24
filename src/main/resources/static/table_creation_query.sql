-- USERS TABLE
CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    avatar_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DEACTIVATED', 'BANNED'))
);

ALTER TABLE users ADD COLUMN full_name VARCHAR(100) NOT NULL;

-- CONVERSATIONS TABLE
CREATE TABLE conversations (
    id CHAR(36) PRIMARY KEY,
    type VARCHAR(20) CHECK (type IN ('PRIVATE', 'GROUP')) NOT NULL,
    name VARCHAR(255),
    created_by CHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE conversation_participants (
    id CHAR(36) PRIMARY KEY,

    conversation_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,

    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_admin BOOLEAN DEFAULT FALSE,

    UNIQUE (conversation_id, user_id)
);

-- MESSAGES TABLE
CREATE TABLE messages (
    id CHAR(36) PRIMARY KEY,
    conversation_id CHAR(36) REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id CHAR(36) REFERENCES users(id),
    content TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    message_type VARCHAR(20) CHECK (message_type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM')),
    attachment_url TEXT,
    reply_to_message_id CHAR(36) REFERENCES messages(id)
);

-- MESSAGE read TABLE (for group read tracking)
CREATE TABLE message_reads (
    message_id CHAR(36),
    user_id CHAR(36),
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id, user_id),
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- TYPING INDICATORS (Will be using cache for this)
CREATE TABLE typing_indicators (
    id UUID PRIMARY KEY,
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    is_typing BOOLEAN,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PRESENCE TABLE
CREATE TABLE presence (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) REFERENCES users(id) ON DELETE CASCADE,
    is_online BOOLEAN DEFAULT FALSE,
    last_seen TIMESTAMP,
    session_id VARCHAR(255)
);

ALTER TABLE presence
DROP COLUMN is_online,
MODIFY last_seen TIMESTAMP NULL DEFAULT NULL;

CREATE TABLE conversation_requests (
    request_id CHAR(36) PRIMARY KEY,
    sender_id VARCHAR(50) REFERENCES users(user_id),
    receiver_id VARCHAR(50) REFERENCES users(user_id),
    status ENUM('PENDING','ACCEPTED','REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

---- NOTIFICATIONS TABLE
--CREATE TABLE notifications (
--    id UUID PRIMARY KEY,
--    recipient_id UUID REFERENCES users(id) ON DELETE CASCADE,
--    type VARCHAR(20) CHECK (type IN ('MESSAGE', 'MENTION', 'INVITE')),
--    content TEXT,
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    is_read BOOLEAN DEFAULT FALSE
--);
