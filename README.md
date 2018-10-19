WebDAV Server Plugin for oXygen XML WebApp
===============================================

This project is a simple oXygen XML WebApp plugin that provides a WebDAV enabled workspace by registering the Tomcat builtin WebDAV servlet to manage a folder.

###**Mapping mechanism**
This plugin provides a mapping mechanism that hides the real path relative to **webdav-server** folder by defining **"mapping=real-path"** mappings in the **mapping.properties** file.

The root path(**"/"**) is by default mapped to the **samples** folder but can be overriden by another mapping "(**/=another-folder**)".

One can create a folder located at *"webdav-server/users-dir/John_Doe/projectX"* and map it to a random string **1234asdf1234** and making this project's folder accessible to colaborators as easy as adding a new mapping **1234asdf1234=webdav-server/users-dir/John_Doe/projectX** but leaving it's parent folder hidden all that is accesible to the users having this link is the shared folder and the folder mapped to root(*"/"*).

This folder will be accessible only to users with this link **http://_[oxygen-webapp-host]_/plugins-dispatcher/webdav-server/1234asdf1234/** but the parent folder of this shared folder will be cosidered the root, just as a *1234asdf1234* folder was inside *webdav-server*.

Copyright and License
---------------------
Copyright 2018 Syncro Soft SRL.

This project is licensed under [Apache License 2.0](https://github.com/oxygenxml/web-author-webdav-server-plugin/blob/master/LICENSE)
