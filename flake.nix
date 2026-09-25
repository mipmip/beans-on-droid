{
  description = "Beans on Droid: unofficial Android reader for hmans/beans issue trackers";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      systems = [
        "x86_64-linux"
        "aarch64-linux"
        "x86_64-darwin"
        "aarch64-darwin"
      ];

      pkgsFor = system: import nixpkgs {
        inherit system;
        config = {
          allowUnfree = true;
          android_sdk.accept_license = true;
        };
      };

      forAllSystems = f: nixpkgs.lib.genAttrs systems (system: f (pkgsFor system));

      androidFor = pkgs: pkgs.androidenv.composeAndroidPackages {
        cmdLineToolsVersion = "22.0";
        platformToolsVersion = "37.0.1";
        buildToolsVersions = [ "37.0.0" ];
        platformVersions = [ "37.0" "36" ];
        includeEmulator = false;
        includeSystemImages = false;
        includeSources = false;
        includeNDK = false;
      };
    in
    {
      devShells = forAllSystems (pkgs:
        let
          android = androidFor pkgs;
          sdk = android.androidsdk;
        in
        {
          default = pkgs.mkShell {
            packages = [
              pkgs.jdk17
              pkgs.gradle
              pkgs.kotlin
              sdk
              pkgs.git
              pkgs.jq
              pkgs.shellcheck
            ];

            JAVA_HOME = "${pkgs.jdk17}";
            ANDROID_HOME = "${sdk}/libexec/android-sdk";
            ANDROID_SDK_ROOT = "${sdk}/libexec/android-sdk";

            shellHook = ''
              export GRADLE_OPTS="-Dorg.gradle.project.android.aapt2FromMavenOverride=$ANDROID_SDK_ROOT/build-tools/37.0.0/aapt2"
              echo "beans-on-droid dev shell: jdk $(javac -version 2>&1), sdk at $ANDROID_SDK_ROOT"
            '';
          };
        });

      checks = forAllSystems (pkgs: {
        shell-scripts = pkgs.runCommand "check-shell-scripts"
          {
            nativeBuildInputs = [ pkgs.shellcheck ];
            src = ./scripts;
          } ''
          shellcheck "$src"/*.sh
          touch "$out"
        '';

        nix-format = pkgs.runCommand "check-nix-format"
          {
            nativeBuildInputs = [ pkgs.nixpkgs-fmt ];
            src = ./flake.nix;
          } ''
          nixpkgs-fmt --check "$src"
          touch "$out"
        '';
      });

      formatter = forAllSystems (pkgs: pkgs.nixpkgs-fmt);
    };
}
