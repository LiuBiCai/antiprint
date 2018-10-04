package io.github.mike10004.antiprint.e2etests;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.UserAgentFamily;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Tests {

    private static final Supplier<Properties> supplier = Suppliers.memoize(() -> {
        Properties p = new Properties();
        try (Reader reader = new InputStreamReader(Tests.class.getResourceAsStream("/tests.properties"), UTF_8)) {
            p.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return p;
    });

    public static Properties getProperties() {
        return supplier.get();
    }

    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    private static boolean isUnfiltered(String key, String value) {
        return ("${" + key + "}").equals(value);
    }

    public static File getParentBaseDir() {
        String val = getProperty("project.parent.basedir");
        if (isUnfiltered("project.parent.basedir", val)) {
            val = getProperty("project.basedir");
            checkState(!isUnfiltered("project.basedir", val), "property unfiltered: %s", val);
            File basedir = new File(val);
            return basedir.getParentFile();
        }
        return new File(val);
    }

    public static BrowserFingerprintTestCase getNavigatorTestCase(UserAgentFamily userAgentFamily, OperatingSystemFamily operatingSystemFamily) {
        return getNavigatorTestCases(testCase -> {
            return userAgentFamily == testCase.input.userAgentFamily && operatingSystemFamily == testCase.input.os;
        }).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no test cases for given requirements"));
    }

    public static ImmutableList<BrowserFingerprintTestCase> getNavigatorTestCases(Predicate<? super BrowserFingerprintTestCase> filter) {
        try (Reader reader = Resources.asCharSource(Tests.class.getResource("/navigator-test-cases.json"), UTF_8).openStream()) {
            BrowserFingerprintTestCase testCases[] = new Gson().fromJson(reader, BrowserFingerprintTestCase[].class);
            checkState(testCases != null);
            return ImmutableList.copyOf(testCases);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getBuildDir() {
        return new File(getProperty("project.build.directory"));
    }

    public static void setUpGeckodriver() {
        FirefoxDriverManager.getInstance().setup();
    }

    public static void setUpChromedriver() {
        ChromeDriverManager.getInstance().setup();
    }

    public static boolean filesEqual(Unzippage a, Unzippage b) throws IOException {
        if (!ImmutableSet.copyOf(a.fileEntries()).equals(ImmutableSet.copyOf(b.fileEntries()))) {
            return false;
        }
        for (String entry : a.fileEntries()) {
            if (!a.getFileBytes(entry).contentEquals(b.getFileBytes(entry))) {
                return false;
            }
        }
        return true;
    }

    public static Unzippage pseudoUnzippage(Path parent) throws IOException {
        Collection<File> files = FileUtils.listFiles(parent.toFile(), null, true);
        Function<File, String> entryNameMapper = file -> FilenameUtils.normalizeNoEndSeparator(parent.relativize(file.toPath()).toString(), true) + (file.isDirectory() ? "/" : "");
        Set<String> directoryEntries = files.stream().map(File::getParentFile)
                .map(entryNameMapper)
                .collect(Collectors.toSet());
        Map<String, ByteSource> fileEntries = files.stream().collect(Collectors.toMap(entryNameMapper, Files::asByteSource));
        return new Unzippage() {
            @Override
            public Iterable<String> fileEntries() {
                return fileEntries.keySet();
            }

            @Override
            public Iterable<String> directoryEntries() {
                return directoryEntries;
            }

            @Override
            public ByteSource getFileBytes(String fileEntry) {
                return checkNotNull(fileEntries.get(fileEntry));
            }
        };
    }

    @Nullable
    public static Object deserialize(JsonElement serialized) {
        if (serialized.isJsonNull()) {
            return null;
        }
        JsonPrimitive primitive = serialized.getAsJsonPrimitive();
        if (primitive.isString()) {
            return primitive.getAsString();
        }
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        checkState(primitive.isNumber());
        Number number = primitive.getAsNumber();
        if (number.longValue() == number.doubleValue()) {
            return number.longValue();
        } else {
            return number.doubleValue();
        }
    }
}
