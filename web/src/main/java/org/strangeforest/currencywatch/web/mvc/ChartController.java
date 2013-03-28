package org.strangeforest.currencywatch.web.mvc;

import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.*;
import org.strangeforest.currencywatch.core.*;

import java.io.*;

@Controller
public class ChartController {

	@Autowired @Qualifier("currencyRateProvider")
	private CurrencyRateProvider currencyProvider;

	@RequestMapping("/currency-chart")
	public ModelAndView chart(@ModelAttribute("chart") ChartModel chart, @RequestParam(required = false) String command, HttpSession session) throws IOException {
		chart.setCurrencyProvider(currencyProvider);
		chart.setSession(session);
		if ("zoomChart".equals(command))
			chart.zoomChart();
		else
			chart.showChart();
		return new ModelAndView("currency-chart", "chart", chart);
	}
}
