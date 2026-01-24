import 'package:sturdy_pancake_bindings/sturdy_pancake_bindings.dart';

extension type Minecraft._(JavaObject _this) {
  @pragma('vm:external-name', '::net.minecraft.client.Minecraft::getGameProfile')
  external GameProfile get gameProfile;

  @pragma('vm:external-name', '::net.minecraft.client.Minecraft::getInstance')
  external static Minecraft getInstance();
}

extension type GameProfile._(JavaObject _this) {
  @pragma('vm:external-name', '::com.mojang.authlib.GameProfile::name')
  external String get name;
}

extension type Event<T>._(JavaObject _this) {
  @pragma('vm:external-name', '::net.fabricmc.fabric.api.event.Event::register')
  external void register(T listener);
}

extension type ClientSendMessageEvents._(JavaObject _this) {
  @pragma('vm:external-name', '::net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents::CHAT::get')
  external static Event<void Function(String)> get chat;
}