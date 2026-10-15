package com.hepsiburada.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.helper.ElementHelper;

public class ProductDetailPage extends BasePage {

    // Locator'lar: resources/element-infos/product_detail.json
    private final By addToCartButton = ElementHelper.getBy("product_detail_add_to_cart_button");
    private final By goToCartButton = ElementHelper.getBy("product_detail_go_to_cart_button");
    private final By popupCloseButton = ElementHelper.getBy("product_detail_popup_close_button");
    private final By heading = ElementHelper.getBy("product_detail_heading");

    /** Ürün detay sayfasındaki "Sepete Ekle" butonu gelene kadar bekler. */
    public void waitUntilLoaded() {
        waitFor(15).until(ExpectedConditions.visibilityOfElementLocated(addToCartButton));
    }

    /** Detay sayfasındaki ürün başlığını döndürür (bulunamazsa null). */
    public String getTitle() {
        for (WebElement h : driver().findElements(heading)) {
            try {
                if (h.isDisplayed() && !h.getText().trim().isEmpty()) {
                    return h.getText().trim();
                }
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return null;
    }

    /** Ürünü sepete ekler, onay popup'ını doğrular ve popup'ı kapatır. */
    public void addToCart() {

        WebDriverWait wait = waitFor(15);

        WebElement button = wait.until(
                ExpectedConditions.visibilityOfElementLocated(addToCartButton)
        );

        scrollIntoViewCenter(button);
        clickWithJsFallback(button);

        WebElement goToCart = wait.until(
                ExpectedConditions.visibilityOfElementLocated(goToCartButton)
        );

        if (!goToCart.isDisplayed()) {
            throw new AssertionError("Sepete ekleme popup'ı görüntülenmedi.");
        }

        System.out.println("Ürün sepete eklenir");

        wait.until(ExpectedConditions.elementToBeClickable(popupCloseButton)).click();

        wait.until(ExpectedConditions.invisibilityOf(goToCart));
    }
}