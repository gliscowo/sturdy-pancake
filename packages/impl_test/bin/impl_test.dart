import 'dart:async';
import 'dart:isolate';

import 'package:impl_test/bindings.dart';
import 'package:sturdy_pancake_bindings/sturdy_pancake_bindings.dart';

late final Minecraft minecraft;

void main() async {
  setupBindingsResolver(['package:impl_test/bindings.dart']);
  minecraft = Minecraft.instance;

  HudElementRegistry.addLast(
    Identifier.fromNamespaceAndPath('sturdy-pancake', 'a_dart_hud_element'),
    BraidHudElement(Center(Label.literal('braid moment????'))),
  );

  ClientSendMessageEvents.chat.register(
    lambda(onChatMessage, r'net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents$Chat'),
  );
}

void onChatMessage(String message) {
  print('chat message: $message');

  Isolate.run(() async {
    print('isolate-ing!');
    await Future.delayed(const Duration(seconds: 2));
    return 'message length: ${message.length}';
  }).then((value) {
    minecraft.gui.chat.addMessage(Component.literal('result from isolate: $value'));
  });
}
