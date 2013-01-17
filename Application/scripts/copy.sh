/home/tcp/Android\ SDK/android-sdk-linux/platform-tools/adb -s HT247W101309 push ~/logs/alexa.txt storage/sdcard0/DCIM/Shared/alexa.txt
/home/tcp/Android\ SDK/android-sdk-linux/platform-tools/adb -s 014994B110009006 push ~/logs/alexa.txt storage/sdcard0/DCIM/Shared/alexa.txt
/home/tcp/Android\ SDK/android-sdk-linux/platform-tools/adb -s 01498B320C00C00E push ~/logs/alexa.txt storage/sdcard0/DCIM/Shared/alexa.txt
/home/tcp/Android\ SDK/android-sdk-linux/platform-tools/adb -s 0A3BDADA03014005 push ~/logs/alexa.txt storage/sdcard0/DCIM/Shared/alexa.txt

/home/tcp/Android\ SDK/android-sdk-linux/platform-tools/adb -s HT247W101309 pull storage/sdcard0/DCIM/Shared/+log.txt ~/logs/phone1/log-curr.txt
/home/tcp/Android\ SDK/android-sdk-linux/platform-tools/adb -s 014994B110009006 pull storage/sdcard0/DCIM/Shared/+log.txt ~/logs/phone2/log-curr.txt
/home/tcp/Android\ SDK/android-sdk-linux/platform-tools/adb -s 01498B320C00C00E pull storage/sdcard0/DCIM/Shared/+log.txt ~/logs/phone3/log-curr.txt
/home/tcp/Android\ SDK/android-sdk-linux/platform-tools/adb -s 0A3BDADA03014005 pull storage/sdcard0/DCIM/Shared/+log.txt ~/logs/phone4/log-curr.txt
