package test.strangeforest.currencywatch.integration.mvc;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.test.context.*;
import org.springframework.test.context.testng.*;
import org.springframework.test.context.web.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.*;
import org.springframework.web.context.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.*;

import static java.text.MessageFormat.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration({"file:src/main/webapp/WEB-INF/currency-watch.xml", "file:src/main/webapp/WEB-INF/SpringMVC-servlet.xml"})
public class CurrencyChartIT extends AbstractTestNGSpringContextTests {

	@Autowired private WebApplicationContext wac;

	private MockMvc mockMvc;

	@BeforeClass
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	public void showCurrencyChart() throws Exception {
		mockMvc.perform(get("/currency-chart").accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk())
			.andExpect(view().name("currency-chart"));
	}

	@Test
	public void zoomCurrencyChart() throws Exception {
		String dateRange = format("{0,date,ddMMyyyy}-{1,date,ddMMyyyy}", TestData.DATE1, TestData.DATE5);
		mockMvc.perform(get("/currency-chart").param("command", "zoomChart").param("dateRange", dateRange).accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("currency-chart"));
	}
}
