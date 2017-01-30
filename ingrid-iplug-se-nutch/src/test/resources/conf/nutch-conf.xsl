<?xml version="1.0"?>
<!--
  **************************************************-
  ingrid-iplug-se-nutch
  ==================================================
  Copyright (C) 2014 - 2017 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.1 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  
  http://ec.europa.eu/idabc/eupl5
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.
  **************************************************#
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/>
<xsl:template match="nutch-conf">
<html>
<body>
<table border="1">
<tr>
 <td>name</td>
 <td>value</td>
 <td>description</td>
</tr>
<xsl:for-each select="property">
<tr>
  <td><xsl:value-of select="name"/></td>
  <td><xsl:value-of select="value"/></td>
  <td><xsl:value-of select="description"/></td>
</tr>
</xsl:for-each>
</table>
</body>
</html>
</xsl:template>
</xsl:stylesheet>
