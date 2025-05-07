-- Create the order database if it doesn't exist
CREATE DATABASE IF NOT EXISTS demoapp_order;

-- Connect to the order database
\c demoapp_order;

-- Create orders table
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    mess_id UUID NOT NULL,
    subscription_id UUID,
    order_status VARCHAR(50) NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_address TEXT,
    delivery_instructions TEXT,
    delivery_contact VARCHAR(20),
    payment_status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_id UUID,
    expected_delivery_time TIMESTAMP
);

-- Create order_items table
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    special_requests TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create order_tracking table
CREATE TABLE order_tracking (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    updated_by UUID NOT NULL
);

-- Create order_ratings table
CREATE TABLE order_ratings (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(order_id, user_id)
);

-- Create indexes for performance optimization
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_mess_id ON orders(mess_id);
CREATE INDEX idx_orders_subscription_id ON orders(subscription_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_tracking_order_id ON order_tracking(order_id);
CREATE INDEX idx_order_ratings_order_id ON order_ratings(order_id);
CREATE INDEX idx_order_ratings_user_id ON order_ratings(user_id); 