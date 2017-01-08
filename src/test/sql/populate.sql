INSERT INTO `Room`(`id`,`name`,`httpPort`) VALUES (1,'Kitchen',9000);

INSERT INTO `Appliance`(`id`,`applianceId`,`name`,`description`,`roomId`) VALUES (1,'kitchenHiFi','kitchen hifi','The HiFi in the kitchen',1);

INSERT INTO `ApplianceMappingEvents`(`id`,`action`,`eventOrder`,`applianceMappingId`) VALUES (1,'turnOn',0,1),(2,'turnOn',1,1);

INSERT INTO `HttpCallEvent`(`id`,`method`,`path`,`applianceMappingEventsId`,`applianceMappingId`) VALUES (1,'GET','/hifi/AUX',1, 1);

INSERT INTO `HttpCallEvent`(`id`,`method`,`path`,`applianceMappingEventsId`,`applianceMappingId`,`delay`,`delayUnits`) VALUES (2,'GET','/hifi/POWER',2, 1, 3,'SECONDS');

-- INSERT INTO `SleepEvent`(`id`,`seconds`,`applianceMappingEventsId`,`applianceMappingId`) VALUES (1,3,2,1);