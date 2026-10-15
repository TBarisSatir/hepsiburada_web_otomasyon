# Hepsiburada web sitesi üzerinde test otomasyon senaryosu örneği.
Fonksiyonlar : 
-Kullanıcı girişi
-Ürün arama
-Sepete ekleme

### Test Hesabının giriş bilgileri
'credentials.properties' dosyasında 'HB_USERNAME' değikeni test sırasında kullanılacak hesabın mail adresi ve 'HB_PASSWORD' alanı ise şifresi olacak şekilde değer verilmelidir. 'credentials.properties' dosyası kullanılacak ise "enter username from config" ve "enter password from config" stepleri kullanılmalıdır.<br>
"enter username `<username>`" ve "enter password `<password>`" stepleri ile spec dosyası içerisinde email adresi ve şifre tanımları yapılması da mümkündür. 

### Dosya yapısı hakkında
'driver' klasörü içerisinde WebDriver yönetimi ve tarayıcı ayarları bulunur. 'pages' klasörü web sitesi üzerinde yapılabilecek eylemlerin fonksiyonlarını içerir. Her sayfa için farklı Java sınıfı bulunur. 'step' klasöründe daha önce tanımlanmış fonksiyonlar kullanılarak steplerin oluşturulduğu 'StepImplementation.java' dosyası bulunur. 


### Element locator'ları
Sayfa sınıflarında kullanılan tüm element locator'ları kodun dışında, `src/test/resources/element-infos/` altındaki JSON dosyalarında tutulur (her sayfa için bir dosya: `home.json`, `login.json`, `search.json`, `product_detail.json`). Her kayıt `key`, `value` ve `type` (`id`, `css`, `xpath`, `name`, `className`, `tagName`, `linkText`, `partialLinkText`) alanlarından oluşur. 'helper' klasöründeki `ElementHelper` bu dosyaları okur ve sayfa sınıfları locator'lara `ElementHelper.getBy("key")` ile erişir; 'model' klasöründeki `ElementInfo` tek bir JSON kaydını temsil eder. Key'ler tüm dosyalarda benzersiz olmalıdır.
