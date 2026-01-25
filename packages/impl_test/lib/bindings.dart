import 'package:sturdy_pancake_bindings/sturdy_pancake_bindings.dart';

extension type Minecraft._(JavaObject _) {
  @pragma('vm:external-name', '::net.minecraft.client.Minecraft::getGameProfile')
  external GameProfile get gameProfile;

  @pragma('vm:external-name', '::net.minecraft.client.Minecraft::gui::get')
  external Gui get gui;

  @pragma('vm:external-name', '::net.minecraft.client.Minecraft::getInstance')
  external static Minecraft get instance;
}

extension type GameProfile._(JavaObject _) {
  @pragma('vm:external-name', '::com.mojang.authlib.GameProfile::name')
  external String get name;
}

extension type Gui._(JavaObject _) {
  @pragma('vm:external-name', '::net.minecraft.client.gui.Gui::getChat')
  external ChatComponent get chat;
}

extension type ChatComponent._(JavaObject _) {
  @pragma('vm:external-name', '::net.minecraft.client.gui.components.ChatComponent::addMessage')
  external void addMessage(Component message);
}

extension type Component._(JavaObject _) {
  @pragma('vm:external-name', '::net.minecraft.network.chat.Component::literal')
  external factory Component.literal(String text);
}

extension type Event<T>._(JavaObject _) {
  @pragma('vm:external-name', '::net.fabricmc.fabric.api.event.Event::register')
  external void register(T listener);
}

extension type ClientSendMessageEvents._(JavaObject _) {
  @pragma('vm:external-name', '::net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents::CHAT::get')
  external static Event<void Function(String)> get chat;
}

extension type Identifier._(JavaObject _) {
  @pragma('vm:external-name', '::net.minecraft.resources.Identifier::fromNamespaceAndPath')
  external factory Identifier.fromNamespaceAndPath(String namespace, String path);
}

// ---

extension type Widget._(JavaObject _) {}

extension type Alignment._(JavaObject _) {
  @pragma('vm:external-name', '::io.wispforest.owo.braid.core.Alignment::TOP::get')
  external static Alignment get top;
}

extension type Align._(JavaObject _) implements Widget {
  @pragma('vm:external-name', '::io.wispforest.owo.braid.widgets.basic.Align::<init>')
  external factory Align(Alignment alignment, Widget child);
}

extension type Label._(JavaObject _) implements Widget {
  @pragma('vm:external-name', '::io.wispforest.owo.braid.widgets.label.Label::literal')
  external factory Label.literal(String text);
}

// ---

extension type HudElement._(JavaObject _) {}

extension type BraidHudElement._(JavaObject _) implements HudElement {
  @pragma('vm:external-name', '::io.wispforest.owo.braid.util.BraidHudElement::<init>')
  external factory BraidHudElement(Widget widget);
}

extension type HudElementRegistry._(JavaObject _) {
  @pragma('vm:external-name', '::net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry::addLast')
  external static void addLast(Identifier id, HudElement element);
}