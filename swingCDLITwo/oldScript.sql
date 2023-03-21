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

INSERT INTO login VALUES('user', 'pass');

INSERT INTO client VALUES('Emma','Johnson','emmajohnson@gmail.com','7035551234','102 Main St',1200);
INSERT INTO client VALUES('Olivia','Smith','oliviasmith@gmail.com','7035551235','103 Main St',1250);
INSERT INTO client VALUES('Ava','Brown','avabrown@gmail.com','7035551236','104 Main St',1275);
INSERT INTO client VALUES('Isabella','Davis','isabelladavis@gmail.com','7035551237','105 Main St',1300);
INSERT INTO client VALUES('Sophia','Wilson','sophiawilson@gmail.com','7035551238','106 Main St',1350);
INSERT INTO client VALUES('Mia','Anderson','miaanderson@gmail.com','7035551239','107 Main St',1400);
INSERT INTO client VALUES('Charlotte','Thomas','charlottethomas@gmail.com','7035551240','108 Main St',1450);
INSERT INTO client VALUES('Amelia','Jackson','ameliajackson@gmail.com','7035551241','109 Main St',1500);
INSERT INTO client VALUES('Harper','White','harperwhite@gmail.com','7035551242','110 Main St',1550);
INSERT INTO client VALUES('Evelyn','Harris','evelynharris@gmail.com','7035551243','111 Main St',1600);
INSERT INTO client VALUES('Abigail','Martin','abigailmartin@gmail.com','7035551244','112 Main St',1650);
INSERT INTO client VALUES('Emily','Thompson','emilythompson@gmail.com','7035551245','113 Main St',1700);
INSERT INTO client VALUES('Elizabeth','Garcia','elizabethgarcia@gmail.com','7035551246','114 Main St',1750);
INSERT INTO client VALUES('Mila','Martinez','milamartinez@gmail.com','7035551247','115 Main St',1800);
INSERT INTO client VALUES('Avery','Robinson','averyrobinson@gmail.com','7035551248','116 Main St',1850);
INSERT INTO client VALUES('Ella','Clark','ellaclark@gmail.com','7035551249','117 Main St',1900);
INSERT INTO client VALUES('Scarlett','Rodriguez','scarlettrodriguez@gmail.com','7035551250','118 Main St',1950);
INSERT INTO client VALUES('Grace','Lewis','gracelewis@gmail.com','7035551251','119 Main St',2000);
INSERT INTO client VALUES('Natalie','Lee','natalilee@gmail.com','7035551252','120 Main St',2050);
INSERT INTO client VALUES('Aubrey','Walker','aubreywalker@gmail.com','7035551253','121 Main St',2100);
INSERT INTO client VALUES('Aria','Hall','ariahall@gmail.com','7035551254','122 Main St',2150);
INSERT INTO client VALUES('Adalynn','Allen','adalynnallen@gmail.com','7035551255','123 Main St',2200);
INSERT INTO client VALUES('Riley','King','rileyking@gmail.com','7035551256','124 Main St',2250);
INSERT INTO client VALUES('Brooklyn','Wright','brooklynwright@gmail.com','7035551257','125 Main St',2300);
INSERT INTO client VALUES('Ellie','Lopez','ellielopez@gmail.com','7035551258','126 Main St',2350);
INSERT INTO client VALUES('Audrey','Hill','audreyhill@gmail.com','7035551259','127 Main St',2400);
INSERT INTO client VALUES('Leah','Green','leahgreen@gmail.com','7035551260','128 Main St',2450);
INSERT INTO client VALUES('Allison','Adams','allisonadams@gmail.com','7035551261','129 Main St',2500);
INSERT INTO client VALUES('Savannah','Nelson','savannahnelson@gmail.com','7035551262','130 Main St',2550);
INSERT INTO client VALUES('Aaliyah','Carter','aaliyahcarter@gmail.com','7035551263','131 Main St',2600);
INSERT INTO client VALUES('Arianna','Mitchell','ariannamitchell@gmail.com','7035551264','132 Main St',2650);
INSERT INTO client VALUES('Camila','Perez','camilaperez@gmail.com','7035551265','133 Main St',2700);
INSERT INTO client VALUES('Kaylee','Roberts','kayleeroberts@gmail.com','7035551266','134 Main St',2750);
INSERT INTO client VALUES('Aurora','Turner','auroraturner@gmail.com','7035551267','135 Main St',2800);
INSERT INTO client VALUES('Hailey','Phillips','haileyphillips@gmail.com','7035551268','136 Main St',2850);
INSERT INTO client VALUES('Hazel','Campbell','hazelcampbell@gmail.com','7035551269','137 Main St',2900);
INSERT INTO client VALUES('Nevaeh','Parker','nevaehparker@gmail.com','7035551270','138 Main St',2950);
INSERT INTO client VALUES('Paisley','Evans','paisleyevans@gmail.com','7035551271','139 Main St',3000);
INSERT INTO client VALUES('Makayla','Edwards','makaylaedwards@gmail.com','7035551272','140 Main St',3050);
INSERT INTO client VALUES('Maria','Collins','mariacollins@gmail.com','7035551273','141 Main St',3100);
INSERT INTO client VALUES('Naomi','Stewart','naomistewart@gmail.com','7035551274','142 Main St',3150);
INSERT INTO client VALUES('Aubree','Sanchez','aubreesanchez@gmail.com','7035551275','143 Main St',3200);
INSERT INTO client VALUES('Brooklynn','Morris','brooklynnmorris@gmail.com','7035551276','144 Main St',3250);
INSERT INTO client VALUES('Brielle','Rogers','briellerogers@gmail.com','7035551277','145 Main St',3300);
INSERT INTO client VALUES('Adalyn','Reed','adalynreed@gmail.com','7035551278','146 Main St',3350);
INSERT INTO client VALUES('Everly','Cook','everlycook@gmail.com','7035551279','147 Main St',3400);
INSERT INTO client VALUES('Ariella','Bailey','ariellabailey@gmail.com','7035551280','148 Main St',3450);
INSERT INTO client VALUES('Avery','Bell','averybell@gmail.com','7035551281','149 Main St',3500);
INSERT INTO client VALUES('Adelynn','Cooper','adelynncooper@gmail.com','7035551282','150 Main St',3550);
INSERT INTO client VALUES('Aria','Richardson','ariarichardson@gmail.com','7035551283','151 Main St',3600);
INSERT INTO client VALUES('Alaina','Cox','alainacox@gmail.com','7035551284','152 Main St',3650);
INSERT INTO client VALUES('Raelynn','Howard','raelynnhoward@gmail.com','7035551285','153 Main St',3700);
INSERT INTO client VALUES('Lydia','Ward','lydiaward@gmail.com','7035551286','154 Main St',3750);
INSERT INTO client VALUES('Annabelle','Torres','annabelletorres@gmail.com','7035551287','155 Main St',3800);
INSERT INTO client VALUES('Ariel','Peterson','arielpeterson@gmail.com','7035551288','156 Main St',3850);
INSERT INTO client VALUES('Aurora','Gray','auroragray@gmail.com','7035551289','157 Main St',3900);
INSERT INTO client VALUES('Scarlet','James','scarletjames@gmail.com','7035551290','158 Main St',3950);
INSERT INTO client VALUES('Audrina','Watson','audrinawatson@gmail.com','7035551291','159 Main St',4000);
INSERT INTO client VALUES('Ayla','Brooks','aylabrooks@gmail.com','7035551292','160 Main St',4050);
INSERT INTO client VALUES('Callie','Kelly','calliekelly@gmail.com','7035551293','161 Main St',4100);
INSERT INTO client VALUES('Cadence','Sanders','cadencesanders@gmail.com','7035551294','162 Main St',4150);
INSERT INTO client VALUES('Daleyza','Price','daleyzaprice@gmail.com','7035551295','163 Main St',4200);
INSERT INTO client VALUES('Diana','Bennett','dianabennett@gmail.com','7035551296','164 Main St',4250);
INSERT INTO client VALUES('Eleanor','Wood','eleanorwood@gmail.com','7035551297','165 Main St',4300);
INSERT INTO client VALUES('Eliana','Barnes','elianabarnes@gmail.com','7035551298','166 Main St',4350);
INSERT INTO client VALUES('Eden','Ross','edenross@gmail.com','7035551299','167 Main St',4400);
INSERT INTO client VALUES('Emery','Henderson','emeryhenderson@gmail.com','7035551300','168 Main St',4450);
INSERT INTO client VALUES('Braelynn','Coleman','braelynncoleman@gmail.com','7035551301','169 Main St',4500);
INSERT INTO client VALUES('Briella','Jenkins','briellajenkins@gmail.com','7035551302','170 Main St',4550);
INSERT INTO client VALUES('Bridgette','Powell','bridgettepowell@gmail.com','7035551303','171 Main St',4600);
INSERT INTO client VALUES('Carly','Sullivan','carlysullivan@gmail.com','7035551304','172 Main St',4650);
INSERT INTO client VALUES('Carmen','Russell','carmenrussell@gmail.com','7035551305','173 Main St',4700);
INSERT INTO client VALUES('Charlie','Ortiz','charlieortiz@gmail.com','7035551306','174 Main St',4750);
INSERT INTO client VALUES('Claire','Gutierrez','clairegutierrez@gmail.com','7035551307','175 Main St',4800);
INSERT INTO client VALUES('Cora','Ramirez','coraramirez@gmail.com','7035551308','176 Main St',4850);
INSERT INTO client VALUES('Emmalyn','Fleming','emmalynfleming@gmail.com','7035551309','177 Main St',4900);
INSERT INTO client VALUES('Evelynn','Vega','evelynnvega@gmail.com','7035551310','178 Main St',4950);
INSERT INTO client VALUES('Gemma','Snyder','gemmasnyder@gmail.com','7035551311','179 Main St',5000);
INSERT INTO client VALUES('Gia','Horton','giahorton@gmail.com','7035551312','180 Main St',5050);
