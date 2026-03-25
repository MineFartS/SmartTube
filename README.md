# SmartTube

![The app screenshot](./images/browse_home.png)

SmartTube is a free and open-source advanced media player for Android TVs and TV boxes. It allows you to play content from various public sources.

## Main Features
- #### [No ads](#adblocking)
- #### [SponsorBlock](#sponsorblock) 

## Installation

- #### Stable
    - Install [Downloader by AFTVnews](https://www.aftvnews.com/downloader/) on your Android TV
    - Open it and enter `{TODO}`, then read, understand and confirm the security prompts.
- #### Debug

    - Install the following applications:

        - [Android Studio](https://developer.android.com/studio#:~:text=Download%20Android%20Studio)

        - [Git](https://git-scm.com/install/windows#:~:text=Windows/x64)

        - [OpenJDK 14](https://philh.myftp.biz/Media/Programs/Java/JDK/14.0.0.exe)
        *OpenJDK 14 or older (!) is required. Newer JDK could cause app crash!*

    - Run the following in Powershell:
        
        - Clone the Repository
        `git clone https://github.com/minefarts/SmartTube.git`

        - Enter the Clone
        `cd SmartTube`

        - Run the Setup Script
        `./setup.ps1`

        - Run the Build Script
        `./build.ps1`

---

### Adblocking

##### SmartTube does not show any ad banners, preroll ads or ad intermissions. 
It not just tries to prevent them, it is actually incapable of displaying ads, so YouTube cannot slip anything in.

---

### SponsorBlock

##### SmartTube includes a SponsorBlock integration.

> [SponsorBlock](https://sponsor.ajay.app/) is an open-source crowdsourced browser extension and open API for **skipping sponsor segments** in YouTube videos. [...] the extension automatically skips sponsors **it knows about** using a privacy preserving query system.

You can select which categories you want to skip in the settings.

---

### Casting

To cast videos from your phone (or other devices), you must link that device to your TV. Unlike the original YouTube app, SmartTube does not automatically show up when you are in the same wifi network. How to link your smartphone and TV:

1. open SmartTube and go to settings
2. go to "Remote control" (2nd option)
3. open your YouTube app on your phone, go to settings > General > watch on TV
4. click on _connect using TV-code_ and enter the code from your TV

[**Screenshot guide**](https://t.me/SmartTubeEN/8514)

Due to technical limitations, you need to open the app on the TV before casting; SmartTube cannot automatically wake up the TV.

---

## FAQ

- **Can you make SmartTube look like the original app?**

    - Compared to SmartTube's UI, Stock Youtube and YT Kids are far ahead. However, we'd need someone who's skilled and willing to dedicate enough time and energy into making it. And into maintaining it longterm (incl. new features, bug fixes). All of this for free. If you are / got someone like that, please help.
Not to mention that SmartTube follows Google's official template & recommendations for Android TV apps. It's Google's fault that the template is somewhat ugly. 😂

- **Can I install this on a Samsung Tizen TV / LG webOS TV / Roku / iOS / toaster?**

    - No, this only works on **Android** devices. If you look at an Android TV's product page, it usually says clearly that it's based on Android. The app **cannot** easily be ported over to other plattforms and we have no plans to even try. **Please do not ask**. Instead, you can connect a separate TV stick or TV box to your TV.


- **Can I install this on a smartphone? / Can you add portrait mode? / Scrolling doesn't work.**

    - **Big No**. This app is **not** for smartphones, we offer **zero support** for that.

    - You **can cast** videos **from** your smartphone to a TV / TV box running SmartTube, though. Just use the official YouTube app or [ReVanced](https://github.com/ReVanced), see [the casting section](#casting) for more information.

    - **There will not be a phone version.** You can use [ReVanced](https://github.com/ReVanced), [Pure Tuber](https://play.google.com/store/apps/details?id=free.tube.premium.advanced.tuber), [NewPipe](https://newpipe.schabi.org), or [NewPipe x SponsorBlock](https://github.com/polymorphicshade/NewPipe#newpipe-x-sponsorblock) instead. Please go to their respective support chats for help.

- **Can I install this on a tablet / car screen / smartphone with docking station?**

    - Yes... maybe.. Requirements:
        - It is an Android device
        - It has a large screen
        - It has a TV remote, controller, or keyboard
    - **Touch input is not supported.** Mouse/touchpad scrolling neither. You cannot properly use SmartTube with only touch or mouse input.

    - Some users reported great success (incl. on a [car entertainment system](https://t.me/SmartTubeEN/6060)). **Please share your success stories with us.**

- **Can updates be installed automatically?**

    - No, this is technically not possible. Only the preinstalled app manager (usually Google PlayStore, Amazon AppStore, etc) has the required permission. All other apps, incl. SmartTube can only show open installation prompt. A workaround using root would be possible, but hasn't been implemented yet.

- **Can I whitelist ads on some channels?**

    - No, this is not possible. SmartTube does not have any code to display ads. Adding this functionality would actually take time and effort, which is instead spent on adding useful features and fixing bugs.

---