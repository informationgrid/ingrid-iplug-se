<%@ include file="/WEB-INF/jsp/base/include.jsp"%>

<div id="bd">
    <div id="yui-main">
        <div class="yui-b">
            <h3>
                <fmt:message key="scheduling.headline" />
            </h3>
            <c:if test="${!empty savedPattern}">
                <div class="row">
                    <form action="delete.html" method="post">
                        <input type="hidden" name="instance" value="${instance.name}" />
                        <label> <fmt:message key="scheduling.cronPattern" /> :
                        </label> &nbsp;${savedPattern}
                        <input type="image" src="../img/delete.png" title="L�schen" align="absmiddle">
                    </form>
                </div>
                <div class="row">
                    <label> <fmt:message key="scheduling.crawlParams" /> :
                    </label> &nbsp;${savedCrawlData}
                </div>
            </c:if>

            <div style="margin-top: 25px"></div>

            <div id="schedulingTabs" class="tab-container">
                <ul class="etabs">
                    <li class="tab"><a href="#tab1"><em><fmt:message key="scheduling.day" /></em></a></li>
                    <li class="tab"><a href="#tab2"><em><fmt:message key="scheduling.week" /></em></a></li>
                    <li class="tab"><a href="#tab3"><em><fmt:message key="scheduling.month" /></em></a></li>
                    <li class="tab"><a href="#tab4"><em><fmt:message key="scheduling.advanced" /></em></a></li>
                </ul>
                <div class="panel-container">
                    <div id="tab1">
                        <p>
                            <form:form action="daily.html" method="post" commandName="clockCommand">
                                <input type="hidden" name="instance" value="${instance.name}" />
                                <fieldset>
                                    <legend>
                                        <fmt:message key="scheduling.daily" />
                                    </legend>
                                    <row> 
                                        <label>um:</label>
                                        <div class="input inline">
                                            <form:input type="text" class="time" path="time" />
                                        </div>
                                    </row>
                                </fieldset>
                                <fieldset>
                                    <legend>
                                        <fmt:message key="scheduling.crawlParams" />
                                    </legend>

                                    <row> <label> <fmt:message key="scheduling.crawlDepth" /> :
                                    </label> <field> 
                                    <div class="input full">
                                        <form:select path="depth">
                                            <form:options items="${depths}" />
                                        </form:select>
                                    </div>
                                    <div class="error">
                                        <form:errors path="depth" />
                                    </div>
                                    </field> <desc></desc> </row>

                                    <row> <label> <fmt:message key="scheduling.pagesPerSegment" /> :
                                    </label> <field> 
                                    <div class="input full">
                                        <form:input path="topn" />
                                    </div>
                                    <div class="error">
                                        <form:errors path="topn" />
                                    </div>
                                    </field> <desc></desc> </row>
                                </fieldset>

                                <input type="submit" value="<fmt:message key="button.save" />" />

                            </form:form>
                        </p>
                    </div>
                    <div id="tab2">
                        <p>

                            <form:form action="weekly.html" method="post" commandName="weeklyCommand">
                                <input type="hidden" name="instance" value="${instance.name}" />
                                <fieldset>
                                    <legend>
                                        <fmt:message key="scheduling.weekly" />
                                    </legend>

                                    <row> <label>um:</label>
                                        <div class="input inline">
                                            <form:input type="text" class="time" path="time" />
                                        </div>
                                    </row>
                                    <row> <label> <fmt:message key="scheduling.weekdays" /> :</label>
                                    <div style="clear:left;"></div>
                                    <field id="weekDays"> <c:forEach var="day" items="${days}">
                                        <div style="float: left; margin: 2px 2px 0 0;">
                                            <input type="checkbox" name="days" value="${day}"
                                                id="dayOfWeekButtons_${day}" />
                                            <label><fmt:message key="scheduling.day.${day}"/></label>
                                        </div>
                                    </c:forEach></field></row>
                                </fieldset>

                                <fieldset>
                                    <legend>
                                        <fmt:message key="scheduling.crawlParams" />
                                    </legend>

                                    <row> <label> <fmt:message key="scheduling.crawlDepth" /> :
                                    </label> <field> 
                                    <div class="input full">
                                        <form:select path="depth">
                                            <form:options items="${depths}" />
                                        </form:select>
                                    </div>
                                    <div class="error">
                                        <form:errors path="depth" />
                                    </div>
                                    </field> <desc></desc> </row>

                                    <row> <label> <fmt:message key="scheduling.pagesPerSegment" /> :
                                    </label> <field> 
                                    <div class="input full">
                                        <form:input path="topn" />
                                    </div>
                                    <div class="error">
                                        <form:errors path="topn" />
                                    </div>
                                    </field> <desc></desc> </row>
                                </fieldset>

                                <input type="submit" value="<fmt:message key="button.save" />" />
                            </form:form>

                        </p>
                    </div>
                    <div id="tab3">
                        <p>
                            <form:form action="monthly.html" method="post" commandName="monthlyCommand">
                                <input type="hidden" name="instance" value="${instance.name}" />
                                <fieldset>
                                    <legend>
                                        <fmt:message key="scheduling.monthly" />
                                    </legend>

                                    <row> <label>um:</label>
                                        <div class="input inline">
                                            <form:input type="text" class="time" path="time" />
                                        </div>
                                    </row>
                                    <row> <label> <fmt:message key="scheduling.atDays" /> :</label>
                                    <field id="monthDays"> <c:forEach items="${month}" var="dayOfMonth">
                                        <div style="float: left; margin: 2px 2px 0 0;">
                                            <input type="checkbox" name="daysOfMonth" value="${dayOfMonth}"
                                                id="daysOfMonth_${dayOfMonth}" />
                                            <label><fmt:formatNumber value="${dayOfMonth}" pattern="00"/></label>
                                        </div>
                                    </c:forEach> 
                                    <div style="clear:left;"></div>
                                    </field>
                                    </row>
                                </fieldset>

                                <fieldset>
                                    <legend>
                                        <fmt:message key="scheduling.crawlParams" />
                                    </legend>

                                    <row> <label> <fmt:message key="scheduling.crawlDepth" /> :
                                    </label> <field> 
                                    <div class="input full">
                                        <form:select path="depth">
                                            <form:options items="${depths}" />
                                        </form:select>
                                    </div>
                                    <div class="error">
                                        <form:errors path="depth" />
                                    </div>
                                    </field> <desc></desc> </row>

                                    <row> <label> <fmt:message key="scheduling.pagesPerSegment" /> :
                                    </label> <field> 
                                    <div class="input full">
                                        <form:input path="topn" />
                                    </div>
                                    <div class="error">
                                        <form:errors path="topn" />
                                    </div>
                                    </field> <desc></desc> </row>
                                </fieldset>

                                <input type="submit" value="<fmt:message key="button.save" />" />

                            </form:form>
                        </p>
                    </div>
                    <div id="tab4">
                        <p>
                            <form:form action="advanced.html" method="post" commandName="advancedCommand">
                                <input type="hidden" name="instance" value="${instance.name}" />
                                <fieldset>
                                    <legend>
                                        <fmt:message key="scheduling.advanced" />
                                    </legend>

                                    <row> <label> <fmt:message key="scheduling.cronPattern" /> :
                                    </label> <field>
                                    <div class="input full">
                                        <form:input path="pattern" />
                                    </div>
                                    <div class="error">
                                        <form:errors path="pattern" />
                                    </div>
                                    <div>
                                        <fmt:message key="scheduling.cronPatternDescription" />
                                    </div>
                                    </field> </row>
                                    <row> <desc> <fmt:message key="scheduling.cronPatternExample" /></desc> </row>
                                </fieldset>

                                <fieldset>
                                    <legend>
                                        <fmt:message key="scheduling.crawlParams" />
                                    </legend>

                                    <row> <label> <fmt:message key="scheduling.crawlDepth" /> :
                                    </label> <field>
                                    <div class="input full">
                                        <form:select path="depth">
                                            <form:options items="${depths}" />
                                        </form:select>
                                    </div>
                                    <div class="error">
                                        <form:errors path="depth" />
                                    </div>
                                    </field> <desc></desc> </row>

                                    <row> <label> <fmt:message key="scheduling.pagesPerSegment" /> :
                                    </label> <field>
                                    <div class="input full">
                                        <form:input path="topn" />
                                    </div>
                                    <div class="error">
                                        <form:errors path="topn" />
                                    </div>
                                    </field> <desc></desc> </row>
                                </fieldset>
                                <input type="submit" value="<fmt:message key="button.save" />" />
                            </form:form>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
