MobilHackathon2014
==================

This repository includes all sources developed during Mobil Hackathon 2014 which is organised by Google Developer Group (GDG) Ankara


For Android phone/tablet application;
* First copy v7-appcompat and then copy your applications (because applications search for appcompat on ../ folder)
* Copy projects on eclipse adt via New -> Project -> Android Project from Existing Code
* Generated R class may be imported in android.R package, if yes replace "import android.R" with your package e.g. "import edu.boun.test"

For Google Glass project
* Create projects on eclipse adt via New -> Project -> Android Project from Existing Code
* Generally eclipse brokes the following line in project.properties file, if yes please use this one
target=Google Inc.:Glass Development Kit Preview:19
