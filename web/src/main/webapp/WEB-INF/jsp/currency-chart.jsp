<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="f" uri="http://www.springframework.org/tags/form" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<link rel="shortcut icon" type="image/x-icon" href="../resources/images/favicon.ico"/>
	<title>${chart.title}</title>
	<script src="../resources/js/jquery.min.js"></script>
	<script src="../resources/js/jquery.Jcrop.min.js"></script>
	<script type="text/javascript">
		var IE = {
			version: function() {
				return navigator.appVersion.indexOf("MSIE") != -1 ? parseFloat(navigator.appVersion.split("MSIE")[1]) : null;
			}
		};
		function setMovAvgPeriodEnabled() {
			var showMovAvg = document.getElementById("showMovAvg");
			var showBollBands = document.getElementById("showBollBands");
			var movAvgPeriod = document.getElementById("movAvgPeriod");
			movAvgPeriod.disabled = !(showMovAvg.checked || showBollBands.checked);
		}
		function showChart(command) {
			var movAvgPeriod = document.getElementById("movAvgPeriod");
			var zoomChart = document.getElementById("zoomChart");
			var chartTable = document.getElementById("chartTable");
			var loadingTable = document.getElementById("loadingTable");
			movAvgPeriod.disabled = false;
			zoomChart.style.display = "none";
			if (chartTable)
				chartTable.style.display = "none";
			loadingTable.style.display = "inline";
			if (IE.version() != null) // Workaround for IE issue with animated GIFs
				setTimeout('document.getElementById("loadingImage").src = "../resources/images/loading.gif"', 100);
			document.getElementById("command").value = command;
			document.getElementById("chartForm").submit();
			return true;
		}
		function doJCrop() {
			$(function () {
				$('#chartImage').Jcrop({
					onSelect:showZoomButton,
					onChange:moveZoomButton,
					onRelease:hideZoomButton
				});
			});
		}
		function showZoomButton(c) {
			var zoomChart = document.getElementById("zoomChart");
			zoomChart.style.display = "block";
			moveZoomButton(c);
		}
		function moveZoomButton(c) {
			var chartTable = document.getElementById("chartTable");
			var zoomChart = document.getElementById("zoomChart");
			zoomChart.style.left = findLeft(chartTable) + c.x + (c.w - zoomChart.offsetWidth)/2;
			zoomChart.style.top = findTop(chartTable) + c.y + (c.h - zoomChart.offsetHeight)/2;
			document.getElementById("zoomx1").value = c.x;
			document.getElementById("zoomx2").value = c.x2;
		}
		function hideZoomButton() {
			document.getElementById("zoomChart").style.display = "none";
		}
		function findLeft(obj) {
			var left = 0;
			if (obj.offsetParent) {
				while(1) {
					left += obj.offsetLeft;
					if (!obj.offsetParent)
						break;
					obj = obj.offsetParent;
				}
			}
			else if (obj.x)
				left += obj.x;
			return left;
		}
		function findTop(obj) {
			var top = 0;
			if (obj.offsetParent) {
				while(1) {
					top += obj.offsetTop;
					if (!obj.offsetParent)
						break;
					obj = obj.offsetParent;
				}
			}
			else if (obj.y)
				top += obj.y;
			return top;
		}

		if (IE.version() != 9)
			doJCrop();
		else
			setTimeout("doJCrop()", 100); // Workaround for IE9 issue with JCrop

		window.onbeforeunload = function() {
			window.name = "reloader";
		}
	</script>
	<link rel="stylesheet" type="text/css" href="../resources/css/currency-watch.css"/>
	<link rel="stylesheet" type="text/css" href="../resources/css/jquery.Jcrop.min.css"/>
