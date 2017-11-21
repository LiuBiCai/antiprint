package io.github.mike10004.antiprint.e2etests;

import com.google.common.io.ByteStreams;
import io.github.mike10004.crxtool.CrxParser;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class FirefoxDriverProvider implements WebDriverProvider<FirefoxDriver> {

    private final String userAgent;

    public FirefoxDriverProvider() {
        this(null);
    }

    public FirefoxDriverProvider(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public FirefoxDriver provide(Map<String, String> environment) throws IOException {
        FirefoxProfile profile = new FirefoxProfile();
        File extensionZipFile = prepareZipFile();
        profile.addExtension(extensionZipFile);
        FirefoxOptions options = new FirefoxOptions();
        if (userAgent != null) {
            options.addPreference("general.useragent.override", userAgent);
        }
        options.setProfile(profile);

        GeckoDriverService service = new GeckoDriverService.Builder()
                .withEnvironment(environment)
                .build();
        FirefoxDriver driver = new FirefoxDriver(service, options);
        return driver;
    }

    protected File prepareZipFile() throws IOException {
        File zipFile = File.createTempFile("antiprint-extension-firefox", ".zip");
        File crxFile = CrxProvider.ofDependency().provide();
        try (InputStream crxStream = new FileInputStream(crxFile)) {
            CrxParser.getDefault().parseMetadata(crxStream);
            try (OutputStream out = new FileOutputStream(zipFile)) {
                ByteStreams.copy(crxStream, out);
            }
        }
        return zipFile;
    }

}
