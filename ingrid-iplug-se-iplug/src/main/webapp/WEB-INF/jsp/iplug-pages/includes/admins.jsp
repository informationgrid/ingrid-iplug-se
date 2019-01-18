<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
  --%>
<%@ include file="/WEB-INF/jsp/base/include.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<div id="loading">
    <p>Admins werden geladen ...</p>
</div>

<div class="hint" onclick="$('#filterComment').toggle()"><span class="ui-icon ui-icon-arrow-1-e"></span>Hinweis</div>
<div id="filterComment" class="comment" style="display: none;">
    Instanz-Administratoren haben nur Zugriff auf die Instanz in der sie angelegt wurden.
    
    Innerhalb der Instanz haben die Instanz-Administratoren <strong>keinen</strong> Zugriff auf:
    <ul>
    <li>Konfiguration</li>
    <li>Administratoren</li>
    </ul>
    
    Außerdem dürfen die Instanz-Administratoren keine neuen Instanzen anlegen oder löschen.

</div>

<div id="adminContent" style="visibility: hidden;">

    <!-- pager -->
    <div id="pager" class="pager left">
        <form>
            <img src="../img/first.png" class="first" />
            <img src="../img/prev.png" class="prev" />
            <span class="pagedisplay"></span>
            <!-- this can be any element, including an input -->
            <img src="../img/next.png" class="next" />
            <img src="../img/last.png" class="last" />
            ( pro Seite:
            <select class="pagesize" style="width: 45px;">
                <option value="10">10</option>
                <option value="20">20</option>
                <option value="50">50</option>
                <option value="100">100</option>
            </select>
            )
        </form>
    </div>

    <button id="btnAddAdmin" type="button" name="add" class="right" style="margin-top: 8px;">Neuer Administrator</button>

    <table id="adminTable" class="data tablesorter space">
        <thead>
            <tr>
                <th data-sort="string" width="20px"></th>
                <th data-sort="string">Login</th>
                <th data-sort="string" width="100px"></th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>

    <button id="btnDeleteAdmins" type="button" name="delete" class="left">Administratoren löschen</button>
    <div style="clear:both;"></div>

    <div id="dialog-form" title="Neuen Administrator anlegen">
        <form>
            <fieldset>
                <div>
                    <h3>Login</h3>
                    <div class="input full">
                        <input type="text" name="login" id="login" maxlength="128" value="" class="text ui-widget-content ui-corner-all" />
                    </div>
                    <div class="fieldhint" >Das Login muss mind. 3 Zeichen lanmg sein. Erlaubte Zeichen sind: a-z, A-Z, 0-9, '_', '-'.</div>
                    <span id="errorLoginFormat" class="error login format">Das Login enthält unerlaubte Zeichen. Erlaubte Zeichen sind: a-z, A-Z, 0-9, '_', '-'. Es muss mind. 3 Zeichen lang sein.</span>
                    <span id="errorLoginDuplicate" class="error login duplicate">Das Login ist schon vorhanden.</span>
                </div>

                <div>
                    <h3>Passwort</h3>
                    <div class="input full">
                        <input type="text" name="password" id="password" value="" maxlength="128" class="text ui-widget-content ui-corner-all">
                    </div>
                    <div class="fieldhint" >Passwort muss aus mind. 4 Zeichen bestehen und darf max. 128 Zeichen lang sein.</div>
                    <span id="errorPassword" class="error password">Passwort muss aus mind. 4 Zeichen bestehen und darf max. 128 Zeichen lang sein. </span>
                </div>
            </fieldset>
        </form>
    </div>

    <div id="dialog-confirm" title="Wirklich löschen?">
        <p>
            <span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
            Möchten Sie die Administratoren wirklich löschen?
        </p>
    </div>

</div>

<div id="waitScreen" style="display: none;">
    <div class="blocker"></div>
    <div class="text">Bitte warten ...</div>
</div>
