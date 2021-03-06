package test.strangeforest.currencywatch.integration.jdbc;

import java.io.*;

import org.strangeforest.currencywatch.jdbc.*;
import org.strangeforest.db.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.integration.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static test.strangeforest.currencywatch.integration.jdbc.H2Data.*;

public class SchemaManagerIT {

	private ConnectionPoolDataSource dataSource;
	private SchemaManager schemaManager;

	@BeforeClass
	public void setUp() throws IOException {
		ITUtil.ensureDirectory(H2_DATA_DIR);
		ITUtil.deleteFiles(H2_DATA_DIR, H2_DATA_FILE_NAME + "-sm\\..+\\.db");
		dataSource = new ConnectionPoolDataSource(DRIVER_CLASS, ADMIN_JDBC_URL + "-sm", ADMIN_USERNAME, ADMIN_PASSWORD);
		dataSource.init();
		schemaManager = new SchemaManager(dataSource, DIALECT);
		schemaManager.setUsername(APP_USERNAME);
		schemaManager.setPassword(APP_PASSWORD);
	}


	@AfterClass
	private void cleanUp() {
		dataSource.close();
	}

	@Test
	public void schemaIsCreated() {
		SchemaManager schemaManagerSpy = spy(schemaManager);

		schemaManagerSpy.ensureSchema();

		assertThat(schemaManager.schemaExists()).isTrue();
		assertThat(schemaManager.getSchemaVersion()).isEqualTo(SchemaManager.VERSION);
		verify(schemaManagerSpy).ensureSchema();
		verify(schemaManagerSpy).schemaExists();
		verify(schemaManagerSpy).createSchema();
		verifyNoMoreInteractions(schemaManagerSpy);
	}

	@Test(dependsOnMethods = "schemaIsCreated")
	public void schemaIsUpToDate() {
		SchemaManager schemaManagerSpy = spy(schemaManager);

		schemaManagerSpy.ensureSchema();

		assertThat(schemaManager.getSchemaVersion()).isEqualTo(SchemaManager.VERSION);
		verify(schemaManagerSpy).ensureSchema();
		verify(schemaManagerSpy).schemaExists();
		verify(schemaManagerSpy).getSchemaVersion();
		verifyNoMoreInteractions(schemaManagerSpy);
	}

	@Test(dependsOnMethods = "schemaIsUpToDate")
	public void schemaIsUpgraded() {
		SchemaManager newSchemaManager = new SchemaManager(dataSource, DIALECT, SchemaManager.VERSION + 1);
		newSchemaManager.setUsername(APP_USERNAME);
		newSchemaManager.setPassword(APP_PASSWORD);
		SchemaManager schemaManagerSpy = spy(newSchemaManager);

		schemaManagerSpy.ensureSchema();

		assertThat(newSchemaManager.getSchemaVersion()).isEqualTo(SchemaManager.VERSION + 1);
		verify(schemaManagerSpy).ensureSchema();
		verify(schemaManagerSpy).schemaExists();
		verify(schemaManagerSpy).getSchemaVersion();
		verify(schemaManagerSpy).upgradeSchema(SchemaManager.VERSION, SchemaManager.VERSION + 1);
		verifyNoMoreInteractions(schemaManagerSpy);
	}
}
