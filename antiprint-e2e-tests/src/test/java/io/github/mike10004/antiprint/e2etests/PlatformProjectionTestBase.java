package io.github.mike10004.antiprint.e2etests;

import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.common.net.MediaType;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class PlatformProjectionTestBase extends BrowserUsingTestBase {

    @Rule
    public Timeout timeout = Timeout.seconds(TIMEOUT_SECONDS);

    protected WebDriver createDriver(String userAgent) throws IOException {
        return createDriver(userAgent, xvfb.getController().newEnvironment());
    }

    protected abstract WebDriver createDriver(String userAgent, Map<String, String> environment) throws IOException;

    protected void testNavigatorProperties(UserAgentFamily requiredUserAgentFamily, OperatingSystemFamily requiredOsFamily) throws IOException, URISyntaxException, InterruptedException {
        Map<String, Object> navigator = Tests.getNavigatorTestCasesByUserAgent(userAgent -> {
            return userAgent.getFamily() == requiredUserAgentFamily
                    && userAgent.getOperatingSystem().getFamily() == requiredOsFamily;
        }).stream().findFirst().orElseThrow(() -> new IllegalArgumentException(requiredUserAgentFamily + "/" + requiredOsFamily));
        System.out.println(navigator);
        String userAgent = navigator.get("userAgent").toString();
        byte[] html = Resources.toByteArray(getClass().getResource("/print-navigator.html"));
        NanoServer server = NanoServer.builder()
                .get(request -> {
                    return NanoResponse.status(200).content(MediaType.HTML_UTF_8, html).build();
                }).build();
        WebDriver driver = createDriver(userAgent);
        try {
            // the extension is only active if the page URL is http[s]
            try (NanoControl control = server.startServer()) {
                driver.get(control.buildUri().build().toString());
                /*
                 * Content is written with document.write, so we don't have to
                 * use a WebDriverWait to poll the page
                 */
                String json = driver.findElements(By.id("content")).stream()
                        .map(WebElement::getText)
                        .filter(text -> !text.trim().isEmpty())
                        .findFirst().orElse(null);
                assertNotNull("div#content contents", json);
                Map<String, Object> actual = Tests.navigatorObjectLoader().apply(CharSource.wrap(json));
                maybePauseUntilKilled();
                navigator.forEach((k, v) -> {
                    System.out.format("%s = %s%n", k, actual.get(k));
                    if (isImportant(k)) {
                        assertEquals("property: " + k, navigator.get(k), actual.get(k));
                    }
                });
            }
        } finally {
            driver.quit();
        }
    }

    private static boolean isImportant(String navigatorProperty) {
        return "platform".equals(navigatorProperty);
    }

}