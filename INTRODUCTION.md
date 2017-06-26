# 為你的 iOS 家庭搭建最強的 Homebridge 支援﹣MiHomePlus 介紹

MiHomePlus 是我為 iOS 家庭編寫的 Android App，主要的作用就是作為 iOS 家庭的操作代理。

MiHomePlus 的**工作原理**是這樣的：
1. MiHomePlus 調用 Android 「無障礙」特性，監視和控制米家 App。
2. 當無障礙功能觸發「TYPE_WINDOW_CONTENT_CHANGED」事件時候，與另一項目 MiPlusServer 通信，把監視的設備狀態同步給 MiPlusServer。
3. MIPlusServer 從 Homebridge 收取到操作通知時候，基於 Socket.io 通知 MiHomePlus 操作米家 App 去切換設備狀態。
4. 在 Homebridge 基於 Switcheroo 插件提供的操作接口。
5. MiPlusServer 是 Web 接口。

## 關聯項目

##### MiHomePlus
https://github.com/qoli/MiHomePlus

##### MiPlusServer
https://github.com/qoli/MiPlusServer

## 準備設備

1. Pi 一枚。
2. 閒置 Android 手機一個。

Pi 我用了 NanoPi，59 元那個 256MB 的版本就足夠了。然而 Android 手機最低要求是 4.2.1 版本的，基於 API 19，因為我是基於這個版本做的開發。

## 初始化 NanoPi 環境
我們需要在 NanoPi 搭建 Homebridge 和安裝 MIPlusServer。

### 安裝 Homebridge

參考這幾遍文章完成 Homebridge 的安裝。
1. http://wiki.friendlyarm.com/wiki/index.php/NanoPi_NEO/zh#.E5.87.86.E5.A4.87.E5.B7.A5.E4.BD.9C
2. http://blog.yongliang.info/2017/0101_play_with_nanopi/
3. http://djzhang.com/nozuonofun/realize-homekit-with-raspberry-pi-and-xiaomi-smart-devices/

建議安裝的 Homebridge 插件：
1. homebridge-yeelight 控制燈
2. homebridge-mi-aqara 改良版的 aqara 網關
3. **homebridge-switcheroo** MIPlusServer 基於這個插件和 Homebridge 通信的。

**homebridge-miio** 這個插件聽說可以控制第一代 WIFI 插座，我沒裝，所以我不知道。

當你完成 Homebridge 的安裝后，我們就要開始進行 **MIPlusServer** 的安裝。

### 安裝 MIPlusServer

#### 第一步，先 SSH Login 到你的 NanoPi，然後執行如下的命令
```shell
git clone https://github.com/qoli/MiPlusServer.git
cd ./MiPlusServer
npm i
chmod +x miServer.sh
chmod +x run.sh
```

#### 第二步，編寫你的 config.js 配置檔案

```shell
touch config.js
nano config.js
```

##### 配置檔
```javascript
module.exports = {
  tgbot: false,
  token: "",
  adminChatID: ""
}
```

#### 第三步，啟動 Telegram BOT（可選步驟）

如果你有啟用 Telegram Bot 作為監視 MIPlusServer 的運行必要，可以參考
https://neighborhood999.github.io/2016/07/19/Develop-telegram-bot/
這個教程，來獲取 Telegram BOT 的 token。

##### 暫時運行服務器
```javascript
module.exports = {
  tgbot: false,
  token: "Your Token Here.",
  adminChatID: ""
}
```

接著，先按照配置檔保存一下，使用 `./miServer.sh` 先讓服務器運行起來。

##### 獲取 adminChatID
按照下圖的辦法，加上你自己的機器人，就輸入 `id` 命令。機器人就會向你返回你的 Chat id 了。

