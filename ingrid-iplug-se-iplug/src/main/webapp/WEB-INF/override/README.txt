Use this folder to override the default spring configuration.
Add a *.xml file containing the beans to use and overriding the default beans (e.g. add an additional mapper when mapping records to index ...).
When updating the iPlug with a new version this directory is kept and functionality will not be lost.

NOTICE:
Nevertheless the bean definition in the *.xml files have to match the according java classes. If these classes change with a new version of the iPlug the bean definition has to be adapted.
