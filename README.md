# gen5w-preconditioning-simple

A simple app that adds a manual battery preconditioning button to your Kia EV6.

Hold the TUNE knob, press **Start**, and the app asks the car to start conditioning the battery. You don't need a navigation destination. The app is called Launchpad, which is the name you'll see in NEMESIS Manager and in this guide.

## What it is

- **A manual preconditioning button.** It lets you ask the car to condition the battery before DC fast charging, without setting a charger as a route in Kia's navigation.
- **A battery temperature display.** It shows the lowest reported battery pack temperature, a short history chart, and the checks the car must pass before a request can be sent.
- **A personal fork.** This is my own version of [paytondevs/gen5w-preconditioning](https://github.com/paytondevs/gen5w-preconditioning), simplified for how I use my car. It has been tried on one car: a US Kia EV6 Light Long Range on head-unit software `260507`.
- **An unofficial modification.** It runs on the car's infotainment system through [NEMESIS](https://github.com/xwtk/hmg_nemesis), a third-party tool.

## What it is not

- **It is not made or supported by Kia or Hyundai.**
- **It is not supported by the original project.** It isn't affiliated with paytondevs, so please don't take problems with this fork to them. This fork also comes with no support of its own.
- **It is not a phone projection app.** It doesn't appear inside phone projection, and it can't open a projection session directly. The **Phone Projection** shortcut opens the car's phone connection settings.
- **It is not a launcher or a replacement for Kia's home screen.** The original project's Home tab and shortcuts have been removed.
- **It is not a heater control.** It doesn't send raw CAN commands or override anything. The car's battery system decides whether to start, and it can say no.
- **It is not a guarantee of faster charging.** **Active** only means the car reports that conditioning is running.
- **It is not a remote or scheduled feature.** You start it from the screen in the car, and it doesn't use a phone app or an OBD adapter.
- **It is not a ready-made download.** This fork doesn't publish installers, so you build your own, as described below.

## What's different in this fork

- Launchpad opens straight to the preconditioning screen.
- The Home tab, its Navigation, Music and Phone shortcuts, and the Default tab setting are gone. Kia's own apps are still available under **Advanced → All Screens**.
- The "Launchpad" title and the **Car home** button are gone, and the tabs are centered. Use the car's own home button instead.
- The Projection shortcut is now **Phone Projection**, and it has moved to **Advanced**.
- Launchpad always follows the car's Day/Night display. The Appearance setting and the Appearance signals panel are gone.
- The Battery warm message is shorter, and several labels and layouts have been tidied up.

## Will it work on my car?

Other cars and phones may work, but they are untested with these changes.

Launchpad only runs on the exact cars and head-unit software versions in the [compatibility chart](COMPATIBILITY.md#compatibility-chart). What matters is the head unit's software version, not the model year. To get a new car or software version added, use the [original project](https://github.com/paytondevs/gen5w-preconditioning).

**EV6 owners:** Start with the [EV6 installation and everyday-use guide](EV6-QUICK-START.md).

## Before you start

> [!WARNING]
> This process involves making **unauthorized modifications to the infotainment system of your car.** One wrong move can render it **entirely unusable, leaving you with a very costly repair bill**. Take care to **never delete or modify any files in the car.** If you are unsure about **anything in this document**, don't leave it up to chance. This fork comes with no support, and you use it entirely at your own risk.

If you aren't comfortable modifying your car's infotainment system, a [hardware mod](https://electroniqbuttons.com/products/ioniq-5-6-ev6-gv60-manual-preconditioning-button-beta-pre-order) offers the same function in a safer, easily reversible way.

**What you'll need:**

- A computer (Mac, Windows, or Linux)
- A USB drive formatted as **FAT32 or exFAT** (a small, empty one is easiest)
- A way to [reboot the head unit](https://www.facebook.com/groups/theioniqguy/posts/1355171539173388/) (the pinhole button in the climate control bar)
- About 15 minutes, parked

You **don't** need ADB, Wi-Fi, or a terminal.

## Installation

### 1. Install NEMESIS

[NEMESIS](https://github.com/xwtk/hmg_nemesis) is what makes it possible to install apps on the head unit. You only need to do this once. Follow the **Quick Installation Guidance** in [its README](https://github.com/xwtk/hmg_nemesis#quick-installation-guidance). In short:

1. Download [`ota_package.tar`](https://github.com/xwtk/hmg_nemesis/blob/main/ota_package.tar) and copy it to the root of the USB drive. You don't need to build it yourself, and you shouldn't extract it.
2. Plug the drive into the car. "No Supported Files" is expected.
3. Open Engineering Mode → **USB Copy** → **Update DSP** → **Update OTA**.
4. Wait for it to install and restart the head unit. **Error 684** afterwards is expected. **Error 651** means the file didn't extract, so check the drive and try again.

To check that it worked, press the **NAV** button three times quickly. **NEMESIS Manager** should open.

When that's done, delete `ota_package.tar` from the drive, or use a different drive for the next step.

### 2. Put the Launchpad installer on the drive

This fork doesn't publish installers, so you build your own on GitHub. It's free and doesn't need any tools on your computer:

1. Fork this repo, then open the **Actions** tab and enable workflows.
2. Choose the **build** workflow, then choose **Run workflow** on `main`.
3. When it finishes, open the run and download its artifact. Inside is **`Launchpad-install-usb.zip`**.

Extract that zip so its **contents** sit at the **root** of the drive, with nothing else from NEMESIS left on it. The drive should look exactly like this:

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

- **Mac:** Double-clicking the zip creates a `Launchpad-install-usb` folder, and Safari may already have done this for you. Open that folder and copy everything **inside** it to the drive, not the folder itself.
- **Windows:** Right-click the zip, choose **Extract All…**, and change the destination to the drive itself (for example, `E:\`). By default, Windows adds a subfolder, which won't work.
- **Inner zip:** Don't unzip `packages/paytondev.launchpad.zip`. NEMESIS needs it as a zip.
- **Old files:** If an old `NEMESIS.data` or `NEMESIS.sh` is on the drive, move it off first. The car only looks at the one `NEMESIS.data` in the root.

<details>
<summary>Or use the helper script, if you've cloned this repo</summary>

The script does the same thing, and it moves anything NEMESIS-related already on the drive into a `NEMESIS-backup-<date>` folder instead of overwriting it.

```sh
sh tools/usb/write-usb.sh /Volumes/YOUR_USB ~/Downloads
```

On Windows PowerShell:

```powershell
.\tools\usb\write-usb.ps1 E:\ $HOME\Downloads
```

The last argument is the folder you downloaded the zip to.
</details>

### 3. Run it in the car

1. Park, turn the car on, and wait for the head unit to finish starting.
2. Unplug your phone if it's using the port, then plug the drive into a USB **data** port (the one you'd use for phone projection, not a charge-only port).
3. NEMESIS asks whether to run the item on the USB drive. Tap **Yes** within 60 seconds. If you miss it, unplug the drive and plug it back in.
4. Wait for the result dialog. It usually takes a few seconds and can take up to about 20.

The installer writes **`launchpad-install.log`** to the drive. If the dialog says the install failed, that file explains why.

### 4. Reboot and open it

Reboot the head unit with the pinhole button and remove the drive. Once it's back up, **hold the TUNE knob for about a second**, and Launchpad should open.

You only need the USB drive again to update.

## Using it

1. Hold TUNE to open Launchpad on the preconditioning screen.
2. Press **Start**. Launchpad shows **Request sent** while it waits for the car to respond. If the battery is already warm, it shows **Battery warm** and offers **Start anyway**.
3. When the car confirms, Launchpad shows **Active**. That's the only sign the battery is actually being conditioned. The car can still decline, for example if the battery is warm enough or the charge is too low.

If the car hasn't confirmed after five minutes on the EV6, Launchpad cancels the request for you. An **Active** session is never cut short.

Press **Stop** to cancel, even while Launchpad is still waiting. Closing Launchpad does **not** stop a request that's already running.

**Dark mode:** Launchpad follows the head unit's Day/Night display setting, including Auto.

## Updating

Updating works the same way as installing. Build a new **`Launchpad-install-usb.zip`** from the latest code, put it on the drive, run it, and reboot. If Launchpad is already installed, the installer updates it in place, and the log says so.

Your favorites and settings are kept. Updating also works if you installed an older version over ADB, and running the drive twice is harmless.

## Uninstalling

1. If preconditioning is running, press **Stop** and wait for it to end.
2. Open NEMESIS Manager (press **NAV** three times) → **Packages** → **Launchpad** → **Uninstall**.
3. Reboot the head unit.

This removes Launchpad and its TUNE shortcut. If you installed an older version over ADB and Launchpad isn't in the Packages list, run the USB installer once first, then uninstall.

NEMESIS stays installed. If you want to remove that too, use **Wipe NEMESIS** in NEMESIS Manager. See the [NEMESIS docs](https://github.com/xwtk/hmg_nemesis/blob/main/03-Install-and-Uninstall.md#5-uninstall-wipe).

## Troubleshooting

| What you see | What to do |
| --- | --- |
| Nothing happens when you plug in the drive | Make sure `NEMESIS.data` is in the **root** of the drive, not in a folder. Check that the drive is FAT32 or exFAT and is in a data port. Check that NEMESIS is installed: press **NAV** three times, and Manager should open. |
| "Invalid package descriptor" | The files were edited or copied incompletely. Extract the zip onto the drive again. |
| The dialog says the install failed | Open `launchpad-install.log` on the drive. For more detail, open NEMESIS Manager → **Logs** → **Copy to USB**, which writes a `NEMESIS-logs` folder to the drive. |
| "Not enough space on /system" in the log | Nothing was changed. Keep the log. |
| Holding TUNE doesn't open Launchpad | Reboot once more. In NEMESIS Manager → **Services**, `paytondev.launchpad/knobd` should be running. A different head unit may need a different TUNE profile. |
| Preconditioning says **Unavailable** | Your car or its software version isn't supported yet, so Launchpad won't send anything to the car. Check the [compatibility chart](COMPATIBILITY.md#compatibility-chart). |
| It stays on **Request sent** | The car hasn't confirmed yet. Press **Stop**, or wait for Launchpad to cancel it after five minutes on the EV6. **Start** comes back once the car reports Off. |

## What this changes on your car

- Launchpad is installed as a normal NEMESIS package, so it shows up in NEMESIS Manager → **Packages**, and uninstalling it from there removes everything.
- It adds **one** file to the car's system area, `/system/app/Launchpad.apk`, because that's the only way Android will show the app. No Hyundai or Kia files are changed.
- The installer checks everything first and puts the previous version back if anything goes wrong.

<details>
<summary>More detail</summary>

- The TUNE-hold service and a copy of the app live in `/data/nemesis/apps/paytondev.launchpad/`.
- Before writing `/system/app/Launchpad.apk`, the installer copies and checks everything in `/data/local/tmp` (cleared on reboot) and checks that there's enough space. `/system` is made writable only for that one write and is set back to read-only right after. If the written file doesn't match, the previous version is put back.
- Uninstalling removes both.
</details>

Already use ADB with your car? There are [manual ADB install steps](ADVANCED-ADB-INSTALL.md), but you don't need them.

## Building from source

See [BUILDING.md](BUILDING.md).

## Credits

- **Launchpad:** [paytondevs](https://github.com/paytondevs) and the contributors to the [original project](https://github.com/paytondevs/gen5w-preconditioning)
- **EV6 development, documentation, and vehicle testing:** [Abro86](https://github.com/Abro86), with development, documentation, and host-test assistance from OpenAI Codex
- **[hmg_nemesis](https://github.com/xwtk/hmg_nemesis):** The NEMESIS exploit that made this possible
- **[Ioniq5_CAN](https://github.com/tylerharvey/Ioniq5_CAN):** Reverse engineering of the CAN messages used to start preconditioning
- **[gen5w-utils](https://github.com/Helloyunho/gen5w-utils):** Other scripts and tools for reverse engineering Gen5W