![Untitled](http://ok7ct2124.bkt.clouddn.com/2017-06-27-Untitled.png)

```javascript
module.exports = {
  tgbot: false,
  token: "Your Token Here.",
  adminChatID: "Your chat id"
}
```

保存配置檔案即可。

#### 第四步，運行服務器

```shell
screen -S miServer
./miServer.sh
```

![螢幕快照 2017-06-26 下午4.33.47](http://ok7ct2124.bkt.clouddn.com/2017-06-27-螢幕快照 2017-06-26 下午4.33.47.png)


你看到這樣的信息就正確了。

隨後，你應該看到這個屏幕。那麼就可以按下「CTRL + A；CTRL + D」來退出 screen 屏幕。我教你記住的口訣，**控制你的 AD 鈣奶**。

現在，你的 NanoPi 服務器就初始化完成了。

## 安裝 MiHomePlus App 到 Android

### 安裝 App
打開 **https://github.com/qoli/MiHomePlus/releases** 下載 **MiHomePlus** 當前的發佈版本。

不過當前 App 我正在被標記為 0.1 版本，可能會有一些問題。也有可能在運行中就退出了。但是，我測試過，可以成功運行 1 天了。第二天我就沒遇到問題，只是我把他拿回來繼續開發了。

### 啟動 App

![MiHomeIMG](http://ok7ct2124.bkt.clouddn.com/2017-06-27-MiHomeIMG.png)

在啟動 App 后，首先，我們進行設定。

### App 設定

#### 第一步，米家 App 的調整

啟動米家的 App，把需要監視的設備都放在同一個房間中。Homebridge 插件能控制的就不要放進來了。

###### 注意
在當前版本下，在一個屏幕之外的設備無法監控。

按照圖片的步驟，把監控的設備都整理到 **AndroidAPI** 的房間中，當然，你可以叫其他的名字，例如「**MiPlusDevices**」。
然後，你的米家就應該像圖二一樣的狀態。

![MiHomePlus.002](http://ok7ct2124.bkt.clouddn.com/2017-06-27-MiHomePlus.002.jpeg)

![MiHomeIMG2](http://ok7ct2124.bkt.clouddn.com/2017-06-27-MiHomeIMG2.png)


#### 第二步，打開 Web 設定頁面

打來瀏覽器，我的 NanoPi 是 http://192.168.1.104:3002 。
所以，打開 **http://192.168.1.104:3002/setting**。

填寫房間名稱和設備列表，設備列表要使用「;」半角分號分開哦。在我這裡，主要就監視了這 4 個設備。

###### 注意
監控設備的名字必須和米家 App 顯示的名字一樣哦。

1. 空調伴侶 ﹣ 坑爹的空調伴侶不能被 Homebridge 控制！！！
2. 電腦燈﹣這個是使用了第一代的智能插座。
3. 落地燈﹣我也忘記這是什麼插座了，不連接網關的。
4. 空氣淨化器﹣還是第一代智能插座……

OK，就這些了哦。

![螢幕快照 2017-06-26 下午11.02.27](http://ok7ct2124.bkt.clouddn.com/2017-06-27-螢幕快照 2017-06-26 下午11.02.27.png)
#### 第三部，設定 Homebridge 配置檔

###### 示例代碼：
```
{
      "accessory": "Switcheroo",
      "type": "switch",
      "name": "空調伴侶",
      "host": "http://192.168.1.104:3002/device/%E7%A9%BA%E8%AA%BF%E4%BC%B4%E4%BE%B6",
      "on": "/ON",
      "off": "/OFF",
      "on_body": "ON",
      "off_body": "OFF"
    }
```

1. name﹣這個你真可以隨便叫，只會影響在 iOS 家庭 App 的顯示名字；
2. host﹣請務必輸入「http://192.168.1.104:3002/device/**設備名字**」設備名字一定要經過 URLEncode。
3. 有多少個設備就把上面的示例代碼添加多少次。

給大家一個 URLEncode 的網址：https://www.urlencoder.org

![MiHomePlus.005](http://ok7ct2124.bkt.clouddn.com/2017-06-27-MiHomePlus.005.jpeg)


#### 第四步，設定 MiHomePlus！

1. 選擇「App 設定」
2. 設定伺服器地址。

我的 NanoPi 就是 192.168.1.104 嘛。所以輸入了 http://192.168.1.104:3002 。
保存后使用「讀取配置檔案」，就會看到按鈕下方的文字更新過來了。數據就會保存在 App 裡面了。

![MiHomePlus.003](http://ok7ct2124.bkt.clouddn.com/2017-06-27-MiHomePlus.003.jpeg)

#### 第五步，關閉小米的神隱模式（小米的才需要）

這個神隱模式嘛，一開始我是不知道的。在開發過程中，每 5 分鐘就遇到 Socket.io 無故斷線，只有重啟才能恢復。後來轉用錘子開發，發現沒有遇到這個問題。我就上網 Google 了一下，才發現小米的神隱模式這個功能導致的。

![MiHomePlus.001](http://ok7ct2124.bkt.clouddn.com/2017-06-27-MiHomePlus.001.jpeg)


#### 第六步，啟動 App

做了這麼多事情，終於開始了！
把你的 Android 拿去充電吧。反正我的紅米第一代，不充電可以運行一天。

![MiHomePlus.004](http://ok7ct2124.bkt.clouddn.com/2017-06-27-MiHomePlus.004.jpeg)



## 享受完整 HomeKit 帶來的快感吧。

![IMG_5182](http://ok7ct2124.bkt.clouddn.com/2017-06-27-IMG_5182.jpg)

![IMG_5178](http://ok7ct2124.bkt.clouddn.com/2017-06-27-IMG_5178.jpg)

![IMG_5240](http://ok7ct2124.bkt.clouddn.com/2017-06-27-IMG_5240.jpg)


## Telegram BOT？
你啟動了這個選項的話。
就可以收到一些 MiHomePlus 的狀態。
![](http://ok7ct2124.bkt.clouddn.com/2017-06-27-14984924878827.jpg)


