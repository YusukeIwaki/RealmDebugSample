# RealmDebugSample

メッセンジャーライクなアプリのサンプルです。

![test](https://cloud.githubusercontent.com/assets/11763113/20866908/b5ca1470-ba7b-11e6-9d7d-0935e69c610c.gif)

Messageモデルがsyncstateという変数を持っていて、

* WAIT_FOR_SYNCのものをBackGroundServiceが拾って、API通信（ダミー）を実行して、SYNCEDもしくはERRORにする
* Activity側は、単純にMessageモデルを描画するだけで、API通信ができるかどうかは全く気にしていない

というのがポイント。

