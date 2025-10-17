# SmartTube

![App Homepage](https://github.com/yuliskov/SmartTube/blob/master/images/browse_home.png?raw=true)

**SmartTube** is a *free* and *open-source* YouTube client for Android and Fire TVs.

<hr>

## Features

### Adblocking

- SmartTube does not show any ad banners, preroll ads, or ad intermissions. It doesn't skip them, but it is **incapable** of displaying ads.
- Some YouTube
channels include sponsored messages in their videos, these can also be skipped,
see [SponsorBlock](#SponsorBlock) below.

### SponsorBlock

SmartTube includes a SponsorBlock integration. From
the [SponsorBlock website](https://sponsor.ajay.app/):

> SponsorBlock is an open-source crowdsourced browser extension and open API for **skipping sponsor
segments** in YouTube videos. [...] the extension automatically skips sponsors **it knows about**
> using a privacy preserving query system. It also supports skipping **other categories**, such as
> intros, outros and reminders to subscribe [and non-music parts in music videos].

You can select which categories you want to skip in the settings.
Note that SponsorBlock is a free and voluntary project based on user submissions, so
don't expect it to 100% work every time. Sometimes, sponsor segments are not yet submitted to the
database, sometimes the SponsorBlock servers are offline/overloaded.

### Adjust Speed

You can adjust the playback speed pressing the speed-indicator icon (gauge) in the top row of the
player. This is remembered across videos. Some speeds may case frame drops, this is a known issue.

<hr>

## Build

**NOTE: OpenJDK 14 or older (!) is required. Newer JDK could cause app crash!**  
To build and install debug version, run these commands:

```
git clone https://github.com/yuliskov/SmartTube.git
cd SmartTube
git submodule update --init
adb connect <device_ip_address>
gradlew clean installStorigDebug
```
