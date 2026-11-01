# gen5w-preconditioning-simple
a simple app that gives you a manual preconditioning button on your EV6, set up for iPhone and CarPlay.

hold the TUNE knob, press **Start**, and Launchpad asks the car to start conditioning the battery. no navigation destination needed.

this is a personal fork of [paytondevs/gen5w-preconditioning](https://github.com/paytondevs/gen5w-preconditioning), which is the original work of paytondevs and its contributors. it isn't affiliated with or supported by that project, so please don't take problems with this fork to them.

## what's different in this fork

- Launchpad opens straight to preconditioning, with **Start** selected first on the TUNE knob.
- the Home tab, its Navigation, Music and Phone shortcuts, and the Default tab setting are gone. Kia's own apps are still under **Advanced → All screens**.
- the "Launchpad" title and the Car home button are gone, and the tabs are centered. use the car's own home button instead.
- the Projection shortcut is now **CarPlay**, under **Advanced**. it opens the car's phone connection settings, not CarPlay itself.
- Launchpad always follows the car's day/night display. the Appearance setting and the Appearance signals panel are gone.
- the EV6 guide is written for iPhone and CarPlay.

## will it work on my car?

this fork has been tried on one car: a US Kia EV6 with an iPhone, using CarPlay. other cars and phones may work but are untested with these changes.

Launchpad only runs on the exact cars and head-unit software versions in the [compatibility chart](COMPATIBILITY.md#compatibility-chart). what matters is the head unit's software version, not the model year. to get a new car or software version added, use the [original project](https://github.com/paytondevs/gen5w-preconditioning).

**EV6 owners:** start with the [EV6 installation and everyday-use guide](EV6-QUICK-START.md).

## before you start

> [!WARNING]
> This process involves making **unauthorized modifications to the infotainment system of your car.** One wrong move can render it **entirely unusable, leaving you with a very costly repair bill**. Take care to **never delete or modify any files in the car.** If you are unsure about **anything in this document**, don't leave it up to chance! This fork comes with no support, and you use it entirely at your own risk.

if you aren't comfortable modifying the infotainment system of your car, a [hardware mod](https://electroniqbuttons.com/products/ioniq-5-6-ev6-gv60-manual-preconditioning-button-beta-pre-order) exists that replicates this functionality in an easily reversible and safer manner.

**what you'll need:**
- a computer (Mac, Windows or Linux)
- a USB drive formatted **FAT32 or exFAT**. a small, empty one is easiest.
- a way to [reboot the headunit](https://www.facebook.com/groups/theioniqguy/posts/1355171539173388/) (the pinhole button in the climate control bar)
- about 15 minutes, parked

you **don't** need ADB, Wi-Fi, or a terminal.

## installation

### 1. install NEMESIS

[NEMESIS](https://github.com/xwtk/hmg_nemesis) is what lets us install things on the headunit. you only do this once. follow the **Quick Installation Guidance** in [its README](https://github.com/xwtk/hmg_nemesis#quick-installation-guidance). In short:

1. download [`ota_package.tar`](https://github.com/xwtk/hmg_nemesis/blob/main/ota_package.tar) (you don't need to build it yourself) and copy it to the root of the USB drive. don't extract it.
2. plug the drive into the car. "No Supported Files" is expected.
3. open Engineering Mode → **USB Copy** → **Update DSP** → **Update OTA**.
4. wait for it to install and restart the headunit. an **Error 684** afterwards is expected. **651** means the file didn't extract; check the drive and try again.

to check that it worked: press the **NAV** button three times quickly and **NEMESIS Manager** should open.

once that's done, delete `ota_package.tar` from the drive (or use a different drive).

### 2. put the Launchpad installer on the drive

this fork doesn't publish installers, so you build your own on GitHub. it's free and doesn't need any tools on your computer:

1. fork this repo, then open the **Actions** tab and enable workflows.
2. choose the **build** workflow, then **Run workflow** on `main`.
3. when it finishes, open the run and download its artifact. inside is **`Launchpad-install-usb.zip`**.

extract it so its **contents** sit at the **root** of the drive, with nothing else from NEMESIS left on it. the drive should look exactly like this:

```text
USB drive/
├── README.txt
├── NEMESIS.data
├── launchpad/
│   ├── common.sh
│   ├── install.sh
│   └── lib.sh
└── packages/
    └── paytondev.launchpad.zip
```

- **Mac:** double-clicking the zip makes a `Launchpad-install-usb` folder (Safari may already have done this for you). open that folder and copy everything **inside** it to the drive, not the folder itself.
- **Windows:** right-click the zip → **Extract All…**, and change the destination to the drive itself (e.g. `E:\`). by default Windows adds a subfolder, which won't work.
- don't unzip `packages/paytondev.launchpad.zip`. NEMESIS needs it as a zip.
- if an old `NEMESIS.data` or `NEMESIS.sh` is on the drive, move it off first. the car only looks at the one `NEMESIS.data` in the root.

<details>
<summary>or use the helper script (if you've cloned this repo)</summary>

it does the same thing, and moves anything NEMESIS-related already on the drive into a `NEMESIS-backup-<date>` folder instead of overwriting it.

```sh
sh tools/usb/write-usb.sh /Volumes/YOUR_USB ~/Downloads
```

on Windows PowerShell:
```powershell
.\tools\usb\write-usb.ps1 E:\ $HOME\Downloads
```

the last argument is the folder you downloaded the zip to.
</details>

### 3. run it in the car

1. park, turn the car on, and wait for the headunit to finish starting.
2. plug the drive into a USB **data** port (the one you'd use for music or CarPlay, not a charge-only port).
3. NEMESIS asks whether to run the item on the USB drive. tap **Yes**. you have 60 seconds. if you miss it, unplug the drive and plug it back in.
4. wait for the result dialog. it usually takes a few seconds and can take up to about 20.

the installer writes **`launchpad-install.log`** to the drive. if the dialog says it failed, that file says why. please include it if you ask me for help.

### 4. reboot and open it

reboot the headunit with the pinhole button and remove the drive. once it's back up, **hold the TUNE knob for about a second** and Launchpad should open.

you only need the USB drive again to update.

## using it

1. hold TUNE to open Launchpad on the preconditioning screen.
2. press **Start**. Launchpad shows **Request sent** while it waits for the car to say yes. if the battery is already warm, it asks you first and offers **Start anyway**.
3. when the car confirms, it shows **Active**. that's the only sign the battery is actually being conditioned. the car can still say no, for example if the battery is warm enough or the charge is too low.

if the car hasn't confirmed after five minutes on the EV6, Launchpad cancels the request for you. an **Active** session is never cut short.

press **Stop** to cancel, even while it's still waiting. closing Launchpad does **not** stop a request that's already running.

**dark mode:** Launchpad follows the head unit's Day/Night display setting, including Auto.

## updating

exactly the same as installing: build a new **`Launchpad-install-usb.zip`** from the latest code, put it on the drive, run it, and reboot. if Launchpad is already installed, it updates it in place, and the log says so.

your favorites and settings are kept. it also works if you installed an older version over ADB. running the drive twice is harmless.

## uninstalling

1. if preconditioning is running, press **Stop** and wait for it to end.
2. open NEMESIS Manager (press NAV three times) → **Packages** → **Launchpad** → **Uninstall**.
3. reboot the headunit.

this removes Launchpad and its TUNE shortcut. if you installed an older version over ADB and Launchpad isn't in the Packages list, run the USB installer once first, then uninstall. NEMESIS stays installed. if you want that gone too, use **Wipe NEMESIS** in NEMESIS Manager. see the [NEMESIS docs](https://github.com/xwtk/hmg_nemesis/blob/main/03-Install-and-Uninstall.md#5-uninstall-wipe).

## troubleshooting

| what you see | what to do |
| --- | --- |
| nothing happens when you plug in the drive | make sure `NEMESIS.data` is in the **root** of the drive, not in a folder. check the drive is FAT32/exFAT and in a data port. check NEMESIS is installed (press NAV three times; Manager should open). |
| "Invalid package descriptor" | the files were edited or copied incompletely. extract the zip again onto the drive. |
| the dialog says it failed | open `launchpad-install.log` on the drive. for more detail, open NEMESIS Manager → **Logs** → **Copy to USB**, which writes a `NEMESIS-logs` folder to the drive. |
| "not enough space on /system" in the log | nothing was changed. keep the log. |
| holding TUNE doesn't open Launchpad | reboot once more. in NEMESIS Manager → **Services**, `paytondev.launchpad/knobd` should be running. a different headunit may need a different TUNE profile. |
| preconditioning says **Unavailable** | your car or its software version isn't supported yet, so Launchpad won't send anything to the car. check the [compatibility chart](COMPATIBILITY.md#compatibility-chart). |
| it stays **Request sent** | the car hasn't confirmed yet. press **Stop**, or wait: Launchpad cancels it after five minutes on the EV6. **Start** comes back once the car reports Off. |

## what this changes on your car

- Launchpad is installed as a normal NEMESIS package, so it shows up in NEMESIS Manager → **Packages**, and uninstalling it from there removes everything.
- it adds **one** file to the car's system area, `/system/app/Launchpad.apk`, because that's the only way Android will show the app. no Hyundai or Kia files are changed.
- the installer checks everything first and puts the previous version back if anything goes wrong.

<details>
<summary>more detail</summary>

- the TUNE-hold service and a copy of the app live in `/data/nemesis/apps/paytondev.launchpad/`.
- before writing `/system/app/Launchpad.apk`, the installer copies and checks everything in `/data/local/tmp` (cleared on reboot) and checks there's enough space. `/system` is made writable only for that one write and is set back to read-only right after. if the written file doesn't match, the previous version is put back.
- uninstalling removes both.
</details>

already use ADB with your car? there are [manual ADB install steps](ADVANCED-ADB-INSTALL.md), but you don't need them.

## building from source

see [BUILDING.md](BUILDING.md).

## credits

- Launchpad: [paytondevs](https://github.com/paytondevs) and the contributors to the [original project](https://github.com/paytondevs/gen5w-preconditioning)
- EV6 development, documentation, and vehicle testing: [Abro86](https://github.com/Abro86), with development, documentation, and host-test assistance from OpenAI Codex.
- [https://github.com/xwtk/hmg_nemesis](https://github.com/xwtk/hmg_nemesis) - the NEMESIS exploit that made this all possible
- [https://github.com/tylerharvey/Ioniq5_CAN](https://github.com/tylerharvey/Ioniq5_CAN) - reverse engineered the CAN messages used to start preconditioning
- [https://github.com/Helloyunho/gen5w-utils](https://github.com/Helloyunho/gen5w-utils) - other scripts and tools for reverse engineering Gen5W
