# Simple proxy to the flake devShell for users who prefer `nix-shell`.
# Requires enabling flakes in your Nix config (nix-command, flakes).
let
  flake = builtins.getFlake (toString ./.);
  system = builtins.currentSystem;
  # Fallback to x86_64-linux if currentSystem is not set (rare)
  devShell = if builtins.hasAttr system flake.devShells
    then flake.devShells.${system}.default
    else flake.devShells.x86_64-linux.default;
in
  devShell

# For full installation instructions, see the flake.nix file.
