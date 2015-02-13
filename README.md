[![Build Status](https://travis-ci.org/fmaul/alfresco-default-user-avatars.svg?branch=master)](https://travis-ci.org/fmaul/alfresco-default-user-avatars)

Alfresco component to create default user avatars for Alfresco
==============================================================

Author: Florian Maul

This project creates avatar images with the user's initals and a colored background
for all user who have not defined a custom avatar image.

Download
--------

See the releases section in Github for AMP downloads: https://github.com/fmaul/alfresco-default-user-avatars/releases


Installation
------------

The component has been developed to install on top of an existing Alfresco
4.0, 4.1 or 4.2 installation. The alfresco-default-user-avatars-<version>.amp needs
to be installed into the Alfresco Repository webapp using the Alfresco Module Management Tool:

    java -jar alfresco-mmt.jar install alfresco-default-user-avatars-<version>.amp /path/to/alfresco.war
  
You can also use the Alfresco Maven SDK to install or overlay the AMP during the build of a
Repository WAR project. See https://artifacts.alfresco.com/nexus/content/repositories/alfresco-docs/alfresco-lifecycle-aggregator/latest/plugins/alfresco-maven-plugin/advanced-usage.html
for details.


Building
--------

To build the module and its AMP / JAR files, run the following command from the base 
project directory:

    mvn install

The command builds the alfresco-default-user-avatars-<version>.amp in the 'target' directory within your project.

To hotdeploy to a local alfresco installation you can use the alfresco:install
command to deploy the alfresco-default-user-avatars.amp directly to a WAR file or an exploded war folder:

    mvn package alfresco:install -Dmaven.alfresco.warLocation=/path/to/tomcat/webapps/alfresco.war


Using the component
-------------------

Upon startup of Alfresco a patch is run and for all users without avatar images a new
avatar image is generated. When a user updates her name the images are automatically 
regenerated.


Configuration options
---------------------

The following configuration options are supported by this extension and can be added to
alfresco-global.properties (below are the defaults):

### users.default.avatars.enabled
Can be used to disable the automatic generation of user images

    users.default.avatars.enabled=true

### users.default.avatars.palette
The color palette that is used for background colors
    
    users.default.avatars.palette=#5191FD,#1C54F5,#F6A615,#D82732,#0DA921,#F35E4C,#A64847

### users.default.avatars.blackFontForLightBackgrounds

By default only white is used for the font in the avatar images, if the there are very bright colors in the palette, then enabling blackFontForLightBackgrounds will use black as foreground color, if the color is very light.

    users.default.avatars.blackFontForLightBackgrounds=false

### users.default.avatars.showInitials

Show initals of firstname and surname. Set to false to only a single letter for the first name

    users.default.avatars.showInitials=true

