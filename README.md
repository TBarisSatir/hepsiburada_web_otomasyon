# Hepsiburada web sitesi üzerinde test otomasyon senaryosu örneği.
Fonksiyonlar : 
-Kullanıcı girişi
-Ürün arama
-Sepete ekleme

### Test Hesabının giriş bilgileri
'credentials.properties' dosyasında 'HB_USERNAME' değikeni test sırasında kullanılacak hesabın mail adresi ve 'HB_PASSWORD' alanı ise şifresi olacak şekilde değer verilmelidir. 'credentials.properties' dosyası kullanılacak ise "enter username from config" ve "enter password from config" stepleri kullanılmalıdır.
"enter username <username>" ve "enter password <password>" stepleri ile spec dosyası içerisinde email adresi ve şifre tanımları yapılması da mümkündür. 

### Dosya yapısı hakkında
'driver' klasörü içerisinde WebDriver yönetimi ve tarayıcı ayarları bulunur. 'pages' klasörü web sitesi üzerinde yapılabilecek eylemlerin fonksiyonlarını içerir. Her sayfa için farklı Java sınıfı bulunur. 'step' klasöründe daha önce tanımlanmış fonksiyonlar kullanılarak steplerin oluşturulduğu 'StepImplementation.java' dosyası bulunur. 

