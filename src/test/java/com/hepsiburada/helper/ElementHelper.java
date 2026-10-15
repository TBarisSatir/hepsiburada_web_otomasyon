package com.hepsiburada.helper;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.openqa.selenium.By;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hepsiburada.model.ElementInfo;

/**
 * resources/element-infos altındaki tüm JSON dosyalarını okur ve locator'ları key ile sunar.
 * Key'ler tüm dosyalarda benzersiz olmalı (bu yüzden sayfa adıyla başlarlar).
 */
public final class ElementHelper {

    private static final String ELEMENT_INFOS_DIR = "element-infos";

    private static final Map<String, ElementInfo> ELEMENTS = loadAll();

    private ElementHelper() {
    }

    /** Key'e karşılık gelen locator'ı döndürür. */
    public static By getBy(String key) {
        ElementInfo info = getElementInfo(key);
        return toBy(info, info.getValue());
    }

    /**
     * Değerinde %s yer tutucusu olan locator'ı verilen argümanlarla doldurup döndürür.
     * Örn. "i%s" + 4 -> By.id("i4")
     */
    public static By getBy(String key, Object... args) {
        ElementInfo info = getElementInfo(key);
        return toBy(info, String.format(info.getValue(), args));
    }

    public static ElementInfo getElementInfo(String key) {
        ElementInfo info = ELEMENTS.get(key);
        if (info == null) {
            throw new IllegalArgumentException(
                    "'" + key + "' key'i " + ELEMENT_INFOS_DIR + " altındaki JSON dosyalarında bulunamadı."
            );
        }
        return info;
    }

    private static By toBy(ElementInfo info, String value) {
        String type = info.getType() == null ? "" : info.getType().trim();

        switch (type) {
            case "id":
                return By.id(value);
            case "css":
                return By.cssSelector(value);
            case "xpath":
                return By.xpath(value);
            case "name":
                return By.name(value);
            case "className":
                return By.className(value);
            case "tagName":
                return By.tagName(value);
            case "linkText":
                return By.linkText(value);
            case "partialLinkText":
                return By.partialLinkText(value);
            default:
                throw new IllegalArgumentException(
                        "Desteklenmeyen locator tipi '" + type + "': " + info
                );
        }
    }

    // ------------------------------------------------------------------
    // JSON yükleme
    // ------------------------------------------------------------------

    private static Map<String, ElementInfo> loadAll() {
        URL url = ElementHelper.class.getClassLoader().getResource(ELEMENT_INFOS_DIR);
        if (url == null) {
            throw new IllegalStateException(
                    "Classpath'te '" + ELEMENT_INFOS_DIR + "' klasörü bulunamadı."
            );
        }

        try {
            URI uri = url.toURI();
            if ("jar".equals(uri.getScheme())) {
                try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    return loadFrom(fs.getPath(ELEMENT_INFOS_DIR));
                }
            }
            return loadFrom(Path.of(uri));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Element bilgileri okunamadı: " + url, e);
        }
    }

    private static Map<String, ElementInfo> loadFrom(Path dir) throws IOException {
        Map<String, ElementInfo> elements = new HashMap<>();
        Gson gson = new Gson();

        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".json"))::iterator) {
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    List<ElementInfo> infos = gson.fromJson(
                            reader, new TypeToken<List<ElementInfo>>() { }.getType()
                    );
                    if (infos == null) {
                        continue;
                    }
                    for (ElementInfo info : infos) {
                        ElementInfo previous = elements.put(info.getKey(), info);
                        if (previous != null) {
                            throw new IllegalStateException(
                                    "Aynı key birden fazla kez tanımlı: '" + info.getKey()
                                            + "' (" + file.getFileName() + ")"
                            );
                        }
                    }
                }
            }
        }
        return elements;
    }
}
