@echo on

mvn install:install-file -Dfile=C:\java\desarrollo\openData\lib\codice-2.8.0.jar -DgroupId=ext.place.codice.common -DartifactId=codice -Dversion=2.8.0 -Dpackaging=jar

mvn install:install-file -Dfile=C:\java\desarrollo\openData\lib\codice_place_ext-1.4.0.jar -DgroupId=ext.place.codice.common -DartifactId=codice_place_ext -Dversion=1.4.0 -Dpackaging=jar

mvn install:install-file -Dfile=C:\java\desarrollo\openData\lib\atom-1.0.jar -DgroupId=org.w3._2005.atom -DartifactId=atom -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=C:\java\desarrollo\openData\lib\codice_place_ext-1.3.jar -DgroupId=ext.place.codice.common -DartifactId=codice_place_ext -Dversion=1.3 -Dpackaging=jar

mvn install:install-file -Dfile=C:\java\desarrollo\openData\lib\codice-2.7.0.jar -DgroupId=ext.place.codice.common -DartifactId=codice -Dversion=2.7.0 -Dpackaging=jar

pause