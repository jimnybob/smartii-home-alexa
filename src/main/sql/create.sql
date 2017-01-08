CREATE TABLE IF NOT EXISTS Room (
  id       INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  name     VARCHAR(256)                       NOT NULL,
  httpPort INTEGER                            NOT NULL
);
CREATE TABLE IF NOT EXISTS Appliance (
  id          INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  applianceId VARCHAR(256)                       NOT NULL,
  name        VARCHAR(100)                       NOT NULL,
  description VARCHAR(256)                       NOT NULL,
  roomId      INTEGER                            NOT NULL,
  FOREIGN KEY (roomId) REFERENCES Room (id)
);
CREATE TABLE IF NOT EXISTS ApplianceMappingEvents (
  id                 INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  action             VARCHAR(50)                        NOT NULL,
  eventOrder         INTEGER                            NOT NULL,
  applianceMappingId INTEGER                            NOT NULL,
  FOREIGN KEY (applianceMappingId) REFERENCES Appliance (id)
);
-- CREATE TABLE IF NOT EXISTS SleepEvent (
--   id                       INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
--   seconds                  INTEGER                            NOT NULL,
--   applianceMappingEventsId INTEGER                            NOT NULL,
--   applianceMappingId       INTEGER                            NOT NULL,
--   FOREIGN KEY (applianceMappingEventsId) REFERENCES ApplianceMappingEvents (id),
--   FOREIGN KEY (applianceMappingId) REFERENCES Appliance (id)
-- );
CREATE TABLE IF NOT EXISTS HttpCallEvent (
  id                       INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  method                   VARCHAR(10)                        NOT NULL,
  path                     VARCHAR(256)                       NOT NULL,
  applianceMappingEventsId INTEGER                            NOT NULL,
  applianceMappingId       INTEGER                            NOT NULL,
  delay                    INTEGER,
  delayUnits               VARCHAR(10),
  FOREIGN KEY (applianceMappingEventsId) REFERENCES ApplianceMappingEvents (id),
  FOREIGN KEY (applianceMappingId) REFERENCES Appliance (id)
);

