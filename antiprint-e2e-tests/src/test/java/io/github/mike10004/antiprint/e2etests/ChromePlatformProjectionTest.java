package io.github.mike10004.antiprint.e2etests;

import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

public class ChromePlatformProjectionTest extends PlatformProjectionTestBase {

    @BeforeClass
    public static void setUpClass() {
        System.out.format("wdm.chromeDriverVersion=%s%n", System.getProperty("wdm.chromeDriverVersion"));
        Tests.setUpChromedriver();
    }

    @Test
    public void windows() throws Exception {
        testNavigatorProperties(UserAgentFamily.CHROME, OperatingSystemFamily.WINDOWS, new DefaultEvaluator());
    }

    @Test
    public void osx() throws Exception {
        testNavigatorProperties(UserAgentFamily.CHROME, OperatingSystemFamily.OS_X, new DefaultEvaluator());
    }
    @Test
    public void linux() throws Exception {
        testNavigatorProperties(UserAgentFamily.CHROME, OperatingSystemFamily.LINUX, new DefaultEvaluator());
    }

    @Override
    protected WebDriverProvider<? extends WebDriver> getWebDriverProvider(String userAgent) {
        return new ChromeDriverProvider(userAgent);
    }
}
