import 'core_lib.dart';

extension type Minecraft._(JavaObject _this) {
  @pragma('vm:external-name', 'method::net.minecraft.client.Minecraft::getGameProfile')
  external GameProfile getGameProfile();

  @pragma('vm:external-name', 'method::net.minecraft.client.Minecraft::getInstance')
  external static Minecraft getInstance();
}

extension type GameProfile._(JavaObject _this) {
  @pragma('vm:external-name', 'method::com.mojang.authlib.GameProfile::name')
  external String get name;
}