DROP TABLE if EXISTS client;
DROP TABLE if EXISTS login;
DROP TABLE if EXISTS client_consultation;
DROP TABLE if EXISTS g_code;
DROP TABLE if EXISTS orders;

CREATE TABLE login (
    username TEXT NOT NULL,
    password TEXT PRIMARY KEY
);

CREATE TABLE client (
	firstname TEXT NOT NULL,
	lastname TEXT NOT NULL,
	email TEXT NOT NULL,
	phone TEXT NOT NULL,
	address TEXT NOT NULL,
    clientid INT PRIMARY KEY
);

CREATE TABLE client_consultation (
    clientid INT NOT NULL,
    consultation TEXT NOT NULL,
    cdate TEXT NOT NULL PRIMARY KEY,
    FOREIGN KEY (clientid) REFERENCES client(clientid)
);

CREATE TABLE g_code (
    gdate TEXT NOT NULL,
    title TEXT NOT NULL PRIMARY KEY,
    code TEXT NOT NULL
);

CREATE TABLE orders (
    odate TEXT NOT NULL,
    items TEXT NOT NULL,
    notes TEXT,
    price DOUBLE NOT NULL,
    shipping DOUBLE NOT NULL,
    tax DOUBLE NOT NULL,
    completed TEXT NOT NULL,
    wixinvoicenumber TEXT NOT NULL PRIMARY KEY,
    clientid INT NOT NULL
);

INSERT INTO login VALUES('jearnhart', 'password');
