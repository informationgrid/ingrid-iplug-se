<%--
  **************************************************-
  ingrid-iplug-se-iplug
  ==================================================
  Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
<%@ page contentType="text/html; charset=UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="DatabaseConfig.main.title" /> -
	Instanzkonfiguration</title>
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css"
	media="all" />
<link rel="StyleSheet" href="../css/jquery-ui.min.css" type="text/css"
	media="all" />
<link rel="StyleSheet" href="../css/chosen.min.css" type="text/css"
	media="all" />
<link rel="StyleSheet" href="../css/jquery.tablesorter.pager.css"
	type="text/css" media="all" />

<link rel="StyleSheet" href="../css/se_styles.css" type="text/css"
	media="all" />

<script type="text/javascript" src="../js/base/jquery-1.8.0.min.js"></script>
<script type="text/javascript" src="../js/jquery-ui.min.js"></script>
<script type="text/javascript" src="../js/jquery.easytabs.min.js"></script>
<script type="text/javascript"
	src="../js/jquery.tablesorter.full.min.js"></script>
<script type="text/javascript"
	src="../js/jquery.tablesorter.widgets.min.js"></script>
<script type="text/javascript"
	src="../js/jquery.tablesorter.pager.min.js"></script>
<script type="text/javascript" src="../js/chosen.jquery.min.js"></script>


<script type="text/javascript">
	$(document).ready(function() {

		$('#configTabs').easytabs({
			animate : false
		});

		$('#configTabs').bind('easytabs:after', function() {
			getStatistic();
		});

		getStatistic();

	}

	);

	function getStatistic() {
		statisticIsUpdated = false;
		// fill table
		var addTableRow = function(item, biggest) {
			var knownWidth = (item.known / biggest.known) * 100;
			var fetchedWidth = (item.fetched / item.known) * knownWidth;
			var toFetchWidth = knownWidth - fetchedWidth;
			$("#statisticTable tbody")
					.append(
							"<tr>"
									+ "<td title='rot=noch nicht analysiert; grün=analysiert'><span style='display: inline-block; background-color: green; height: 3px; width: " + fetchedWidth + "px'></span><span style='display: inline-block; background-color: red; height: 3px; width: " + toFetchWidth + "px'></span></td>"
									+ "<td>" + item.host + "</td>" + "<td>"
									+ item.known + "</td>" + "<td>"
									+ item.fetched + "</td>" + "<td>"
									+ item.ratio + "</td>" + "</tr>");
		};

		$.ajax("../rest/status/${ instance.name }/statistic", {
			type : "GET",
			contentType : 'application/json',
			success : function(data) {
				if (!data) {
					$("#statisticTable").hide();
					$("#overallStatistic").hide();
					return;
				} else {
					$("#statisticTable").show();
					$("#overallStatistic").show();
				}

				var json = JSON.parse(data);
				var overall = json.splice(0, 1);
				// var labels = [], dataKnown = [], dataFetched = [];

				// determine highest known and fetched values
				var biggest = {
					known : -1,
					fetched : -1
				};
				$.each(json, function(index, item) {
					if (item.known > biggest.known)
						biggest.known = item.known;
					if (item.fetched > biggest.fetched)
						biggest.fetched = item.fetched;
				});

				// remove all rows first
				$("#statisticTable tbody tr").remove();
				$.each(json, function(index, item) {
					// labels.push( item.host );
					// dataKnown.push( item.known );
					// dataFetched.push( item.fetched );
					addTableRow(item, biggest);
				});

				$("#overallStatistic .known").text(overall[0].known);
				$("#overallStatistic .fetched").text(overall[0].fetched);

				$("#statisticTable").tablesorter({
					headers : {
						0 : {
							sorter : false
						}
					},
					widgets : [ 'zebra' ],
				});
			}

		});
	}
</script>

