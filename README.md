# Ingenico POS Receipt Printer Bridge

A React Native application that runs **on an Ingenico Android payment terminal** and prints
receipts on the device's built-in thermal printer, driven by print jobs pushed from a backend
over WebSockets.

The problem: React Native has no concept of a POS terminal's printer. The terminal exposes it
through a vendor **AIDL service** (`com.usdk.apiservice`) that's only reachable from native
Android. So the app is really two halves — a JS layer that receives jobs, and a custom native
module that knows how to talk to the hardware.

## How it works

```
backend  ──Pusher WebSocket──►  App.js  ──NativeModules bridge──►  ReactOneCustomMethod.java
                                                                            │
                                                                            ▼
                                                              UVectorPrinter (AIDL service)
                                                                            │
                                                                            ▼
                                                                   thermal receipt
```

1. **`App.js`** subscribes to a Pusher channel on launch. Each event carries the receipt payload
   as JSON — no polling, so a receipt prints the moment the backend emits it.
2. The payload crosses the bridge via **`NativeModules.ReactOneCustomMethod`**.
3. **`ReactOneCustomMethod.java`** (in `android/app/src/main/java/com/ingenico/`) binds to the
   terminal's printer service and renders the job through `UVectorPrinter`.

## The native module

`ReactOneCustomMethod` is registered with React Native through `ReactOnePackage.java` and exposes
these `@ReactMethod` entry points to JavaScript:

| Method | Purpose |
|---|---|
| `bind()` | Binds to the terminal's AIDL printer service. Called once on mount. |
| `register(boolean)` | Registers the module with the device service. |
| `startPrint(String json)` | Parses the receipt payload and renders it to the printer. |
| `getPhoneID(Promise)` | Returns the terminal's hardware ID — resolved as a JS Promise. |
| `show(String, int)` | Native toast, used for on-device debugging. |

Receipt layout is composed in Java rather than passed as a bitmap, so the output stays crisp at
the printer's native resolution:

- `addTitle(text)` — centred header line
- `addContent(col1, col2, col1Width, col2Width, bold)` — two-column row (item / amount) with
  explicit column widths, which is what keeps prices right-aligned on a fixed-width receipt
- `Dash(dash)` — separator rule

Printing is asynchronous: the module implements `OnPrintListener` (`onStart` / `onFinish` /
`onError`) so hardware faults — out of paper, overheating — surface as real errors rather than
silently dropped jobs.

## Stack

`React Native 0.70` · `React 18` · `Java` · Android AIDL (`com.usdk.apiservice`) ·
`@pusher/pusher-websocket-react-native`

## Running it

This app targets **physical Ingenico terminal hardware**. It builds in an emulator, but
`bind()` fails without the vendor printer service present, so printing can only be exercised
on-device.

```bash
yarn install
yarn android      # deploy to a connected terminal via adb
yarn test         # Jest
```

## Status & notes

Built in 2022 and **archived as portfolio work** — it is not maintained.

One thing I'd do differently today: the Pusher connection config in `App.js` is hardcoded and
belongs in build config. (The Pusher *key* is a public client-side identifier rather than a
secret — the app secret never touches the device — but it still shouldn't be inlined in
source.)
