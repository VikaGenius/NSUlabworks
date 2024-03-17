CREATE TABLE Category (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(25) UNIQUE NOT NULL,
    necessity VARCHAR(25) NOT NULL
);

CREATE TABLE Components (
    component_id SERIAL PRIMARY KEY,
    component_name VARCHAR(100) NOT NULL,
    category_id INTEGER REFERENCES Category(category_id),
    price INTEGER NOT NULL,
    warranty_period INTERVAL NOT NULL
);

CREATE TABLE Computers (
    serial_number VARCHAR(25) PRIMARY KEY,
    vendor_id INTEGER
);

CREATE TABLE Computers_components (
    serial_number VARCHAR(25) NOT NULL REFERENCES Computers(serial_number),
    component_id INTEGER REFERENCES Components(component_id),
    date_sale_component DATE,
    price_computer INTEGER NOT NULL,
    PRIMARY KEY(serial_number, component_id)
);