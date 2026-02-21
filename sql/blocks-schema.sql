-- Blocks table for membership analytics
CREATE TABLE IF NOT EXISTS blocks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    short_name VARCHAR(20) NOT NULL,
    total_flats INTEGER NOT NULL,
    display_order INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_blocks_active ON blocks(active);
CREATE INDEX IF NOT EXISTS idx_blocks_display_order ON blocks(display_order);

-- Sample data (modify these to match your actual blocks)
INSERT INTO blocks (name, short_name, total_flats, display_order) VALUES
('Windings House', 'Windings', 32, 1),
('Field House', 'Field', 30, 2),
('Bluster House', 'Bluster', 28, 3),
('Ashby House', 'Ashby', 25, 4)
ON CONFLICT (name) DO NOTHING;
