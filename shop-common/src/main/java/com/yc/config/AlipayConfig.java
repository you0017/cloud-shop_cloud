package com.yc.config;

import java.io.FileWriter;
import java.io.IOException;

public class AlipayConfig {

	public static String app_id = "9021000136670922";

    public static String merchant_private_key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCPivL21VKMZwf69j4bofnEmlEVfRSGsEix8NBgB2ojv8gTfIWdNHwOSqEjZph3WfPXUqUdOWybq27lVQrjCz1jg2lu+ySxv+Sv+g0B+8ThGd+EPh+uqfb5pD9mD8Sqv6AqjV5haUKn2nGkhTQVgtkRyG5R6Sii0EdL4eLyNx6ImbBoaUSHDTVc9oX5f0IuHzX4FLTlZpLbCM9rDGtm2wBSBqkN2SEyeExOYeS9LNw2pAGp+5HduQPVA7+FOPLi1CASDZYQXB1edcdXcwvVSdH84OfL8bTC8L+fbB+lA5iHbux2CEvyFYVjMmHwJ0qoRWlPF9f8F/i5tzE2dcnIBpzZAgMBAAECggEAN5NWqpbBVTb28gWw2kYGTEZrKmS5M8TBAJP6OZPlVl4Eevb1TH5roaTnaqtoUJ11OWL4Jq57DHJ9hzE7+9f4NrVEdwGpnWBsjG3F54SxH25DChJ7dca8pr8fBMcKIAn9WzxM1G5Krm53HXmtRr7YiHgProsVfqQhVpHoqlsI6V3zEvhRNX1TyhgEZvABs6H0DyIt/WtCdw7TypwmcCCvAOxR5U+enGN/sfZTskpGEVoy2+hSOOGYCxWX8UzQ4zK6u3LpNmLk1fVCqxKFy8t8PyCz28JEquL101jdwsFz/oAhvv9T9TlXU1vmI9wv2EPIRXqFgP9ckbeGTpvVDxs2YQKBgQDNUajAZ5Cw7Zc1kq73jP3Zrymwn6aOmGi3wifphteSmMKY7HUuOVqawt73d3SfQ45Qw/i6L1UkkOQH87uy9Zp1yC7d+qedn3WRRIPJKw+lB9KK2mcJyBc6b/0ItlqZ32j5912fQjKaztyvWYlh9HYhXXfsSk4pgmqriB7NV3aebQKBgQCy+ZSgeag1064GIu4mEf13eVfYoZacvkORLZrTjae1I1liDrCv9iDmqMT+VjnHMZgKJTJ2KxfFZBHN0NdIXGkf9zQqtCSJLk1tN6q/M4GgP7nOhYbevs2l+L6FDFR2Aq6soG8ehspk8+f+gDxpRdzgLNbqluwoMNIfEuf9UWzEnQKBgE9CLaWQlS9nmE6LAFX5rGjjEHHpP2+ecBY3shaZAhDKPZyw8w9T/qDthwr6px9wAbk13YdqDDEgaZaURSnxW54KF+WDBD95tfXNAB6hK9nmKiWF5+7DugvJ/WDmnBT4RFryXcbD1CskndZm9vzEVPHOViP2DzRA7xKmalWUZOsFAoGAe25hMGN2vNimNhk1ARB7LECFcyPOCX+2fZNkKe6R8PzWEihrdo9alXljkrzI0DXF04lfydtiY4HmPXmAX6lL3v3P/nuKjwld4Kr5mS/egDQy4tl38HrKvWJVktC0F0c2TFvBBt6TCxijsQOjpj7GW9zQw60eZYF2CHQzhqL3TokCgYA0mqu6xyZd2fE7ZbeN8J0hT/QncUg4t1pOjoFOfkyL5vNSr4aydTUwRM7mQJ3ZaMHD1OL3glXoMZ3MoCjPIR9Sq5i72DDNj6TRDHE4d3HiXbl89Ir0i3DdxgvwJmL2vRWuT7o82+Ux9TigqrtmP3N3U8N2VI6V9I4tFlsIvTg4mA==";

    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjipaNHVCI3kUT4yKtj0QT7LCrZPeVxRriubc15lwLE5B8TYIkmf4Tm4/7o3pLqCxSziYZYNP4UOmBfgQchdx5ZyH/c3Q2HF+pA7mTmfUe7FW0RQ+/b79QzSIRzoW9/Cjn2r3qaqN4EZp485vNS9mctJp17G9SmUNJYnJPs7kjjNVRzl2smf1qq66FXFUT/tdw0gYyxSaC2RFKATAKtIqmZld/gwhz7cMqkn3c+pmkwyzarYcl6TO2jR+CC0aeSjW3FjKDwMJIO0aWxLrLprZwstQJTNM+VP8/Yhp+kYYO8Y+vzPrFVJE6ENwIdgIEkZtuyNhFMuOqbrl4zA1iUOSPwIDAQAB";

	public static String notify_url = "http://localhost:8080/shop_war/html/index.html?status=1";

	public static String return_url = "http://localhost:8080/shop_war/html/index.html?status=1";

	public static String sign_type = "RSA2";

	public static String charset = "utf-8";

	public static String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

	public static String log_path = "F:\\";



    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

