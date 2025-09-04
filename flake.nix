{
  description = "Android development shell with Android Studio on NixOS";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.05";
    nixpkgs-unstable.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = inputs@{ self, nixpkgs, ... }:
    let
      systems = [ "x86_64-linux" ];
      forEachSystem = f: builtins.listToAttrs (map (system: { name = system; value = f system; }) systems);
    in
    {
      devShells = forEachSystem (system:
        let
          pkgsStable = import nixpkgs { inherit system; };
          pkgsUnstable = import inputs."nixpkgs-unstable" { inherit system; };
        in {
          default = pkgsUnstable.mkShell {
            name = "android-dev-shell";
            buildInputs = [
              pkgsUnstable.android-studio
              pkgsStable.jdk17
              pkgsStable.android-tools # adb, fastboot
              pkgsStable.gradle
              pkgsStable.git
              pkgsStable.go

              pkgsStable.xorg.libX11
              pkgsStable.pulseaudio
              pkgsStable.libpng
              pkgsStable.gperftools
              pkgsStable.protobuf
              pkgsStable.libcxx
              pkgsStable.nss
              pkgsStable.nspr
              pkgsStable.expat
              pkgsStable.libdrm
              pkgsStable.xorg.libxcb
              pkgsStable.xorg.libXext
              pkgsStable.xorg.libXdamage
              pkgsStable.xorg.libXfixes
              pkgsStable.xorg.libXcomposite
              pkgsStable.xorg.libXrender
              pkgsStable.xorg.libXtst
              pkgsStable.xorg.libXi
              pkgsStable.xorg.libXcursor
              pkgsStable.xorg.libXrandr
              pkgsStable.xorg.libXinerama
              pkgsStable.xorg.libxkbfile
              pkgsStable.libbsd

              # Qt xcb and related runtime libs for Android Emulator
              pkgsStable.libxkbcommon
              pkgsStable.xorg.xcbutil
              pkgsStable.xorg.xcbutilimage
              pkgsStable.xorg.xcbutilkeysyms
              pkgsStable.xorg.xcbutilrenderutil
              pkgsStable.xorg.xcbutilwm
              pkgsStable.xorg.xcbutilcursor
              pkgsStable.libglvnd
              pkgsStable.mesa
            ];

            shellHook = ''
              export ANDROID_HOME="$HOME/Android"
              export ANDROID_SDK_ROOT="$ANDROID_HOME"
              export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH:$ANDROID_HOME/tools/bin"
              export JAVA_HOME="${pkgsStable.jdk17.home}"
              export LD_LIBRARY_PATH="$ANDROID_HOME/emulator/lib64:$ANDROID_HOME/emulator/lib64/qt/lib''${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}:${pkgsStable.xorg.libX11}/lib:${pkgsStable.pulseaudio}/lib:${pkgsStable.libpng}/lib:${pkgsStable.gperftools}/lib:${pkgsStable.protobuf}/lib:${pkgsStable.libcxx}/lib:${pkgsStable.nss}/lib:${pkgsStable.nspr}/lib:${pkgsStable.expat}/lib:${pkgsStable.libdrm}/lib:${pkgsStable.xorg.libxcb}/lib:${pkgsStable.xorg.libXext}/lib:${pkgsStable.xorg.libXdamage}/lib:${pkgsStable.xorg.libXfixes}/lib:${pkgsStable.xorg.libXcomposite}/lib:${pkgsStable.xorg.libXrender}/lib:${pkgsStable.xorg.libXtst}/lib:${pkgsStable.xorg.libXi}/lib:${pkgsStable.xorg.libXcursor}/lib:${pkgsStable.xorg.libXrandr}/lib:${pkgsStable.xorg.libxkbfile}/lib:${pkgsStable.libbsd}/lib:${pkgsStable.libxkbcommon}/lib:${pkgsStable.xorg.xcbutil}/lib:${pkgsStable.xorg.xcbutilimage}/lib:${pkgsStable.xorg.xcbutilkeysyms}/lib:${pkgsStable.xorg.xcbutilrenderutil}/lib:${pkgsStable.xorg.xcbutilwm}/lib:${pkgsStable.xorg.xcbutilcursor}/lib:${pkgsStable.libglvnd}/lib:${pkgsStable.mesa}/lib"

              # Ensure Qt uses XCB and can find the emulator's bundled Qt plugins
              export QT_QPA_PLATFORM=xcb
              export QT_XCB_GL_INTEGRATION=none
              export QT_PLUGIN_PATH="$ANDROID_HOME/emulator/lib64/qt/plugins''${QT_PLUGIN_PATH:+:$QT_PLUGIN_PATH}"

              echo "Android dev shell ready."
              echo "Hint: If adb doesn't see your device, ensure programs.adb.enable = true and you're in the 'adbusers' group on NixOS."
              [ -n "$SHELL" ] && [ -x "$SHELL" ] && exec "$SHELL" || exec /bin/sh
            '';
          };
        });
    };
}

# Install steps:
# 1. Enter the flake shell with `nix develop` or `nix-shell` (you might have to set some stuff with
#   non-free software and android licenses.
# 2. Start android-studio and select the project directory `./android/`.
# 3. In android-studio, open the settings and go to `Languages & Frameworks > Android SDK`.
# 4. Edit the SDK path to point to `$HOME/Android`.
# 5. Accept all the licenses and software installs.
# 6. You should now be able to use android-studio normally.
