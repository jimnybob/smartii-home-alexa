INSERT INTO `Room`(`id`,`name`) VALUES (1,'Kitchen');

INSERT INTO `ApplianceMapping`(`id`,`applianceId`,`roomId`) VALUES (1,'kitchenHiFi',1);

INSERT INTO `ApplianceMappingEvents`(`id`,`action`,`eventOrder`,`applianceMappingId`) VALUES (1,'turnOn',0,1),(2,'turnOn',1,1),(3,'turnOn',2,1);

INSERT INTO `HttpCallEvent`(`id`,`method`,`path`,`applianceMappingEventsId`) VALUES (1,'GET','/hifi/AUX',1),(2,'GET','/hifi/POWER',3);

INSERT INTO `SleepEvent`(`id`,`seconds`,`applianceMappingEventsId`) VALUES (1,3,2);