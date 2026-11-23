#!/bin/bash
# Build ONE HENDRIUS CHAT APK.
# Pipeline terbukti: aapt2 link -> javac --release 8 -> d8 -> zip dex
# -> zipalign -> apksigner. Satu codebase: web/index.html disalin ke
# assets/app/index.html sebelum build, jadi APK dan versi link memakai
# file yang sama persis (termasuk FIREBASE_CONFIG yang sama).
set -e
cd "$(dirname "$0")"
BT="$HOME/android-sdk/build-tools/34.0.0"
JAR="$HOME/android-sdk/platforms/android-34/android.jar"

# Satu codebase: salin aplikasi web (versi link) ke dalam APK.
cp web/index.html assets/app/index.html

rm -rf gen classes dexout res.zip unsigned.apk aligned.apk
mkdir -p gen classes dexout assets/app

if [ ! -f app.keystore ]; then
  keytool -genkeypair -keystore app.keystore -alias app -keyalg RSA -keysize 2048 \
    -validity 10000 -storepass android -keypass android -dname "CN=ONE HENDRIUS CHAT" -noprompt
fi

"$BT/aapt2" compile --dir res -o res.zip
"$BT/aapt2" link -o unsigned.apk --manifest AndroidManifest.xml -I "$JAR" --java gen -A assets res.zip
javac --release 8 -parameters -cp "$JAR" -d classes $(find src gen -name "*.java")
# d8: pass .class files explicitly (menolak direktori mentah; NPE tanpa -parameters).
"$BT/d8" --lib "$JAR" --output dexout $(find classes -name "*.class")
cd dexout && zip -q ../unsigned.apk classes.dex && cd ..
"$BT/zipalign" -f 4 unsigned.apk aligned.apk
"$BT/apksigner" sign --ks app.keystore --ks-pass pass:android --key-pass pass:android \
  --out one-hendrius-chat.apk aligned.apk
"$BT/apksigner" verify one-hendrius-chat.apk
echo "BUILD OK"
ls -la one-hendrius-chat.apk
unzip -l one-hendrius-chat.apk | grep -E "classes.dex|assets/app/index.html"
