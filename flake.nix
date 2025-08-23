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
            ];

            shellHook = ''
              export ANDROID_HOME="$HOME/Android/Sdk"
              export ANDROID_SDK_ROOT="$ANDROID_HOME"
              export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
              export JAVA_HOME="${pkgsStable.jdk17.home}"

              echo "Android dev shell ready."
              echo "Hint: If adb doesn't see your device, ensure programs.adb.enable = true and you're in the 'adbusers' group on NixOS."
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