</head>
<body onload="setMovAvgPeriodEnabled()">
	<f:form id="chartForm" method="GET" action="currency-chart" commandName="chart">
		<input type="hidden" id="command" name="command"/>
		<table style="text-align: center"><tr>
			<td><h1 style="width: ${chart.chartWidth}">Currency Watch</h1></td>
		</tr><tr>
			<td align="center"><table cellspacing="0" cellpadding="0"><tr>
				<td class="toolbar">
					<f:label path="currency">Currency:</f:label>
					<f:select id="currency" path="currency" title="Currency" items="${chart.currencies}"/>
				</td>
				<td class="toolbar">
					<f:label path="period">Period:</f:label>
					<f:select id="period" path="period" title="Chart period" items="${chart.periods}"/>
				</td>
				<td class="toolbar">
					<f:label path="quality">Quality:</f:label>
					<f:select id="quality" path="quality" title="Series quality" items="${chart.qualities}"/>
				</td>
				<td class="toolbar">
					<f:checkbox id="showBidAsk" path="showBidAsk" title="Show Bid and Ask"/>
					<f:label path="showBidAsk">Bid/Ask</f:label>
				</td>
				<td class="toolbar">
					<f:checkbox id="showMovAvg" path="showMovAvg" title="Show Moving Average" onclick="setMovAvgPeriodEnabled()"/>
					<f:label path="showMovAvg">Mov. Avg.</f:label>
				</td>
				<td class="toolbar">
					<f:checkbox id="showBollBands" path="showBollBands" title="Show Bollinger Bands" onclick="setMovAvgPeriodEnabled()"/>
					<f:label path="showBollBands">Boll. Bands</f:label>
				</td>
				<td class="toolbar">
					<f:select id="movAvgPeriod" path="movAvgPeriod" title="Moving Average period" items="${chart.movAvgPeriods}"/>
				</td>
				<td class="toolbar">
					<input type="button" value="Show Chart" onclick="showChart('showChart')" title="Show Chart"/>
				</td>
			</tr></table></td>
		</tr><tr>
			<td>
				<f:errors style="color: red;"/>
				<input id="zoomChart" type="submit" value="Zoom Chart" onclick="showChart('zoomChart')" title="Zoom Chart" style="position: absolute; display: none; z-index: 2000;"/>
				<f:hidden id="dateRange" path="dateRange"/>
				<f:hidden id="zoomx1" path="zoomx1"/>
				<f:hidden id="zoomx2" path="zoomx2"/>
			</td>
		</tr><tr>
			<td>
				<table id="chartTable" cellspacing="0" cellpadding="0" style="text-align: center">
					<tr>
						<td>
							<img id="chartImage" src="../chart?filename=${chart.chartFileName}" width="${chart.chartWidth}" height="${chart.chartHeight}" alt="Currency Chart for ${chart.currencyFullName}"/>
						</td>
					</tr>
					<tr>
						<td>
							${chart.currencyFullName}, period: ${!chart.zoomed ? chart.period.label() : ''}
							[<fmt:formatDate value="${chart.dateFrom}" pattern="dd-MM-yyyy" timeZone="CET"/>, <fmt:formatDate value="${chart.dateTo}" pattern="dd-MM-yyyy" timeZone="CET"/>]
						</td>
					</tr>
					<tr>
						<td>
							<fmt:formatDate value="${chart.currentRate.date}" pattern="dd-MM-yyyy" timeZone="CET"/> -
							Bid: <fmt:formatNumber value="${chart.currentRate.rate.bid}" maxFractionDigits="2"/>,
							Middle: <span style="color: ${chart.currentRate.color};font-weight: bold;"><fmt:formatNumber value="${chart.currentRate.rate.middle}" maxFractionDigits="2"/></span>,
							Ask: <fmt:formatNumber value="${chart.currentRate.rate.ask}" maxFractionDigits="2"/>
						</td>
					</tr>
				</table>
				<table id="loadingTable" cellspacing="0" cellpadding="0" style="text-align: center; display: none;">
					<tr><td>&#160;</td></tr>
					<tr><td><img id="loadingImage" src="../resources/images/loading.gif" width="66" height="66" alt="Loading..."/></td></tr>
					<tr><td>&#160;</td></tr>
					<tr><td class="loading">Loading...</td></tr>
				</table>
			</td>
		</tr><tr>
			<td>&#160;</td>
		</tr><tr>
			<td class="small">
				<a href="https://github.com/mcekovic/currency-watch/downloads">Download Currency Watch desktop application</a>,&#160;
				<a href="https://github.com/mcekovic/currency-watch">Currency Watch on GitHub</a>,&#160;
				<a href="../api/application.wadl">Currency Watch REST API</a>
			</td>
		</tr></table>
	</f:form>
</body>
</html>