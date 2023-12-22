Create table IF NOT EXISTS card (
  name VARCHAR(100) NOT NULL PRIMARY KEY,
  cost VARCHAR(100),
  supertypes VARCHAR(150),
  types VARCHAR(150),
  subtypes VARCHAR(150),
  text VARCHAR(2000),
  power INT,
  toughness INT,
  multiverseid INT
);
