# SmartParkingSystem
2015 Java/Orale Database認證日－未來的浪潮，Java嵌入式、Raspberry Pi與樂高Mindstorms EV3

示範影片網址：<a href="http://youtu.be/Eit_9ONI364">http://youtu.be/Eit_9ONI364</a>

##SmartParkingDemo
這個應用程式必須在下列的環境執行：

* Raspberry Pi Model B Rev 2或Model B+
* 已經建立與設定好作業系統記憶卡
* Raspberry Pi與工作電腦在同一個區域網路

這是一個Java Embedded應用程式專案。關於Raspberry Pi與Java Embedded應用程式開發環境，請參考<a href="http://www.codedata.com.tw/java/java-embedded-getting-started-from-raspberry-pi/" target="_blank">http://www.codedata.com.tw/java/java-embedded-getting-started-from-raspberry-pi/</a>。

這個應用程式專案使用Apache Mina傳送與接收訊息，請在<a href="https://mina.apache.org/" target="_blank">https://mina.apache.org/</a>下載。
##SmartParkingEV3
這是一個LEJOS應用程式專案。LEJOS應用程式開發環境請參考<a href="http://www.codedata.com.tw/java/lego-mindstorms-ev3-lejos-mac-installation/" target="_blank">http://www.codedata.com.tw/java/lego-mindstorms-ev3-lejos-mac-installation/</a>(Mac OS)，或是<a href="http://www.codedata.com.tw/java/lego-mindstorms-ev3-lejos-windows-installation/" target="_blank">http://www.codedata.com.tw/java/lego-mindstorms-ev3-lejos-windows-installation/</a>(Windows)。建立好LEJOS開發環境以後，就可以開啟這個應用程式專案。
##SmartParkingMobile
這是一個Android應用程式專案，專案使用的開發工具是Android Studio，所以你必須使用Android Studio開啟這個專案。Android Studio的相關資訊請參考<a href="http://developer.android.com/sdk/index.html" target="_blank">http://developer.android.com/sdk/index.html</a>。
##應用程式執行順序
依照下列的順序啟動應用程式：

1. SmartParkingEV3
2. SmartParkingDemo
3. SmartParkingMobile

##關於作者
* 個人檔案：<a href="http://tw.linkedin.com/in/macdidi5/zh-tw" target="_blank">http://tw.linkedin.com/in/macdidi5/zh-tw</a>
* 專欄文章：<a href="http://www.codedata.com.tw/author/michael" target="_blank">http://www.codedata.com.tw/author/michael</a>
* 電子郵件：macdidi5@gmail.com