<script type="text/javascript">
	$(document)
			.ready(
					function() {
						var pagerOptions = {
							container : $(".pager"),
							output : '{startRow} bis {endRow} von {filteredRows} URLs',
							//size: 2,
							savePages : false,
							ajaxUrl : '../rest/urlerrors/${instance.name}?page={page}',
							serverSideSorting : false,
							ajaxObject : {
								dataType : 'json'
							},
							ajaxProcessing : function(data) {
								if (data) {// && data.hasOwnProperty('rows')) {
									var r, d = data.data,
									// total number of rows (required)
									total = data.totalUrls,
									// array of header names (optional)
									//headers = data.headers,
									// all rows: array of arrays; each internal array has the table cell data for that row
									rows = "",
									// len should match pager set size (c.size)
									len = d.length;
									// this will depend on how the json is set up - see City0.json
									// rows
									for (r = 0; r < len; r++) {
										// row = ""; // new row array
										// cells
										rows += "<tr data-id='" + d[r].id + "'>"
												+ "<td></td>"
												+ "<td>"
												+ d[r].url
												+ "</td>"
												+ "<td>"
												+ d[r].msg.toUpperCase()
												+ "</td>" + "</tr>";

										//rows.push(row); // add new row array to rows array

									}
									//setTimeout( function() { createActionButton( $("tr .btnUrl") ); }, 0);

									// in version 2.10, you can optionally return $(rows) a set of table rows within a jQuery object
									return [ total, $(rows) ];
								}
							},
							customAjaxUrl : function(table, url) {
								// manipulate the url string as you desire
								var statusfilter = $("#urlTable").data().statusfilter;
								var urlfilter = $("#urlTable").data().urlfilter;
								var sort = $('#urlTable').data().tablesorter.sortList[0];

								if (statusfilter)
									url += "&statusfilter=" + statusfilter;
								if (urlfilter)
									url += "&urlfilter=" + urlfilter;
								if (sort)
									url += "&sort=" + sort.join(',');
								url += "&pagesize=" + this.size;

								return encodeURI(url);
							}
						};

						// initialize the table and its paging option
						$("#urlTable").tablesorter({
							delayInit : true,
							headers : {
								0 : {
									sorter : false
								},
								1 : {
									sorter : false
								},
								2 : {
									sorter : false
								}
							},
							widgets : [ 'zebra', 'filter' ],
							widgetOptions : {
								// filter_external: '#filterUrl',
								filter_serversideFiltering : true,
								filter_columnFilters : false,
								filter_hideFilters : false,
								// include child row content while filtering, if true
								// filter_childRows  : false,
								// class name applied to filter row and each input
								filter_cssFilter : 'filtered',
								pager_removeRows : false
							}
						}).tablesorterPager(pagerOptions);

						// convert select boxes to better ones
						var chosenOptions = {
							width : "100%",
							disable_search_threshold : 5,
							placeholder_text_multiple : "Bitte auswählen",
							no_results_text : "Keinen Eintrag gefunden"
						};

						var updateBrowserHistory = function() {

							// avoid problem with missing functionality in IE9
							if (window.history.pushState) {
								window.history
										.pushState(
												null,
												null,
												location.pathname
														+ "?instance=${instance.name}&urlfilter="
														+ $("#urlTable").data().urlfilter
														+ "&filter="
														+ $("#urlTable").data().statusfilter);
							}
						};

						var setFilterValues = function() {
							var filter = [];
							var options = $("#filterStatus option:selected");
							$.each(options, function(index, option) {
								filter.push(option.getAttribute("value"));
							});
							var filterParam = filter.join(",");
							var value = $("#filterUrl").val();

							$("#urlTable").data().statusfilter = filterParam;
							$("#urlTable").data().urlfilter = value;

							var filterString = filterParam + value;
							// workaround to trigger search filter correctly if resetting a filter
							if (filterString === "")
								filterString = " ";

							// use a combination of both filters to initiate a new search
							// since we prepare the final url with request parameters ourselves
							// it doesn't matter which value we trigger the search with
							$('#urlTable').trigger('search',
									[ [ filterString ] ]);
							updateBrowserHistory();
						};

						$("#filterStatus").chosen(chosenOptions).change(
								function() {
									setFilterValues();
								});

						// filter by Url
						$("#filterUrl").on("keyup", function() {
							setFilterValues();
						});

						setFilterValues();

						$("#urlContent").css("visibility", "visible");
						$("#loading").hide();

					});
</script>

</head>
<body>

	<c:set var="activeTab" value="6" scope="request" />
	<c:import url="includes/body.jsp"></c:import>

</body>
</html>
