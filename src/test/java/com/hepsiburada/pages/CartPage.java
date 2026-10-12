package com.hepsiburada.pages;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

public class CartPage extends BasePage {

    private static final String CART_URL_PART = "sepetim";

    /** Sepet sayfası farklı bir sekmede açıldıysa o sekmeye geçer. */
    public void switchToCartWindow() {
        if (driver().getCurrentUrl().contains(CART_URL_PART)) {
            return;
        }
        for (String handle : driver().getWindowHandles()) {
            driver().switchTo().window(handle);
            if (driver().getCurrentUrl().contains(CART_URL_PART)) {
                return;
            }
        }
    }

    /**
     * Seçilen ürünün adı (arama sonucundaki ad veya detay sayfası başlığı) sepette görünene kadar bekler.
     * Sepet adı kısaltıyorsa adın ilk 6 kelimesi de kabul edilir.
     */
    public void verifyProductNameVisible(String productName, String detailTitle) {

        requireProduct(productName);

        try {
            waitFor(20).until(d -> {
                try {
                    switchToCartWindow();
                    return d.getCurrentUrl().contains(CART_URL_PART)
                            && containsProductName(getPageText(), productName, detailTitle);
                } catch (WebDriverException e) {
                    return false;
                }
            });
        } catch (TimeoutException e) {
            throw new AssertionError(
                    "Sepette ürün adı bulunamadı. Beklenen: '" + productName + "'"
                            + (detailTitle != null ? " (detay başlığı: '" + detailTitle + "')" : "")
                            + " | URL: " + driver().getCurrentUrl()
            );
        }

        System.out.println("Sepet ekranında eklenen ürün görünür");
        System.out.println("  Ürün: " + productName);
    }

    /** Marka, adın ilk kısmı ve (ürün adında geçiyorsa) arama kelimesi sepette görünmeli. */
    public void verifyProductBasicDetails(String productName, String searchedKeyword) {

        requireProduct(productName);
        switchToCartWindow();

        String cartText = getPageText();
        String name = normalize(productName);
        String brand = name.split(" ")[0];
        String namePart = firstWords(name, 3);
        String keyword = normalize(searchedKeyword);

        if (!cartText.contains(brand)) {
            throw new AssertionError(
                    "Sepette marka bulunamadı. Beklenen marka: '" + brand
                            + "' (ürün: '" + productName + "')"
            );
        }

        if (!cartText.contains(namePart)) {
            throw new AssertionError(
                    "Sepette ürün adının ilk kısmı bulunamadı. Beklenen: '" + namePart + "'"
            );
        }

        // Arama kelimesi ürün adında geçiyorsa (örn. "bilgisayar"), sepette de geçmeli
        boolean keywordChecked = !keyword.isEmpty() && name.contains(keyword);
        if (keywordChecked && !cartText.contains(keyword)) {
            throw new AssertionError(
                    "Sepette arama kelimesi bulunamadı: '" + keyword + "'"
            );
        }

        System.out.println("Ürün temel detayları sepetle eşleşir");
        System.out.println("  Marka: " + brand);
        System.out.println("  Ad (ilk kısım): " + namePart);
        if (keywordChecked) {
            System.out.println("  Arama kelimesi: " + keyword);
        }
    }

    private boolean containsProductName(String cartText, String productName, String detailTitle) {
        for (String name : new String[]{productName, detailTitle}) {
            String normalized = normalize(name);
            if (normalized.isEmpty()) {
                continue;
            }
            if (cartText.contains(normalized) || cartText.contains(firstWords(normalized, 6))) {
                return true;
            }
        }
        return false;
    }

    private void requireProduct(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new AssertionError(
                    "Seçilen ürünün adı alınamadı. 'open second row first product' adımı "
                            + "bu adımdan önce çalışmalı."
            );
        }
    }
}