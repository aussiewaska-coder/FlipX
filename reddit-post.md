# Reddit post draft — r/MobileLinux

**Title:**
No DeX on my Flip6, so I built FlipX — full Debian XFCE desktop with cover-screen + external monitor support

**Body:**

Hey r/MobileLinux! Like a lot of Samsung owners, I watched phone after phone
get DeX while the Flip line got nothing. So I stopped waiting and put together
FlipX: a full Debian XFCE desktop running on my Flip6 via Termux + Termux:X11 +
proot-distro, plus a couple of scripts that make the whole thing actually
behave like a real setup.

What it does:

- Full Debian XFCE desktop, launched with one command (`./guistart`)
- `displayResolutionMode:native` — the X server adopts whatever screen it's on:
  full widescreen when open, proper square 720×748 on the cover screen when
  folded (no squash), external monitor over USB-C
- `restoggle` — a tiny watcher that forces 16:9 only while the desktop is in
  the foreground, then snaps the phone back to native resolution for normal
  Android apps. Resets everything on logout, so the phone is never stuck in a
  forced mode
- Phone-as-trackpad: on an external monitor, set Termux:X11 Touch Mode to
  Trackpad and the phone screen becomes a big, precise trackpad. Honestly this
  combo is nicer than DeX ever was for my use
- Working Chrome (ARM64 build, proot flags baked into a launcher) and mouse
  sensitivity taming via xinput

Repo (scripts + setup guide): https://github.com/aussiewaska-coder/FlipX

Compatibility: tested on the Flip6; should work on Flip5/4, other DeX-less
Galaxies, and honestly any modern Android. Would love testers — especially
cover-screen behavior on the Flip5, and whether "Force desktop mode" does
anything on other non-DeX Samsungs.

Honest caveats: software rendering only (no GPU under proot, Chrome needs
`--disable-gpu`), ~1s flicker when the resolution auto-switches, cover screen
needs Good Lock/MultiStar, and the resolution switching needs wireless ADB to
localhost. Battery life on an external monitor: bring a charger. 😅

Happy to answer questions and take feedback — and if someone gets it running
on a Fold or an A-series, tell me and I'll update the compat table.

---

**Posting tips:** post to r/MobileLinux (suggest the "Showcase" / project flair
if available), attach `flipx.jpeg` as the post image — image posts get far more
eyes. Consider x-posting to r/termux once the discussion gets going.
